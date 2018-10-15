#include "jniplatform.hpp"
#include "VorbisFileEncoder.hpp"
#include "JNISoundEnergyListener.hpp"

CJNICALL(jlong)
Java_test_gleb_110_114_1android_cppiface_VorbisFileEncoder_create(
    JNIEnv *env,
    jclass type,
    jstring fileName_,
    jint channels_,
    jint sampleRate_,
    jfloat quality_
) {
    JNIString fileName{env,fileName_};
    return reinterpret_cast<jlong>(new NativeRefHolder<VorbisFileEncoder>{
        std::string{fileName.c_str(),fileName.size()},
        channels_,
        sampleRate_,
        quality_
    });
}

CJNICALL(jboolean)
Java_test_gleb_110_114_1android_cppiface_VorbisFileEncoder_nativeInitialize(
    JNIEnv *env,
    jclass type,
    jlong ref
) {
    return (jboolean)getRef<VorbisFileEncoder>(ref)->initialize();
}

CJNICALL(void)
Java_test_gleb_110_114_1android_cppiface_VorbisFileEncoder_nativeDeIitialize(
    JNIEnv *env,
    jclass type,
    jlong ref
) {
    getRef<VorbisFileEncoder>(ref)->deInitialize();
}

CJNICALL(void)
Java_test_gleb_110_114_1android_cppiface_VorbisFileEncoder_nativeWritePCM(
    JNIEnv *env,
    jclass type,
    jlong ref,
    jbyteArray data_,
    jlong size
) {
    JNIByteArray ba{env,data_};
    getRef<VorbisFileEncoder>(ref)->writePcm((const char*)ba.data(),size);
}

CJNICALL(void)
Java_test_gleb_110_114_1android_cppiface_VorbisFileEncoder_nativeSetEnergyLevelListener(
    JNIEnv *env,
    jclass type,
    jlong ref,
    jobject listener
) {
    getRef<VorbisFileEncoder>(ref)->setSoundEnergyLevelListener(
        [listener = std::make_shared<JNISoundEnergyListener>(env,listener)](long level) mutable {
            listener->onEnergyLevelCalculated(level);
        }
    );
}