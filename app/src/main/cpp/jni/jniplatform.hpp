#pragma once

#include <jni.h>
#include <memory>
#include <type_traits>

#define CJNICALL(x) extern "C" JNIEXPORT x JNICALL
JNIEnv * jniGetThreadEnv();

struct GlobalRefDeleter {
    void operator() (jobject globalRef) noexcept;
};

template <typename PointerType>
using GlobalRefBase = std::unique_ptr<
    typename std::remove_pointer<PointerType>::type,
    GlobalRefDeleter
>;

template <typename PointerType>
class GlobalRef
: public GlobalRefBase<PointerType> {
public:
    GlobalRef() {}

    GlobalRef(GlobalRef && obj)
    : GlobalRefBase<PointerType>{std::move(obj)}
    {}

    GlobalRef(JNIEnv * env, PointerType localRef)
    : GlobalRefBase<PointerType>{
        static_cast<PointerType>(env->NewGlobalRef(localRef)),
        GlobalRefDeleter{}
    } {}
};

jbyteArray jbyteArrayFromCharData(
    JNIEnv* env,
    const char* data,
    size_t length
);

class JNIString {

    JNIEnv* _env;
    jstring _jStr;
    const char* _str;
    size_t _size;

public:

    JNIString(
        JNIEnv* env,
        jstring jStr
    );

    ~JNIString();

    const char* c_str() { return _str; }
    size_t size() { return _size; }

};

class JNIByteArray {

    JNIEnv* _env;
    jbyteArray _jData;
    jbyte* _data;
    size_t _size;

public:

    JNIByteArray(
        JNIEnv* env,
        jbyteArray jData
    );

    ~JNIByteArray();

    const jbyte * data() { return _data; }
    size_t size() { return _size; }

};