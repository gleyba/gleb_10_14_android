package test.gleb_10_14_android;

import android.arch.lifecycle.LiveData;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import test.gleb_10_14_android.cppiface.VorbisFileDecoder;

public class AudioPlayerTask extends AudioTaskBase {
    private static final String TAG = "AudioPlayerTask";

    private short[] pcmDataBuffer = new short[BufferSize];

    static final int ChannelConfiguration
        = Channels == 1
            ? AudioFormat.CHANNEL_OUT_MONO
            : AudioFormat.CHANNEL_OUT_STEREO;

    static final int BufferMinSize = AudioTrack.getMinBufferSize(
        SampleRate,
        ChannelConfiguration,
        AudioFormat.ENCODING_PCM_16BIT
    );


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
    public LiveData<Long> soundEnergy() {
        return decoder.soundEnergy();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        decoder.initialize();
        audioTrack.play();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        while(!isCancelled()) {
            int read = decoder.readPcm(pcmDataBuffer, BufferSize);
            switch (read) {
                case -1:
                    break;
                default:
                    if (read > 0) {
                        audioTrack.write(pcmDataBuffer, 0, read);
                    }
                    break;
            }
        }

        audioTrack.stop();
        audioTrack.release();
        decoder.deInitialize();
        return null;
    }
}
