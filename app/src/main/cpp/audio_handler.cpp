#include <jni.h>
#include <string>
#include <android/log.h>

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,"audio_handler", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,"audio_handler", __VA_ARGS__)

extern "C" {
#include <includes/libavcodec/avcodec.h>
#include <includes/libswresample/swresample.h>
#include <includes/libavutil/imgutils.h>
#include <includes/libavutil/samplefmt.h>
#include <includes/libavutil/timestamp.h>
#include <includes/libavformat/avformat.h>
#include <includes/libswscale/swscale.h>
#include <includes/libfaac/faac.h>
};

AVSampleFormat inSampleFmt = AV_SAMPLE_FMT_S16;
//    AVSampleFormat outSampleFmt = AV_SAMPLE_FMT_S16;
AVSampleFormat outSampleFmt = AV_SAMPLE_FMT_FLTP;
const int sampleRate = 44100;
const int channels = 2;
const int sampleByte = 2;
#define MAX_AUDIO_FRAME_SIZE 192000 // 1 second of 48khz 32bit audio

extern "C"
JNIEXPORT jint JNICALL
Java_com_explain_media_utils_FFmpegCmd_getAVCodecVersion(JNIEnv *env, jclass clazz) {
    char str[25];
    int i;
    i = sprintf(str, "sshh%d", avcodec_version());
    LOGD("Output:avcodec_version = %d\n", i);
    return i;
}

int flush_encoder(AVFormatContext *fmt_ctx,unsigned int stream_index){
    int ret;
    int got_frame;
    AVPacket enc_pkt;
    if (!(fmt_ctx->streams[stream_index]->codec->codec->capabilities &
          AV_CODEC_CAP_DELAY))
        return 0;
    while (1) {
        enc_pkt.data = NULL;
        enc_pkt.size = 0;
        av_init_packet(&enc_pkt);
        ret = avcodec_encode_audio2 (fmt_ctx->streams[stream_index]->codec, &enc_pkt,
                                     NULL, &got_frame);
        av_frame_free(NULL);
        if (ret < 0)
            break;
        if (!got_frame){
            ret=0;
            break;
        }
        LOGE("Flush Encoder: Succeed to encode 1 frame!\tsize:%5d\n",enc_pkt.size);
        /* mux encoded frame */
        ret = av_write_frame(fmt_ctx, &enc_pkt);
        if (ret < 0)
            break;
    }
    return ret;
}

