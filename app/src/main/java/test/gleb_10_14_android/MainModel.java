package test.gleb_10_14_android;

import android.arch.lifecycle.LiveData;
import android.content.Context;

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
    public LiveData<Long> startRecord(String fileName) {
        recorderTask = new AudioRecorderTask(fileName);
        recorderTask.execute();
        return recorderTask.soundEnergy();
    }


    @Override
    public void stop() {
        if (recorderTask != null) {
            recorderTask.cancel(false);
        }
    }
}