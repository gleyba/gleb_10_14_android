#include "JNISoundEnergyListener.hpp"
#include "platform.hpp"

JNISoundEnergyListener::JNISoundEnergyListener(
    JNIEnv* env,
    jobject listener
)
: _soundEnergyListener{env,listener}
{
    jclass type = env->GetObjectClass(listener);
    _onEnergyLevelCalculated = env->GetMethodID (type, "onEnergyLevelCalculated", "(F)V");
}

void JNISoundEnergyListener::onEnergyLevelCalculated(float soundEnergy) {
//    logstr("onEnergyLevelCalculated: " + std::to_string(soundEnergy));
    jfloat jSoundEnergy = soundEnergy;
    jniGetThreadEnv()->CallVoidMethod(
        _soundEnergyListener.get(),
        _onEnergyLevelCalculated,
        jSoundEnergy
    );
}