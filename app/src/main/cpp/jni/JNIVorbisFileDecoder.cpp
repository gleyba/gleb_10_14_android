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

CJNICALL(jboolean)
Java_test_gleb_110_114_1android_cppiface_VorbisFileDecoder_nativeReadPCM(
    JNIEnv *env,
    jclass type,
    jlong ref,
    jshortArray data_,
    jobject acceptor
) {
    JNIShortArray sa{env,data_};

    static std::once_flag sMethodsOnce;
    static jmethodID sAccept;
    std::call_once(
        sMethodsOnce,
        [env,acceptor]{
            jclass type = env->GetObjectClass(acceptor);
            sAccept = env->GetMethodID(type, "accept", "(I)Z");
        }
    );

    return getRef<VorbisFileDecoder>(ref)->readPcm(
        (ogg_int16_t*)sa.data(),
        [env,acceptor,data_,&sa](int size) -> bool {
            env->SetShortArrayRegion(data_,0,size,sa.data());
            return (bool)env->CallBooleanMethod(acceptor,sAccept,(jint)size);
        }
    );
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
        [listenerSh](float level) {
            listenerSh->onEnergyLevelCalculated(level);
        }
    );
}
