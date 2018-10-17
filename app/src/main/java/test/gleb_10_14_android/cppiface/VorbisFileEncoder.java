package test.gleb_10_14_android.cppiface;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

public final class VorbisFileEncoder {

    private final NativeRef ref;
    private final MutableLiveData<Float> soundEnergyMutable = new MutableLiveData<>();

    private static native long create(
        String fileName,
        int channels,
        int sampleRate,
        float quality
    );

    private static native boolean nativeInitialize(long ref);
    private static native void nativeDeInitialize(long ref);

    private static native void nativeSetEnergyLevelListener(
        long ref,
        SoundEnergyListener listener
    );

    private static native void nativeWritePCM(
        long ref,
        byte[] data,
        long size
    );

    public VorbisFileEncoder(
        String fileName,
        int channels,
        int sampleRate,
        float quality
    ) {
        ref = new NativeRef(create(
            fileName,
            channels,
            sampleRate,
            quality
        ));
    }

    public LiveData<Float> soundEnergy() {
        return soundEnergyMutable;
    }

    public boolean initialize() {
        if (!nativeInitialize(ref.cRef))
            return false;

        nativeSetEnergyLevelListener(
            ref.cRef,
            soundEnergyMutable::postValue
        );

        return true;
    }

    public void deInitialize() {
        nativeDeInitialize(ref.cRef);
    }

    public void writePCM(
        byte[] data,
        long size
    ) {
        nativeWritePCM(
            ref.cRef,
            data,
            size
        );
    }
}
