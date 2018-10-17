package test.gleb_10_14_android;

import android.arch.lifecycle.LiveData;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AudioPlayerTask extends AudioTaskBase {
    private static final String TAG = "AudioPlayerTask";

    private AudioTrack audioTrack = new AudioTrack(
        AudioManager.STREAM_MUSIC,
        SampleRate,
        ChannelConfiguration,
        AudioFormat.ENCODING_PCM_16BIT,
        BufferSize,
        AudioTrack.MODE_STREAM
    );

    public AudioPlayerTask(
        String dir,
        String fileName
    ) {

    }

    public void writePCMData(short[] pcmData, int amountToWrite) {
        if (pcmData != null && amountToWrite > 0) {
            audioTrack.write(pcmData, 0, amountToWrite);
        }
    }

    @Override
    public LiveData<Long> soundEnergy() {
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        audioTrack.play();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        audioTrack.stop();
        audioTrack.release();
        return null;
    }
}
