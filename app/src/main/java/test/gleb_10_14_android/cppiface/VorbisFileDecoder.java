package test.gleb_10_14_android.cppiface;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

public class VorbisFileDecoder {
    private final NativeRef ref;
    private final MutableLiveData<Float> soundEnergyMutable = new MutableLiveData<>();

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

    private static native boolean nativeReadPCM(
        long ref,
        short[] data,
        PcmWriteAcceptor acceptor
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

    public boolean readPcm(
        short[] data,
        PcmWriteAcceptor acceptor
    ) {
        return nativeReadPCM(
            ref.cRef,
            data,
            acceptor
        );
    }
}
