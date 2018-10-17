package test.gleb_10_14_android;

import android.arch.lifecycle.LiveData;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import test.gleb_10_14_android.cppiface.VorbisFileEncoder;

public class AudioRecorderTask extends AudioTaskBase {

    private static final String TAG = "AudioRecorderTask";

    private static float Quality = 1;;

    private byte[] pcmDataBuffer = new byte[BufferSize];

    private final AudioRecord audioRecorder = new AudioRecord(
        MediaRecorder.AudioSource.MIC,
        SampleRate,
        ChannelConfiguration,
        AudioFormat.ENCODING_PCM_16BIT,
        BufferMinSize
    );

    private final String fileName;
    private final VorbisFileEncoder encoder;

    public AudioRecorderTask(
        String dir,
        String fileName
    ) {
        this.fileName = fileName;
        this.encoder = new VorbisFileEncoder(
            dir + "/" + fileName,
            Channels,
            SampleRate,
            Quality
        );
    }

    public String getFileName() {
        return fileName;
    }

    @Override
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
            int read = audioRecorder.read(pcmDataBuffer, 0, BufferSize);
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
        audioRecorder.stop();
        encoder.deInitialize();
        return null;
    }

}
