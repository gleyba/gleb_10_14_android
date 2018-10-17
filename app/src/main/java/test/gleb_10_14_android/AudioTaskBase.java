package test.gleb_10_14_android;

import android.arch.lifecycle.LiveData;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.AsyncTask;

abstract class AudioTaskBase  extends AsyncTask<Void,Void,Void> {
    static final int SampleRate = 16000;
    static final int Channels = 1;

    static int BufferSize = 1024 * 4;

    public abstract LiveData<Long> soundEnergy();
}
