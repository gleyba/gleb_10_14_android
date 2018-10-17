package test.gleb_10_14_android;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import java.io.File;
import java.util.ArrayList;

import test.gleb_10_14_android.utility.RandomString;

public class MainModel implements MainContract.Model {

    private Context ctx;
    private MainContract.ViewModel viewModel;

    public MainModel(
        Context ctx,
        MainContract.ViewModel viewModel
    ) {
        this.ctx = ctx;
        this.viewModel = viewModel;
    }

    private AudioRecorderTask recorderTask = null;
    @Override
    public LiveData<Long> startRecord() {
        recorderTask = new AudioRecorderTask(
            ctx.getFilesDir().toString(),
            new RandomString(10).nextString() + ".ogg"
        );
        recorderTask.execute();
        return recorderTask.soundEnergy();
    }

    private AudioPlayerTask playerTask = null;
    @Override
    public LiveData<Long> startPlaying(String fileName) {
        playerTask = new AudioPlayerTask(
            ctx.getFilesDir().toString(),
            fileName
        );
        playerTask.execute();
        return playerTask.soundEnergy();
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

        for(File file : ctx.getFilesDir().listFiles()) {
            if (file.getName().endsWith((".ogg"))) {
                result.add(file.getName());
            }
        }

        return result;
    }

    @Override
    public void removeFile(String fileName) {
        new File(ctx.getFilesDir() + "/" + fileName).delete();
    }

    @Override
    public void removeAllFiles() {
        for(File file : ctx.getFilesDir().listFiles()) {
            if (file.getName().endsWith((".ogg"))) {
                file.delete();
            }
        }
    }
}