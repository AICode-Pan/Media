//
// Created by lxh on 2020/5/18.
//

#include "jni_hello.h"
#include <string>
#include <jni.h>
#include <android/log.h>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,"keymatch", __VA_ARGS__)

extern "C" jint
Java_com_explain_media_utils_FFmpegCmd_pcm2aac(JNIEnv *env, jclass clazz, jstring file_path,
                                               jstring new_file_path) {
    LOGD("hjkhjk");
    LOGD("jgeq");
    return 0;
}
