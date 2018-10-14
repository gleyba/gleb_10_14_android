package test.gleb_10_14_android.cppiface;

public class NativeRef {

    private static native void release(long ref);

    public final long cRef;

    NativeRef(long ref) {
        cRef = ref;
    }

    @Override
    public void finalize() {
        release(cRef);
    }

}
