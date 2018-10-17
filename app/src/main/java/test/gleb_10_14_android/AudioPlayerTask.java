package test.gleb_10_14_android;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import test.gleb_10_14_android.cppiface.VorbisFileDecoder;

public class AudioPlayerTask extends AudioTaskBase {
    private static final String TAG = "AudioPlayerTask";

    private final MutableLiveData<Void> stoppedEvent = new MutableLiveData<>();

    static final int ChannelConfiguration
        = Channels == 1
            ? AudioFormat.CHANNEL_OUT_MONO
            : AudioFormat.CHANNEL_OUT_STEREO;

    static final int BufferMinSize = AudioTrack.getMinBufferSize(
        SampleRate,
        ChannelConfiguration,
        AudioFormat.ENCODING_PCM_16BIT
    );

    private short[] pcmDataBuffer = new short[BufferMinSize];
    private AudioTrack audioTrack = new AudioTrack(
        AudioManager.STREAM_MUSIC,
        SampleRate,
        ChannelConfiguration,
        AudioFormat.ENCODING_PCM_16BIT,
        BufferMinSize,
        AudioTrack.MODE_STREAM
    );

    private final VorbisFileDecoder decoder;

    public AudioPlayerTask(
        String dir,
        String fileName
    ) {
        this.decoder = new VorbisFileDecoder(
            dir + "/" + fileName,
            Channels,
            SampleRate
        );
    }

    @Override
    public LiveData<Float> soundEnergy() {
        return decoder.soundEnergy();
    }

    @Override
    public LiveData<Void> stopped() {
        return stoppedEvent;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        decoder.initialize();
        audioTrack.play();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        while (decoder.readPcm(
            pcmDataBuffer,
            read -> {
                if (isCancelled())
                    return false;

                audioTrack.write(
                    pcmDataBuffer,
                    0,
                    read
                );
                return true;
            }
        ));

        audioTrack.stop();
        audioTrack.release();
        decoder.deInitialize();
        stoppedEvent.postValue(null);
        return null;
    }
}
