#pragma once

#include <mutex>
#include "jniplatform.hpp"

class JNISoundEnergyListener {
    GlobalRef<jobject> _soundEnergyListener;
    jmethodID _onEnergyLevelCalculated;

public:

    JNISoundEnergyListener(
        JNIEnv* env,
        jobject listener
    );


    void onEnergyLevelCalculated(float soundEnergy);
};

