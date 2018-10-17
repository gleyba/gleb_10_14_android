package test.gleb_10_14_android;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableField;

public class MainViewModel
extends BaseObservable
implements MainContract.ViewModel {

    private Context ctx;
    private MainContract.View view = null;

    enum State { None, Playing, Recording }

    public MutableLiveData<State> state = new MutableLiveData<>();


    public ObservableField<Boolean> notProcessing = new ObservableField<>(true);
    public ObservableField<String> startButtonCaption = new ObservableField<>();

    public MainViewModel(
        Context ctx,
        MainContract.View view
    ) {
        this.ctx = ctx;
        this.view = view;

        state.observe(
            view.getOwner(),
            value -> {
                switch (value) {
                    case None:
                        startButtonCaption.set(ctx.getString(R.string.start_record));
                        notProcessing.set(true);
                        break;
                    case Playing:
                        startButtonCaption.set(ctx.getString(R.string.stop_playing));
                        notProcessing.set(false);
                        break;
                    case Recording:
                        startButtonCaption.set(ctx.getString(R.string.stop_record));
                        notProcessing.set(false);
                        break;
                }
            }
        );
        state.postValue(State.None);

    }

    private MainContract.Model model = null;
    public void setModel( MainContract.Model model) {
        this.model = model;
        view.adapter().addItems(model.getAllOggFiles());
        model.newOggFile().observe(
            view.getOwner(),
            view.adapter()::addItem
        );

        view.onFlush().observe(
            view.getOwner(),
            x -> model.removeAllFiles()
        );
        view.onRemove().observe(
            view.getOwner(),
            model::removeFile
        );
        view.onPlay().observe(
            view.getOwner(),
            fileName -> {
                view.start(model.startPlaying(fileName));
                state.postValue(State.Playing);
            }
        );
    }

    public void startOrStopRecord() {
        view.withPermissionsChecked(() -> {
            switch (state.getValue()) {
                case None:
                    view.start(model.startRecord());
                    state.postValue(State.Recording);
                    break;
                case Playing:
                    model.stop();
                    state.postValue(State.None);
                    break;
                case Recording:
                    model.stop();
                    state.postValue(State.None);
                    break;
            }
        });
    }

}
