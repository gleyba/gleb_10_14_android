#include "jniplatform.hpp"
#include "platform.hpp"

#include <string>
#include <jni.h>
#include <pthread.h>
#include <cstdlib>
#include <cassert>
#include <android/log.h>

void logstr(const std::string& str) {
    __android_log_print(ANDROID_LOG_VERBOSE, "metaquotes_test", "%s\n",str.c_str());
}

static JavaVM * gCachedJVM;

CJNICALL(jint) JNI_OnLoad(JavaVM* jvm, void *) {
    gCachedJVM = jvm;
    return JNI_VERSION_1_6;
}

CJNICALL(void) Java_test_gleb_110_114_1android_cppiface_NativeRef_release(
    JNIEnv,
    jclass,
    jlong ref
) {
    delete reinterpret_cast<NativeRef*>(ref);
}

void attachThread() {
    JNIEnv *env;
    gCachedJVM->AttachCurrentThread(&env, NULL);
}

void detachThread() {
    gCachedJVM->DetachCurrentThread();
}

JNIEnv * jniGetThreadEnv() {
    assert(gCachedJVM);
    JNIEnv * env = nullptr;
    const jint get_res = gCachedJVM->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);
    if (get_res != 0 || !env) {
        // :(
        std::abort();
    }

    return env;
}

void GlobalRefDeleter::operator() (jobject globalRef) noexcept {
    if (globalRef) {
        jniGetThreadEnv()->DeleteGlobalRef(globalRef);
    }
}

jbyteArray jbyteArrayFromCharData(
    JNIEnv* env,
    const char* data,
    size_t length
) {
    jbyteArray retArray = env->NewByteArray(length);
    jbyte *temp = env->GetByteArrayElements(retArray,NULL);
    memcpy(temp, data, length);
    env->ReleaseByteArrayElements(retArray, temp, 0);
    return retArray;
}

JNIString::JNIString(
    JNIEnv* env,
    jstring jStr
)
: _env{env}
, _jStr{jStr}
, _str{env->GetStringUTFChars(jStr, 0)}
, _size{(size_t)_env->GetStringUTFLength(jStr)}
{}

JNIString::~JNIString() {
    _env->ReleaseStringUTFChars(_jStr,_str);
}

JNIByteArray::JNIByteArray(
    JNIEnv* env,
    jbyteArray jData
)
: _env{env}
, _jData{jData}
, _data{env->GetByteArrayElements(jData, nullptr)}
, _size{(size_t)_env->GetArrayLength(jData)}
{}

JNIByteArray::~JNIByteArray() {
    _env->ReleaseByteArrayElements(_jData,_data,0);
}

