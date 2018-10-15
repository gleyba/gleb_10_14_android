package test.gleb_10_14_android;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;

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

    AudioRecorderTask recorderTask = null;
    @Override
    public LiveData<Long> startRecord() {
        recorderTask = new AudioRecorderTask(
            ctx.getFilesDir().toString(),
            new RandomString(10).nextString() + ".ogg"
        );
        recorderTask.execute();
        return recorderTask.soundEnergy();
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
}