// 音频编码
extern "C"
JNIEXPORT jint
JNICALL
Java_com_explain_media_utils_FFmpegCmd_audioEncode(JNIEnv *env, jclass clazz, jstring file_path,
                                               jstring out_file_path) {
    const char *souPath = env->GetStringUTFChars(file_path, 0);
    const char *out_file = env->GetStringUTFChars(out_file_path, 0);

    AVFormatContext* pFormatCtx;
    AVOutputFormat* fmt;
    AVStream* audio_st;
    AVCodecContext* pCodecCtx;
    AVCodec* pCodec;

    uint8_t* frame_buf;
    AVFrame* pFrame;
    AVPacket pkt;

    int got_frame=0;
    int ret=0;
    int size=0;

    FILE *in_file=NULL;	                        //Raw PCM data
    int framenum=1000;                          //Audio frame number
    int i;

    in_file= fopen(souPath, "rb");

    av_register_all();

    //Method 1.
    pFormatCtx = avformat_alloc_context();
    fmt = av_guess_format(NULL, out_file, NULL);
    pFormatCtx->oformat = fmt;


    //Method 2.
    //avformat_alloc_output_context2(&pFormatCtx, NULL, NULL, out_file);
    //fmt = pFormatCtx->oformat;

    //Open output URL
    if (avio_open(&pFormatCtx->pb,out_file, AVIO_FLAG_READ_WRITE) < 0){
        LOGE("Failed to open output file!\n");
        return -1;
    }

    audio_st = avformat_new_stream(pFormatCtx, 0);
    if (audio_st==NULL){
        return -1;
    }
    pCodecCtx = audio_st->codec;
    pCodecCtx->codec_id = AV_CODEC_ID_AAC;
    pCodecCtx->codec_type = AVMEDIA_TYPE_AUDIO;
    pCodecCtx->sample_fmt = AV_SAMPLE_FMT_S16;
    pCodecCtx->sample_rate= 44100;
    pCodecCtx->channel_layout=AV_CH_LAYOUT_STEREO;
    pCodecCtx->channels = av_get_channel_layout_nb_channels(pCodecCtx->channel_layout);
    pCodecCtx->bit_rate = 64000;

    //Show some information
    av_dump_format(pFormatCtx, 0, out_file, 1);

    pCodec = avcodec_find_encoder(pCodecCtx->codec_id);

    if (!pCodec){
        LOGE("Can not find encoder!\n");
        return -1;
    }
    if (avcodec_open2(pCodecCtx, pCodec,NULL) < 0){
        LOGE("Failed to open encoder!\n");
        return -1;
    }
    pFrame = av_frame_alloc();
    pFrame->nb_samples= pCodecCtx->frame_size;
    pFrame->format= pCodecCtx->sample_fmt;

    size = av_samples_get_buffer_size(NULL, pCodecCtx->channels,pCodecCtx->frame_size,pCodecCtx->sample_fmt, 1);
    frame_buf = (uint8_t *)av_malloc(size);
    avcodec_fill_audio_frame(pFrame, pCodecCtx->channels, pCodecCtx->sample_fmt,(const uint8_t*)frame_buf, size, 1);

    //Write Header
    avformat_write_header(pFormatCtx,NULL);

    av_new_packet(&pkt,size);

    for (i=0; i<framenum; i++){
        //Read PCM
        if (fread(frame_buf, 1, size, in_file) <= 0){
            LOGE("Failed to read raw data! \n");
            return -1;
        }else if(feof(in_file)){
            break;
        }
        pFrame->data[0] = frame_buf;  //PCM Data

        pFrame->pts=i*100;
        got_frame=0;
        //Encode
        ret = avcodec_encode_audio2(pCodecCtx, &pkt,pFrame, &got_frame);
        if(ret < 0){
            LOGE("Failed to encode!\n");
            return -1;
        }
        if (got_frame==1){
            LOGE("Succeed to encode 1 frame! \tsize:%5d\n",pkt.size);
            pkt.stream_index = audio_st->index;
            ret = av_write_frame(pFormatCtx, &pkt);
            av_free_packet(&pkt);
        }
    }

    //Flush Encoder
    ret = flush_encoder(pFormatCtx,0);
    if (ret < 0) {
        LOGE("Flushing encoder failed\n");
        return -1;
    }

    //Write Trailer
    av_write_trailer(pFormatCtx);

    //Clean
    if (audio_st){
        avcodec_close(audio_st->codec);
        av_free(pFrame);
        av_free(frame_buf);
    }
    avio_close(pFormatCtx->pb);
    avformat_free_context(pFormatCtx);

    fclose(in_file);

    return 0;
}

