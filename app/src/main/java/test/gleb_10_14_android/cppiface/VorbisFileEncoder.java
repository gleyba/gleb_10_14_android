package test.gleb_10_14_android.cppiface;

public final class VorbisFileEncoder {
    private final NativeRef ref;

    private static native long create(String fileName);

    public VorbisFileEncoder(String fileName) {

        ref = new NativeRef(create());
    }

}
