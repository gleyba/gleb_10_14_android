package test.gleb_10_14_android;

import android.arch.lifecycle.LifecycleOwner;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import test.gleb_10_14_android.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements MainContract.View {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("ogg");
        System.loadLibrary("vorbis");
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


    public boolean checkPermission() {
        int storageWPermission = ContextCompat.checkSelfPermission(
            getApplicationContext(),
            WRITE_EXTERNAL_STORAGE
        );
        int recordPermission = ContextCompat.checkSelfPermission(
            getApplicationContext(),
            RECORD_AUDIO
        );
        return storageWPermission == PackageManager.PERMISSION_GRANTED
            && recordPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static final int RequestPermissionCode = 1;
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO},
                RequestPermissionCode
            );
        }
    }

    private Runnable onPermissionsGrantedRunnable = null;
    @Override
    public void onRequestPermissionsResult(
        int requestCode,
        String permissions[],
        int[] grantResults
    ) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length == 2
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    onPermissionsGrantedRunnable.run();
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.permissions_denied),
                        Toast.LENGTH_LONG
                    ).show();

                    onPermissionsGrantedRunnable = null;
                }
                break;
        }
    }

    @Override
    public void withPermissionsChecked(Runnable r) {
        if (checkPermission()) {
            r.run();
        } else {
            onPermissionsGrantedRunnable = r;
            requestPermissions();
        }
    }

    public native String stringFromJNI();
}
