#include "JNISoundEnergyListener.hpp"
#include "platform.hpp"

std::once_flag JNISoundEnergyListener::sMethodsOnce;

jmethodID JNISoundEnergyListener::sOnEnergyLevelCalculated;

JNISoundEnergyListener::JNISoundEnergyListener(
    JNIEnv* env,
    jobject listener
)
: _soundEnergyListener{env,listener}
{
    std::call_once(
        sMethodsOnce,
        [env,listener]{
            jclass type = env->GetObjectClass(listener);
            sOnEnergyLevelCalculated = env->GetMethodID (type, "onEnergyLevelCalculated", "(J)V");
        }
    );
}

void JNISoundEnergyListener::onEnergyLevelCalculated(long soundEnergy) {
//    logstr("onEnergyLevelCalculated: " + std::to_string(soundEnergy));
    jlong jSoundEnergy = soundEnergy;
    jniGetThreadEnv()->CallVoidMethod(
        _soundEnergyListener.get(),
        sOnEnergyLevelCalculated,
        jSoundEnergy
    );
}