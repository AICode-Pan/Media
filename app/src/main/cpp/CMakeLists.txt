cmake_minimum_required(VERSION 3.4.1)

add_library( # Sets the name of the library.
        media-handle
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        audio_handler.cpp
        )
add_library(ffmpeg
        SHARED
        IMPORTED)
set_target_properties(ffmpeg
        PROPERTIES IMPORTED_LOCATION
        ../../../../libs/${ANDROID_ABI}/libffmpeg.so)
add_library(mp3lame
        SHARED
        IMPORTED)
set_target_properties(mp3lame
        PROPERTIES IMPORTED_LOCATION
        ../../../../libs/${ANDROID_ABI}/libmp3lame.so)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

include_directories(.)
include_directories(include)
include_directories(include/armeabi-v7a)

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

target_link_libraries( # Specifies the target library.
        media-handle
        ffmpeg
        mp3lame

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})