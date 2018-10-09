//
// Created by frank on 2018/2/3.
//
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libswresample/swresample.h"
#include "AVpacket_queue.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <stdio.h>
#include <unistd.h>
#include <libavutil/imgutils.h>
#include <android/log.h>
#include <pthread.h>
#include <jni.h>
#include <libavutil/time.h>

#define TAG "MediaPlayer"
#define LOGI(FORMAT,...) __android_log_print(ANDROID_LOG_INFO, TAG, FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR, TAG, FORMAT,##__VA_ARGS__);

#define MAX_AUDIO_FRAME_SIZE 48000 * 4
#define PACKET_SIZE 50
#define MIN_SLEEP_TIME_US 1000ll
#define AUDIO_TIME_ADJUST_US -200000ll

typedef struct MediaPlayer{
    AVFormatContext* format_context;
    int video_stream_index;
    int audio_stream_index;
    AVCodecContext* video_codec_context;
    AVCodecContext* audio_codec_context;
    AVCodec* video_codec;
    AVCodec* audio_codec;
    ANativeWindow* native_window;
    uint8_t* buffer;
    AVFrame* yuv_frame;
    AVFrame* rgba_frame;
    int video_width;
    int video_height;
    SwrContext* swrContext;
    int out_channel_nb;
    int out_sample_rate;
    enum AVSampleFormat out_sample_fmt;
    jobject audio_track;
    jmethodID audio_track_write_mid;
    uint8_t* audio_buffer;
    AVFrame* audio_frame;
    AVPacketQueue* packets[2];
    pthread_mutex_t mutex;
    pthread_cond_t cond;
    int64_t start_time;
    int64_t audio_clock;
    pthread_t write_thread;
    pthread_t video_thread;
    pthread_t audio_thread;
}MediaPlayer;

typedef struct Decoder{
    MediaPlayer* player;
    int stream_index;
}Decoder;

JavaVM* javaVM;
MediaPlayer* player;

jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved){
    javaVM = vm;
    return JNI_VERSION_1_6;
}

//初始化输入格式上下文
int init_input_format_context(MediaPlayer* player, const char* file_name){
    //注册所有组件
    av_register_all();
    //分配上下文
    player->format_context = avformat_alloc_context();
    //打开视频文件
    if(avformat_open_input(&player->format_context, file_name, NULL, NULL)!=0) {
        LOGE("Couldn't open file:%s\n", file_name);
        return -1;
    }
    //检索多媒体流信息
    if(avformat_find_stream_info(player->format_context, NULL)<0) {
        LOGE("Couldn't find stream information.");
        return -1;
    }
    //寻找音视频流索引位置
    int i;
    player->video_stream_index = -1;
    player->audio_stream_index = -1;
    for (i = 0; i < player->format_context->nb_streams; i++) {
        if (player->format_context->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO
            && player->video_stream_index < 0) {
            player->video_stream_index = i;
        } else if (player->format_context->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO
            && player->audio_stream_index < 0) {
            player->audio_stream_index = i;
        }
    }
    if(player->video_stream_index==-1) {
        LOGE("couldn't find a video stream.");
        return -1;
    }
    if(player->audio_stream_index==-1) {
        LOGE("couldn't find a audio stream.");
        return -1;
    }
    LOGI("video_stream_index=%d", player->video_stream_index);
    LOGI("audio_stream_index=%d", player->audio_stream_index);
    return 0;
}

//打开音视频解码器
int init_condec_context(MediaPlayer* player){
    //获取codec上下文指针
    player->video_codec_context = player->format_context->streams[player->video_stream_index]->codec;
    //寻找视频流的解码器
    player->video_codec = avcodec_find_decoder(player->video_codec_context->codec_id);
    if(player->video_codec == NULL) {
        LOGE("couldn't find video Codec.");
        return -1;
    }
    if(avcodec_open2(player->video_codec_context, player->video_codec, NULL) < 0) {
        LOGE("Couldn't open video codec.");
        return -1;
    }
    player->audio_codec_context = player->format_context->streams[player->audio_stream_index]->codec;
    player->audio_codec = avcodec_find_decoder(player->audio_codec_context->codec_id);
    if( player->audio_codec == NULL) {
        LOGE("couldn't find audio Codec.");
        return -1;
    }
    if(avcodec_open2(player->audio_codec_context, player->audio_codec, NULL) < 0) {
        LOGE("Couldn't open audio codec.");
        return -1;
    }
    // 获取视频宽高
    player->video_width = player->video_codec_context->width;
    player->video_height = player->video_codec_context->height;
    return 0;
}

//视频解码
void video_player_prepare(MediaPlayer* player, JNIEnv* env, jobject surface){
    // 获取native window
    player->native_window = ANativeWindow_fromSurface(env, surface);
}

