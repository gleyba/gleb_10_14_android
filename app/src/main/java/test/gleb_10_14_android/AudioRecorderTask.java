package test.gleb_10_14_android;

import android.arch.lifecycle.LiveData;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import test.gleb_10_14_android.cppiface.VorbisFileEncoder;

public class AudioRecorderTask extends AsyncTask<Void,Void,Void> {

    private static final String TAG = "AudioRecorderTask";
    private static final int SampleRate = 16000;
    private static final int Channels = 1;
    private static final int ChannelConfiguration
        = Channels == 1
            ? AudioFormat.CHANNEL_IN_MONO
            : AudioFormat.CHANNEL_IN_STEREO;

    private static final int BufferSize = AudioRecord.getMinBufferSize(
        SampleRate,
        ChannelConfiguration,
        AudioFormat.ENCODING_PCM_16BIT
    );
    private static float Quality = 1;
    private static int ReadSize = 1024;

    private byte[] pcmDataBuffer = new byte[ReadSize * 4];

    private final AudioRecord audioRecorder = new AudioRecord(
        MediaRecorder.AudioSource.MIC,
        SampleRate,
        ChannelConfiguration,
        AudioFormat.ENCODING_PCM_16BIT,
        BufferSize
    );

    private final VorbisFileEncoder encoder;

    public AudioRecorderTask(
        String fileName
    ) {
        this.encoder = new VorbisFileEncoder(
            fileName,
            Channels,
            SampleRate,
            Quality
        );
    }

    public LiveData<Long> soundEnergy() {
        return encoder.soundEnergy();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        encoder.initialize();
        audioRecorder.startRecording();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        while(!isCancelled()) {
            int read = audioRecorder.read(pcmDataBuffer, 0, ReadSize * 4);
            switch (read) {
                case AudioRecord.ERROR_INVALID_OPERATION:
                    Log.e(TAG, "Invalid operation on AudioRecord object");
                    break;
                case AudioRecord.ERROR_BAD_VALUE:
                    Log.e(TAG, "Invalid value returned from audio recorder");
                    break;
                case -1:
                    break;
                default:
                    //Successfully read from audio recorder
                    encoder.writePCM(pcmDataBuffer,read);
                    break;
            }
        }
        encoder.deInitialize();
        return null;
    }

}
