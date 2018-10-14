package test.gleb_10_14_android;

import android.content.Context;
import android.databinding.BaseObservable;

public class MainViewModel  extends BaseObservable implements MainContract.ViewModel {


    private Context ctx;
    private MainContract.View view;

    public MainViewModel(
        Context ctx,
        MainContract.View view
    ) {
        this.ctx = ctx;
        this.view = view;
    }
}
