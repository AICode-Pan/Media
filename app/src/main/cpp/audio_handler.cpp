#include <jni.h>
#include <string>
#include <jni.h>
#include <android/log.h>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,"audio_handler", __VA_ARGS__)


//
// Created by PC on 2020/5/23.
//
extern "C" {
#include <include/libavcodec/avcodec.h>
JNIEXPORT jint

JNICALL
Java_com_explain_media_utils_FFmpegCmd_pcm2aac(JNIEnv *env, jclass clazz, jstring file_path,
                                               jstring new_file_path) {
    char str[25];
    int i;
    i = sprintf(str, "sshh%d", avcodec_version());
    LOGD( "Output:avcodec_version = %d\n", i );
    return i;
}
}