//获取当前播放时间
int64_t get_play_time(MediaPlayer* player){
    return (int64_t)(av_gettime() - player->start_time);
}

/**
 * 延迟等待，音视频同步
 */
void player_wait_for_frame(MediaPlayer *player, int64_t stream_time) {
    pthread_mutex_lock(&player->mutex);
    for(;;){
        int64_t current_video_time = get_play_time(player);
        int64_t sleep_time = stream_time - current_video_time;
        if (sleep_time < -300000ll) {
            // 300 ms late
            int64_t new_value = player->start_time - sleep_time;
            player->start_time = new_value;
            pthread_cond_broadcast(&player->cond);
        }

        if (sleep_time <= MIN_SLEEP_TIME_US) {
            // We do not need to wait if time is slower then minimal sleep time
            break;
        }

        if (sleep_time > 500000ll) {
            // if sleep time is bigger then 500ms just sleep this 500ms
            // and check everything again
            sleep_time = 500000ll;
        }
        //等待指定时长
        pthread_cond_timeout_np(&player->cond, &player->mutex,
                                                  (unsigned int) (sleep_time / 1000ll));
    }
    pthread_mutex_unlock(&player->mutex);
}

//视频解码
int decode_video(MediaPlayer* player, AVPacket* packet){
    // 设置native window的buffer大小,可自动拉伸
    ANativeWindow_setBuffersGeometry(player->native_window,  player->video_width,
                                     player->video_height, WINDOW_FORMAT_RGBA_8888);

    ANativeWindow_Buffer windowBuffer;
    //申请内存
    player->yuv_frame = av_frame_alloc();
    player->rgba_frame = av_frame_alloc();
    if(player->rgba_frame == NULL || player->yuv_frame == NULL) {
        LOGE("Couldn't allocate video frame.");
        return -1;
    }

    // buffer中数据用于渲染,且格式为RGBA
    int numBytes=av_image_get_buffer_size(AV_PIX_FMT_RGBA, player->video_width, player->video_height, 1);

    player->buffer = (uint8_t *)av_malloc(numBytes*sizeof(uint8_t));
    av_image_fill_arrays(player->rgba_frame->data, player->rgba_frame->linesize, player->buffer, AV_PIX_FMT_RGBA,
                         player->video_width, player->video_height, 1);

    // 由于解码出来的帧格式不是RGBA的,在渲染之前需要进行格式转换
    struct SwsContext *sws_ctx = sws_getContext(
            player->video_width,
            player->video_height,
            player->video_codec_context->pix_fmt,
            player->video_width,
            player->video_height,
            AV_PIX_FMT_RGBA,
            SWS_BILINEAR,
            NULL,
            NULL,
            NULL);

    int frameFinished;
    //对该帧进行解码
    int ret = avcodec_decode_video2(player->video_codec_context, player->yuv_frame, &frameFinished, packet);
    if(ret < 0){
        LOGE("avcodec_decode_video2 error...");
        return -1;
    }
    if (frameFinished) {
        // lock native window
        ANativeWindow_lock(player->native_window, &windowBuffer, 0);
        // 格式转换
        sws_scale(sws_ctx, (uint8_t const * const *)player->yuv_frame->data,
                  player->yuv_frame->linesize, 0, player->video_height,
                  player->rgba_frame->data, player->rgba_frame->linesize);
        // 获取stride
        uint8_t * dst = windowBuffer.bits;
        int dstStride = windowBuffer.stride * 4;
        uint8_t * src = player->rgba_frame->data[0];
        int srcStride = player->rgba_frame->linesize[0];
        // 由于window的stride和帧的stride不同,因此需要逐行复制
        int h;
        for (h = 0; h < player->video_height; h++) {
            memcpy(dst + h * dstStride, src + h * srcStride, (size_t) srcStride);
        }

        //计算延迟
        int64_t pts = av_frame_get_best_effort_timestamp(player->yuv_frame);
        AVStream *stream = player->format_context->streams[player->video_stream_index];
        //转换（不同时间基时间转换）
        int64_t time = av_rescale_q(pts, stream->time_base, AV_TIME_BASE_Q);
        //音视频帧同步
        player_wait_for_frame(player, time);

        ANativeWindow_unlockAndPost(player->native_window);
    }
//    //延迟等待
//    usleep(1000 * 16);
    return 0;
}

