package test.gleb_10_14_android.cppiface;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

public class VorbisFileDecoder {
    private final NativeRef ref;
    private final MutableLiveData<Long> soundEnergyMutable = new MutableLiveData<>();

    private static native long create(
        String fileName,
        int channels,
        int sampleRate
    );

    private static native boolean nativeInitialize(long ref);
    private static native void nativeDeInitialize(long ref);

    private static native void nativeSetEnergyLevelListener(
        long ref,
        SoundEnergyListener listener
    );

    private static native int nativeReadPCM(
        long ref,
        short[] data,
        int size
    );

    public VorbisFileDecoder(
        String fileName,
        int channels,
        int sampleRate
    ) {
        ref = new NativeRef(create(
            fileName,
            channels,
            sampleRate
        ));
    }

    public LiveData<Long> soundEnergy() {
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

    public int readPcm(
        short[] data,
        int size
    ) {
        return nativeReadPCM(
            ref.cRef,
            data,
            size
        );
    }
}
