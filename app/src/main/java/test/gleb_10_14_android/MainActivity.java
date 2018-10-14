package test.gleb_10_14_android;

import android.arch.lifecycle.LifecycleOwner;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import test.gleb_10_14_android.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements MainContract.View {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        MainViewModel viewModel = new MainViewModel(this,this);
        viewModel.setModel(new MainModel(this,viewModel));


        ActivityMainBinding binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        );
        binding.setViewmodel(viewModel);
    }

    @Override
    public LifecycleOwner getOwner() {
        return this;
    }

    public native String stringFromJNI();
}
