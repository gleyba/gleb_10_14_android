package test.gleb_10_14_android;

import android.arch.lifecycle.LiveData;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.AsyncTask;

abstract class AudioTaskBase  extends AsyncTask<Void,Void,Void> {
    static final int SampleRate = 16000;
    static final int Channels = 1;
    static final int ChannelConfiguration
            = Channels == 1
            ? AudioFormat.CHANNEL_IN_MONO
            : AudioFormat.CHANNEL_IN_STEREO;

    static int BufferSize = 1024 * 4;

    static final int BufferMinSize = AudioRecord.getMinBufferSize(
        SampleRate,
        ChannelConfiguration,
        AudioFormat.ENCODING_PCM_16BIT
    );

    public abstract LiveData<Long> soundEnergy();
}
