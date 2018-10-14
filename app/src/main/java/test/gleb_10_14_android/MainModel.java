package test.gleb_10_14_android;

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
}