package test.gleb_10_14_android.cppiface;

public final class VorbisFileEncoder {
    private final NativeRef ref;

    private static native long create(
        String fileName,
        int channels,
        int sampleRate,
        float quality
    );

    private static native boolean nativeInitialize(long ref);
    private static native void nativeDeInitialize(long ref);

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

    public boolean initialize() {
        return nativeInitialize(ref.cRef);
    }

    public void deInitialize() {
        nativeDeInitialize(ref.cRef);
    }
}
