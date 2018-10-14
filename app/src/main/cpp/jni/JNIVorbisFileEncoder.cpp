#include "jniplatform.hpp"
#include "VorbisFileEncoder.hpp"

CJNICALL(jlong)
Java_test_gleb_110_114_1android_cppiface_VorbisFileEncoder_create(
    JNIEnv *env,
    jclass type,
    jstring fileName_
) {
    JNIString fileName{env,fileName_};
    return reinterpret_cast<jlong>(new NativeRefHolder<VorbisFileEncoder>{
        std::string{fileName.c_str(),fileName.size()};
    });
}