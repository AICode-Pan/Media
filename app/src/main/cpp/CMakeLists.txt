cmake_minimum_required(VERSION 3.4.1)

add_library( # Sets the name of the library.
        media-handle
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        audio_handler.cpp
        )
add_library(avcodec
        SHARED
        IMPORTED)
set_target_properties(avcodec
        PROPERTIES IMPORTED_LOCATION
        ../../../../libs/${ANDROID_ABI}/libavcodec.so)
add_library(avdevice
        SHARED
        IMPORTED)
set_target_properties(avdevice
        PROPERTIES IMPORTED_LOCATION
        ../../../../libs/${ANDROID_ABI}/libavdevice.so)
add_library(avfilter
        SHARED
        IMPORTED)
set_target_properties(avfilter
        PROPERTIES IMPORTED_LOCATION
        ../../../../libs/${ANDROID_ABI}/libavfilter.so)
add_library(avformat
        SHARED
        IMPORTED)
set_target_properties(avformat
        PROPERTIES IMPORTED_LOCATION
        ../../../../libs/${ANDROID_ABI}/libavformat.so)
add_library(avresample
        SHARED
        IMPORTED)
set_target_properties(avresample
        PROPERTIES IMPORTED_LOCATION
        ../../../../libs/${ANDROID_ABI}/libavresample.so)
add_library(avutil
        SHARED
        IMPORTED)
set_target_properties(avutil
        PROPERTIES IMPORTED_LOCATION
        ../../../../libs/${ANDROID_ABI}/libavutil.so)
add_library(swresample
        SHARED
        IMPORTED)
set_target_properties(swresample
        PROPERTIES IMPORTED_LOCATION
        ../../../../libs/${ANDROID_ABI}/libswresample.so)
add_library(swscale
        SHARED
        IMPORTED)
set_target_properties(swscale
        PROPERTIES IMPORTED_LOCATION
        ../../../../libs/${ANDROID_ABI}/libswscale.so)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

include_directories(.)
include_directories(includes)

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

target_link_libraries( # Specifies the target library.
        media-handle
        avcodec
        avdevice
        avfilter
        avformat
        avresample
        avutil
        swresample
        swscale

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})