//音频解码初始化
void audio_decoder_prepare(MediaPlayer* player) {
    //frame->16bit 44100 PCM 统一音频采样格式与采样率
    player->swrContext = swr_alloc();

    //输入的采样格式
    enum AVSampleFormat in_sample_fmt = player->audio_codec_context->sample_fmt;
    //输出采样格式16bit PCM
    player->out_sample_fmt = AV_SAMPLE_FMT_S16;
    //输入采样率
    int in_sample_rate = player->audio_codec_context->sample_rate;
    //输出采样率
    player->out_sample_rate = in_sample_rate;
    //声道布局（2个声道，默认立体声stereo）
    uint64_t in_ch_layout = player->audio_codec_context->channel_layout;
    //输出的声道布局（立体声）
    uint64_t out_ch_layout = AV_CH_LAYOUT_STEREO;

    swr_alloc_set_opts(player->swrContext,
                       out_ch_layout, player->out_sample_fmt, player->out_sample_rate,
                       in_ch_layout, in_sample_fmt, in_sample_rate,
                       0, NULL);
    swr_init(player->swrContext);
    //输出的声道个数
    player->out_channel_nb = av_get_channel_layout_nb_channels(out_ch_layout);
}

//音频播放器
void audio_player_prepare(MediaPlayer* player, JNIEnv* env, jclass jthiz){
    jclass player_class = (*env)->GetObjectClass(env,jthiz);
    if(!player_class){
        LOGE("player_class not found...");
    }
    //AudioTrack对象
    jmethodID audio_track_method = (*env)->GetMethodID(
            env,player_class,"createAudioTrack","(II)Landroid/media/AudioTrack;");
    if(!audio_track_method){
        LOGE("audio_track_method not found...");
    }
    jobject audio_track = (*env)->CallObjectMethod(
            env,jthiz,audio_track_method, player->out_sample_rate, player->out_channel_nb);

    //调用play方法
    jclass audio_track_class = (*env)->GetObjectClass(env, audio_track);
    jmethodID audio_track_play_mid = (*env)->GetMethodID(env,audio_track_class,"play","()V");
    (*env)->CallVoidMethod(env, audio_track, audio_track_play_mid);

    player->audio_track = (*env)->NewGlobalRef(env, audio_track);
    //获取write()方法
    player->audio_track_write_mid = (*env)->GetMethodID(env,audio_track_class,"write","([BII)I");

    //16bit 44100 PCM 数据
    player->audio_buffer = (uint8_t *)av_malloc(MAX_AUDIO_FRAME_SIZE);
    //解压缩数据
    player->audio_frame = av_frame_alloc();
}

//音频解码
int decode_audio(MediaPlayer* player, AVPacket* packet){
    int got_frame = 0, ret;
    //解码
    ret = avcodec_decode_audio4(player->audio_codec_context, player->audio_frame, &got_frame, packet);
    if(ret < 0){
        LOGE("avcodec_decode_audio4 error...");
        return -1;
    }
    //解码一帧成功
    if(got_frame > 0){
        //音频格式转换
        swr_convert(player->swrContext, &player->audio_buffer,  MAX_AUDIO_FRAME_SIZE, (const uint8_t **)player->audio_frame->data, player->audio_frame->nb_samples);
        int out_buffer_size = av_samples_get_buffer_size(NULL, player->out_channel_nb,
                                                         player->audio_frame->nb_samples, player->out_sample_fmt, 1);

        //音视频帧同步
        int64_t pts = packet->pts;
        if (pts != AV_NOPTS_VALUE) {
            AVStream *stream = player->format_context->streams[player->audio_stream_index];
            player->audio_clock = av_rescale_q(pts, stream->time_base, AV_TIME_BASE_Q);
            player_wait_for_frame(player, player->audio_clock + AUDIO_TIME_ADJUST_US);
        }

        if(javaVM != NULL){
            JNIEnv * env;
            (*javaVM)->AttachCurrentThread(javaVM, &env, NULL);
            jbyteArray audio_sample_array = (*env)->NewByteArray(env,out_buffer_size);
            jbyte* sample_byte_array = (*env)->GetByteArrayElements(env,audio_sample_array,NULL);
            //拷贝缓冲数据
            memcpy(sample_byte_array, player->audio_buffer, (size_t) out_buffer_size);
            //释放数组
            (*env)->ReleaseByteArrayElements(env,audio_sample_array,sample_byte_array,0);
            //调用AudioTrack的write方法进行播放
            (*env)->CallIntMethod(env, player->audio_track, player->audio_track_write_mid,
                                  audio_sample_array,0,out_buffer_size);
            //释放局部引用
            (*env)->DeleteLocalRef(env,audio_sample_array);
//        usleep(1000 * 16);
        }
    }
    if(javaVM != NULL){
        (*javaVM)->DetachCurrentThread(javaVM);
    }
    return 0;
}

//初始化队列
void init_queue(MediaPlayer* player, int size){
    int i;
    for (i = 0; i < 2; ++i) {
        AVPacketQueue* queue = queue_init(size);
        player->packets[i] = queue;
    }
}

