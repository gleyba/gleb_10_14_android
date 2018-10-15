package test.gleb_10_14_android;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableField;

import java.nio.charset.Charset;
import java.util.Random;

public class MainViewModel  extends BaseObservable implements MainContract.ViewModel {


    private Context ctx;
    private MainContract.View view = null;

    enum State { None, Playing, Recording }

    public MutableLiveData<State> state = new MutableLiveData<>();


    public ObservableField<Boolean> processing = new ObservableField<>(false);
    public ObservableField<String> startButtonCaption = new ObservableField<>();
    public ObservableField<String> soundEnergyLevel = new ObservableField<>();

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
    }

    public void startOrStopRecord() {
        view.withPermissionsChecked(() -> {
            switch (state.getValue()) {
                case None:
                    byte[] array = new byte[7]; // length is bounded by 7
                    new Random().nextBytes(array);

                    LiveData<Long> soundLevel = model.startRecord(
                        ctx.getFilesDir()
                        + "/"
                        + new String(array, Charset.forName("UTF-8"))
                        + ".ogg"
                    );

                    if (soundLevel == null) {
                        break;
                    }

                    soundLevel.observe(
                        view.getOwner(),
                        value -> {
                            soundEnergyLevel.set(String.valueOf(value));
                        }
                    );

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
