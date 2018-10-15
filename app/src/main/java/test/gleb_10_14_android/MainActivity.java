package test.gleb_10_14_android;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import test.gleb_10_14_android.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements MainContract.View {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("ogg");
        System.loadLibrary("vorbis");
        System.loadLibrary("native-lib");
    }

    private ActivityMainBinding binding;
    private MainViewRowsAdapter rowsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        MainViewModel viewModel = new MainViewModel(this,this);

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        );
        rowsAdapter = new MainViewRowsAdapter();
        binding.recycler.setAdapter(rowsAdapter);
        binding.setViewmodel(viewModel);

        viewModel.setModel(new MainModel(this,viewModel));

        int graphArray[] = new int[256];
        for(int i = 0; i < graphArray.length; ++i) {
            graphArray[i] = i % 50;
        }

    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    @Override
    public void start(LiveData<Long> soundEnergy) {
        binding.soundEnergy.setText("");
        binding.graph.flush();
        binding.graph.invalidate();
        soundEnergy.observe(
            this,
            value -> {
                binding.graph.addValue(safeLongToInt(value));
                binding.graph.invalidate();
                binding.soundEnergy.setText(
                    String.format(
                        getString(R.string.sound_energy_lvl),
                        String.valueOf(value)
                    )
                );
            }
        );
    }

    @Override
    public void stop() {
    }

    @Override
    public LifecycleOwner getOwner() {
        return this;
    }

    @Override
    public Adapter adapter() {
        return rowsAdapter;
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

}
