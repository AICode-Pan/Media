#include <jni.h>
#include <stdio.h>
#include <android/log.h>

#define LOG_TAG "FFmpegEncode"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

#ifdef __cplusplus
extern "C" {
#endif
#include <libavutil/opt.h>
#include <libavcodec/avcodec.h>
#include <libavutil/imgutils.h>

JNIEXPORT jint JNICALL Java_com_explain_media_Utils_FFmpegCmd_pcm2aac
(JNIEnv *env, jobject, jstring filePath, jstring newFilePath)
{
    char *padts = (char *)malloc(sizeof(char) * 7);
    int profile = 2;	                                        //AAC LC
    int freqIdx = 4;                                            //44.1KHz
    int chanCfg = 2;            //MPEG-4 Audio Channel Configuration. 1 Channel front-center
    padts[0] = (char)0xFF;      // 11111111     = syncword
    padts[1] = (char)0xF1;      // 1111 1 00 1  = syncword MPEG-2 Layer CRC
    padts[2] = (char)(((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
    padts[6] = (char)0xFC;

	AVCodec *pCodec;
    AVCodecContext *pCodecCtx= NULL;
    int i, ret, got_output;
    FILE *fp_in;
	FILE *fp_out;

    AVFrame *pFrame;
	uint8_t* frame_buf;
	int size=0;

	AVPacket pkt;
	int y_size;
	int framecnt=0;


    const char *filename_in, *filename_out;

    filename_in = env->GetStringUTFChars(filePath, NULL);

    AVCodecID codec_id=AV_CODEC_ID_AAC;
    filename_out = env->GetStringUTFChars(newFilePath, NULL);

	int framenum=100000;

	avcodec_register_all();

    pCodec = avcodec_find_encoder(codec_id);
    if (!pCodec) {
        LOGI("Codec not found\n");
        return -1;
    }

    pCodecCtx = avcodec_alloc_context3(pCodec);
    if (!pCodecCtx) {
        LOGI("Could not allocate video codec context\n");
        return -1;
    }

	pCodecCtx->codec_id         = codec_id;
	pCodecCtx->codec_type       = AVMEDIA_TYPE_AUDIO;
	pCodecCtx->sample_fmt       = AV_SAMPLE_FMT_FLTP;
	pCodecCtx->sample_rate      = 44100;

	pCodecCtx->channel_layout   = AV_CH_LAYOUT_STEREO;
	pCodecCtx->channels         = av_get_channel_layout_nb_channels(pCodecCtx->channel_layout);
//	pCodecCtx->bit_rate         = 64000;

    int ccode = avcodec_open2(pCodecCtx, pCodec, NULL);
    LOGI("avcodec_open2 result: %5d",ccode);
    if ((ret = ccode) < 0) {
        LOGI("Could not open codec");
        return -1;
    }

	pFrame = av_frame_alloc();

	pFrame->nb_samples = pCodecCtx->frame_size;
	pFrame->format = pCodecCtx->sample_fmt;
    pFrame->channels = 2;

	size = av_samples_get_buffer_size(NULL, pCodecCtx->channels, pCodecCtx->frame_size, pCodecCtx->sample_fmt, 0);
	frame_buf = (uint8_t *)av_malloc(size);
    /**
     *   avcodec_fill_audio_frame 实现：
     *   frame_buf是根据声道数、采样率和采样格式决定大小的。
     *   调用次函数后，AVFrame存储音频数据的成员有以下变化：data[0]指向frame_buf，data[1]指向frame_buf长度的一半位置
     *   data[0] == frame_buf , data[1] == frame_buf + pCodecCtx->frame_size * av_get_bytes_per_sample(pCodecCtx->sample_fmt)
     */

	ret = avcodec_fill_audio_frame(pFrame, pCodecCtx->channels, pCodecCtx->sample_fmt, (const uint8_t*)frame_buf, size, 0);
    if (ret < 0) {
        LOGI("avcodec_fill_audio_frame error ");
        return 0;
    }


    //Input raw data
	fp_in = fopen(filename_in, "rb");
	if (!fp_in) {
        LOGI("Could not open %s\n", filename_in);
		return -1;
	}
	//Output bitstream
	fp_out = fopen(filename_out, "wb");
	if (!fp_out) {
        LOGI("Could not open %s\n", filename_out);
		return -1;
	}

    //Encode
//    for (i = 0; i < framenum; i++) {
//        av_init_packet(&pkt);
//        pkt.data = NULL;    // packet data will be allocated by the encoder
//        pkt.size = 0;
//		//Read raw data
//		if (fread(frame_buf, 1, size, fp_in) <= 0){
//            LOGI("Failed to read raw data! \n");
//			return -1;
//		} else if (feof(fp_in)) {
//			break;
//		}
//
//        pFrame->pts = i;
//        ret = avcodec_encode_audio2(pCodecCtx, &pkt, pFrame, &got_output);
//        if (ret < 0) {
//            LOGI("Error encoding frame ret : %2d", ret);
//            return -1;
//        }
//
//        if (pkt.data == NULL)
//        {
//            av_free_packet(&pkt);
//            continue;
//        }
//
//
//        if (got_output) {
//            LOGI("Succeed to encode frame: %5d\tsize:%5d\n", framecnt, pkt.size);
//			framecnt++;
//
//            padts[3] = (char)(((chanCfg & 3) << 6) + ((7 + pkt.size) >> 11));
//            padts[4] = (char)(((7 + pkt.size) & 0x7FF) >> 3);
//            padts[5] = (char)((((7 + pkt.size) & 7) << 5) + 0x1F);
//            fwrite(padts, 7, 1, fp_out);
//            fwrite(pkt.data, 1, pkt.size, fp_out);
//            av_free_packet(&pkt);
//        }
//    }

    //Flush Encoder
    for (got_output = 1; got_output; i++) {
        ret = avcodec_encode_audio2(pCodecCtx, &pkt, NULL, &got_output);
        if (ret < 0) {
            LOGE("Error Flush encoding frame ret : %2d", ret);
            return -1;
        }
        if (got_output) {
            LOGI("Flush Encoder: Succeed to encode 1 frame!\tsize:%5d\n",pkt.size);
            padts[3] = (char)(((chanCfg & 3) << 6) + ((7 + pkt.size) >> 11));
            padts[4] = (char)(((7 + pkt.size) & 0x7FF) >> 3);
            padts[5] = (char)((((7 + pkt.size) & 7) << 5) + 0x1F);

            fwrite(padts, 7, 1, fp_out);
            fwrite(pkt.data, 1, pkt.size, fp_out);
            av_free_packet(&pkt);
        }
    }

    fclose(fp_out);
    avcodec_close(pCodecCtx);
    av_free(pCodecCtx);
    av_freep(&pFrame->data[0]);
    av_frame_free(&pFrame);

	return 0;
}

#ifdef __cplusplus
}
#endif

