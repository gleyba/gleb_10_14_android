package test.gleb_10_14_android;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableField;

import java.nio.charset.Charset;

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
                        break;
                    case Playing:
                        startButtonCaption.set(ctx.getString(R.string.stop_playing));
                        break;
                    case Recording:
                        startButtonCaption.set(ctx.getString(R.string.stop_record));
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
    }

    public void startOrStopRecord() {
        view.withPermissionsChecked(() -> {
            switch (state.getValue()) {
                case None:

                    LiveData<Long> soundLevel = model.startRecord();

                    if (soundLevel == null) {
                        break;
                    }

                    view.start(soundLevel);
                    state.postValue(State.Recording);
                    notProcessing.set(false);
                    break;
                case Playing:
                    model.stop();
                    view.stop();
                    state.postValue(State.None);
                    notProcessing.set(true);
                    break;
                case Recording:
                    model.stop();
                    view.stop();
                    state.postValue(State.None);
                    notProcessing.set(true);
                    break;
            }
        });
    }

    @Override
    public LifecycleOwner getOwner() {
        return view.getOwner();
    }

    @Override
    public LiveData<Void> onFlush() {
        return view.onFlush();
    }
}
