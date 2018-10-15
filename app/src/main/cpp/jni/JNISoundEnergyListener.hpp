#pragma once

#include <mutex>
#include "jniplatform.hpp"

class JNISoundEnergyListener {
    static std::once_flag sMethodsOnce;
    GlobalRef<jobject> _soundEnergyListener;

    static jmethodID sOnEnergyLevelCalculated;

public:

    JNISoundEnergyListener(
        JNIEnv* env,
        jobject listener
    );


    void onEnergyLevelCalculated(long soundEnergy);
};