//音频解码
extern "C"
JNIEXPORT int
JNICALL
Java_com_explain_media_utils_FFmpegCmd_audioDecode(JNIEnv *env, jclass clazz, jstring file_path,
                                                   jstring new_file_path) {
    const char *input = env->GetStringUTFChars(file_path, 0);
    const char *output = env->GetStringUTFChars(new_file_path, 0);
    AVCodec *pCodec;
    AVCodecContext *pCodecContext;
    AVFormatContext *pFormatContext;
    struct SwrContext *au_convert_ctx;


    uint8_t *out_buffer;

    //1. 注册
    av_register_all();
    //2.打开解码器 <-- 拿到解码器  <-- 拿到id <-- 拿到stream和拿到AVCodecContext <-- 拿到AVFormatContext

    //2.1 拿到AVFormatContext
    pFormatContext = avformat_alloc_context();
    //2.1.1 打开文件
    if (avformat_open_input(&pFormatContext, input, NULL, NULL) != 0) {
        LOGE("打开文件失败!");
        return -1;
    }
    //2.2 拿到AVCodecContext
    //2.2.1 拿到流信息
    if (avformat_find_stream_info(pFormatContext, NULL) < 0) {
        LOGE("AVFormatContext获取流信息失败!");
        return -1;
    }
    //打印信息
//    av_dump_format(pFormatContext, 0, input, false);

    //2.2.2 通过streams找到audio的索引下标 也就获取到了stream
    int audioStream = -1;
    int i = 0;
    for (; i < pFormatContext->nb_streams; i++)
        if (pFormatContext->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
            audioStream = i;
            break;
        }

    if (audioStream == -1) {
        LOGE("AVMEDIA_TYPE_AUDIO索引没找到!");
        return -1;
    }
    //2.2.3 获取到AVCodecContext
    pCodecContext = pFormatContext->streams[audioStream]->codec;

    //2.2.4 通过AVCodecContext拿到id ，拿到解码器
    pCodec = avcodec_find_decoder(pCodecContext->codec_id);
    if (pCodec == NULL) {
        LOGE("AVCodec获取失败!");
        return -1;
    }
    //2.2.5 打开解码器
    if (avcodec_open2(pCodecContext, pCodec, NULL) < 0) {
        LOGE("打开解码器失败!");
        return -1;
    }

    //3. 解码  将解码数据封装在AVFrame <-- 拿到编码的数据AVPacket  <-- 读取数据源 <-- 解码文件参数设置

    //3.1 AVPacket初始化
    AVPacket *packet = (AVPacket *) av_malloc(sizeof(AVPacket));
    av_init_packet(packet);

    //3.2 解码文件参数设置
    uint64_t out_channel_layout = AV_CH_LAYOUT_STEREO;

    //nb_samples: AAC-1024 MP3-1152
    //音频帧中每个声道的采样数
    int out_nb_samples = pCodecContext->frame_size;

    //音频采样格式 量化精度
    AVSampleFormat out_sample_fmt = AV_SAMPLE_FMT_S16;
    //采样率
    int out_sample_rate = 44100;
    //声道
    int out_channels = av_get_channel_layout_nb_channels(out_channel_layout);

    //获取到 缓冲大小
    int out_buffer_size = av_samples_get_buffer_size(NULL, out_channels, out_nb_samples,
                                                     out_sample_fmt, 1);
    out_buffer = (uint8_t *) av_malloc(MAX_AUDIO_FRAME_SIZE * 2);

    //3.3 初始化AVFrame
    AVFrame *pFrame = av_frame_alloc();


    //3.4 获取到编码文件的参数信息
    //声道
    int64_t in_channel_layout = av_get_default_channel_layout(pCodecContext->channels);

    //3.5 参数设置
    au_convert_ctx = swr_alloc();
    au_convert_ctx = swr_alloc_set_opts(au_convert_ctx, out_channel_layout, out_sample_fmt,
                                        out_sample_rate,
                                        in_channel_layout, pCodecContext->sample_fmt,
                                        pCodecContext->sample_rate, 0, NULL);
    swr_init(au_convert_ctx);

    //4. 读取编码数据到AVPacket 然后将数据解码存储到AVFrame  转换存储数据
    //4.1 读取编码数据到AVPacket
    int got_picture;
    int index = 0;
    FILE *outputFile = fopen(output, "wb");
    while (av_read_frame(pFormatContext, packet) >= 0) {
        if (packet->stream_index == audioStream) {
            //4.2 将数据解码存储到AVFrame
            if (avcodec_decode_audio4(pCodecContext, pFrame, &got_picture, packet) < 0) {
                LOGE("解码失败");
                return -1;
            }

            if (got_picture > 0) {
                //4.3 转换音频数据
                swr_convert(au_convert_ctx, &out_buffer, MAX_AUDIO_FRAME_SIZE,
                            (const uint8_t **) pFrame->data, pFrame->nb_samples);
                LOGE("index:%5d\t pts:%lld\t packet size:%d\n", index, packet->pts, packet->size);

                //4.4 存储数据
                fwrite(out_buffer, 1, static_cast<size_t>(out_buffer_size), outputFile);
                index++;
            }
        }
        //5. 释放相关资源
        av_packet_unref(packet);
    }

    swr_free(&au_convert_ctx);
    fclose(outputFile);
    av_free(out_buffer);
    // Close the codec
    avcodec_close(pCodecContext);
    // Close the video file
    avformat_close_input(&pFormatContext);

    env->ReleaseStringUTFChars(file_path, input);
    env->ReleaseStringUTFChars(new_file_path, output);
    return 0;
}