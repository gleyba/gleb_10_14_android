package test.gleb_10_14_android;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import test.gleb_10_14_android.utility.RandomString;

public class MainModel implements MainContract.Model {
    private static final String TAG = "MainModel";
    private Context ctx;
    private MainContract.ViewModel viewModel;

    File filesDir() {
//        File publicDir = new File(Environment.getExternalStorageDirectory() + "/oggfiles");
//        if (!publicDir.exists()) {
//            publicDir.mkdir();
//        }
//        return publicDir;
        return ctx.getFilesDir();
    }

    public MainModel(
        Context ctx,
        MainContract.ViewModel viewModel
    ) {
        this.ctx = ctx;
        this.viewModel = viewModel;
    }

    private AudioRecorderTask recorderTask = null;
    @Override
    public AudioTaskEvents startRecord() {
        recorderTask = new AudioRecorderTask(
            filesDir().toString(),
            new RandomString(10).nextString() + ".ogg"
        );
        recorderTask.execute();
        return recorderTask;
    }

    private AudioPlayerTask playerTask = null;
    @Override
    public AudioTaskEvents startPlaying(String fileName) {
        playerTask = new AudioPlayerTask(
            filesDir().toString(),
            fileName
        );
        playerTask.execute();
        return playerTask;
    }

    MutableLiveData<String> onNewOggFile = new MutableLiveData<>();

    @Override
    public LiveData<String> newOggFile() {
        return onNewOggFile;
    }

    @Override
    public void stop() {
        if (recorderTask != null) {
            recorderTask.cancel(false);
            onNewOggFile.postValue(recorderTask.getFileName());
            recorderTask = null;
        }
        if (playerTask != null) {
            playerTask.cancel(false);
            playerTask = null;
        }
    }

    @Override
    public ArrayList<String> getAllOggFiles() {
        ArrayList<String> result = new ArrayList<>();

        for(File file : filesDir().listFiles()) {
            if (file.getName().endsWith((".ogg"))) {
                result.add(file.getName());

                Log.v(TAG,"Adding: " + file.getName() + " size: " + file.length());
            }
        }

        return result;
    }

    @Override
    public void removeFile(String fileName) {
        new File(filesDir() + "/" + fileName).delete();
    }

    @Override
    public void removeAllFiles() {
        for(File file : filesDir().listFiles()) {
            if (file.getName().endsWith((".ogg"))) {
                file.delete();
            }
        }
    }
}