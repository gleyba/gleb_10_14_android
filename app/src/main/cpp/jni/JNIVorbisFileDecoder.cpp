#include "jniplatform.hpp"
#include "VorbisFileDecoder.hpp"
#include "JNISoundEnergyListener.hpp"
#include "platform.hpp"

CJNICALL(jlong)
Java_test_gleb_110_114_1android_cppiface_VorbisFileDecoder_create(
    JNIEnv *env,
    jclass type,
    jstring fileName_,
    jint channels,
    jint sampleRate
) {
    JNIString fileName{env,fileName_};
    return reinterpret_cast<jlong>(new NativeRefHolder<VorbisFileDecoder>{
        std::string{fileName.c_str(),fileName.size()},
        channels,
        sampleRate
    });
}

CJNICALL(jboolean)
Java_test_gleb_110_114_1android_cppiface_VorbisFileDecoder_nativeInitialize(
    JNIEnv *env,
    jclass type,
    jlong ref
) {
    return (jboolean)getRef<VorbisFileDecoder>(ref)->initialize();
}

CJNICALL(void)
Java_test_gleb_110_114_1android_cppiface_VorbisFileDecoder_nativeDeInitialize(
    JNIEnv *env,
    jclass type,
    jlong ref
) {
    getRef<VorbisFileDecoder>(ref)->deInitialize();
}

CJNICALL(jint)
Java_test_gleb_110_114_1android_cppiface_VorbisFileDecoder_nativeReadPCM(
    JNIEnv *env,
    jclass type,
    jlong ref,
    jshortArray data_,
    jint size
) {
    JNIShortArray sa{env,data_};
    return getRef<VorbisFileDecoder>(ref)->readPcm((ogg_int16_t*)sa.data(),size);
}

CJNICALL(void)
Java_test_gleb_110_114_1android_cppiface_VorbisFileDecoder_nativeSetEnergyLevelListener(
    JNIEnv *env,
    jclass type,
    jlong ref,
    jobject listener
) {
    auto listenerSh = std::make_shared<JNISoundEnergyListener>(env,listener);

    getRef<VorbisFileDecoder>(ref)->setSoundEnergyLevelListener(
            [listenerSh](long level) {
                listenerSh->onEnergyLevelCalculated(level);
            }
    );
}