//释放队列
void delete_queue(MediaPlayer* player){
    int i;
    for (i = 0; i < 2; ++i) {
        queue_free(player->packets[i]);
    }
}

//读取AVPacket线程(生产者)
void* write_packet_to_queue(void* arg){
    MediaPlayer* player = (MediaPlayer*)arg;
    AVPacket packet, *pkt = &packet;
    int ret;
    for(;;){
        ret = av_read_frame(player->format_context, pkt);
        if(ret < 0){
            break;
        }
        if(pkt->stream_index == player->video_stream_index || pkt->stream_index == player->audio_stream_index){
            //根据AVPacket->stream_index获取对应的队列
            AVPacketQueue *queue = player->packets[pkt->stream_index];
            pthread_mutex_lock(&player->mutex);
            AVPacket* data = queue_push(queue, &player->mutex, &player->cond);
            pthread_mutex_unlock(&player->mutex);
            //拷贝（间接赋值，拷贝结构体数据）
            *data = packet;
        }
    }
}

//音视频解码线程(消费者)
void* decode_func(void* arg){
    Decoder *decoder_data = (Decoder*)arg;
    MediaPlayer *player = decoder_data->player;
    int stream_index = decoder_data->stream_index;
    //根据stream_index获取对应的AVPacket队列
    AVPacketQueue *queue = player->packets[stream_index];
    int ret = 0;
    int video_frame_count = 0, audio_frame_count = 0;
    for(;;) {
        pthread_mutex_lock(&player->mutex);
        AVPacket *packet = (AVPacket*)queue_pop(queue, &player->mutex, &player->cond);
        pthread_mutex_unlock(&player->mutex);

        if(stream_index == player->video_stream_index) {//视频流
            ret = decode_video(player, packet);
            LOGI("decode video stream = %d", video_frame_count++);
        } else if(stream_index == player->audio_stream_index) {//音频流
            ret = decode_audio(player, packet);
            LOGI("decode audio stream = %d", audio_frame_count++);
        }
        av_packet_unref(packet);
        if(ret < 0){
            break;
        }
    }
}

JNIEXPORT jint JNICALL Java_com_frank_ffmpeg_MediaPlayer_setup
        (JNIEnv * env, jclass clazz, jstring filePath, jobject surface){

    const char *file_name = (*env)->GetStringUTFChars(env, filePath, JNI_FALSE);
    int ret;
    player = malloc(sizeof(MediaPlayer));
    if(player == NULL){
        return -1;
    }
    //初始化输入格式上下文
    ret = init_input_format_context(player, file_name);
    if(ret < 0){
        return ret;
    }
    //初始化音视频解码器
    ret = init_condec_context(player);
    if(ret < 0){
        return ret;
    }
    //初始化视频surface
    video_player_prepare( player, env, surface);
    //初始化音频相关参数
    audio_decoder_prepare(player);
    //初始化音频播放器
    audio_player_prepare(player, env,  clazz);
    //初始化音视频packet队列
    init_queue(player, PACKET_SIZE);

    return 0;
}

JNIEXPORT jint JNICALL Java_com_frank_ffmpeg_MediaPlayer_play
        (JNIEnv * env, jclass clazz){
    pthread_mutex_init(&player->mutex, NULL);
    pthread_cond_init(&player->cond, NULL);

    //生产者线程
    pthread_create(&player->write_thread, NULL, write_packet_to_queue, (void*)player);
    sleep(1);
    player->start_time = 0;

    //消费者线程
    Decoder data1 = {player, player->video_stream_index}, *decoder_data1 = &data1;
    pthread_create(&player->video_thread, NULL, decode_func, (void*)decoder_data1);

    Decoder data2 = {player, player->audio_stream_index}, *decoder_data2 = &data2;
    pthread_create(&player->audio_thread,NULL,decode_func,(void*)decoder_data2);


    pthread_join(player->write_thread, NULL);
    pthread_join(player->video_thread, NULL);
    pthread_join(player->audio_thread, NULL);

    return 0;
}

JNIEXPORT void JNICALL Java_com_frank_ffmpeg_MediaPlayer_release
        (JNIEnv * env, jclass clazz){
    //释放内存以及关闭文件
    free(player->audio_track);
    free(player->audio_track_write_mid);
    av_free(player->buffer);
    av_free(player->rgba_frame);
    av_free(player->yuv_frame);
    av_free(player->audio_buffer);
    av_free(player->audio_frame);
    avcodec_close(player->video_codec_context);
    avcodec_close(player->audio_codec_context);
    avformat_close_input(&player->format_context);
    ANativeWindow_release(player->native_window);
    delete_queue(player);
    pthread_cond_destroy(&player->cond);
    pthread_mutex_destroy(&player->mutex);
    free(player);
    (*javaVM)->DestroyJavaVM(javaVM);
}
