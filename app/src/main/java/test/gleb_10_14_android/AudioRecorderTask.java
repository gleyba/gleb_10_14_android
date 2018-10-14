package test.gleb_10_14_android;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;

import test.gleb_10_14_android.cppiface.VorbisFileEncoder;

public class AudioRecorderTask extends AsyncTask<Void,Void,Void> {

    private static int SampleRate = 44100;
    private static int Channels = 2;
    private static int BufferSize = AudioRecord.getMinBufferSize(
        SampleRate,
        Channels,
        AudioFormat.ENCODING_PCM_16BIT
    );
    private static int ReadSize = 1024;

    private final AudioRecord audioRecorder = new AudioRecord(
        MediaRecorder.AudioSource.MIC,
        SampleRate,
        Channels,
        AudioFormat.ENCODING_PCM_16BIT,
        BufferSize
    );

    private final VorbisFileEncoder encoder;

    public AudioRecorderTask(
        String fileName
    ) {
        this.encoder = new VorbisFileEncoder(fileName);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        audioRecorder.startRecording();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

    }
}
