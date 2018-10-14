package test.gleb_10_14_android;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableField;

public class MainViewModel  extends BaseObservable implements MainContract.ViewModel {


    private Context ctx;
    private MainContract.View view = null;
    public MutableLiveData<Boolean> recording = new MutableLiveData<>();
    public MutableLiveData<Boolean> playing = new MutableLiveData<>();
    public ObservableField<String> startButtonCaption = new ObservableField<>();

    public MainViewModel(
        Context ctx,
        MainContract.View view
    ) {
        this.ctx = ctx;
        this.view = view;

//        recording.setValue(false);
//        recording.setValue(false);

        recording.observe(
            view.getOwner(),
            value -> {
                if (value) {
                    startButtonCaption.set(ctx.getString(R.string.start_record));
                } else {
                    startButtonCaption.set(ctx.getString(R.string.stop_record));
                }
            }
        );
        recording.postValue(false);
    }

    private MainContract.Model model = null;
    public void setModel( MainContract.Model model) {
        this.model = model;
    }

    public void startOrStopRecord() {
        recording.postValue(!recording.getValue());
    }
}
