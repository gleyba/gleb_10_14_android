package test.gleb_10_14_android;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Canvas;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    private SwipeController swipeController;

    private MenuItem reloadItem;

    private final MutableLiveData<Void> flushEvent = new MutableLiveData<>();
    @Override
    public LiveData<Void> onFlush() {
        return flushEvent;
    }
    private final MutableLiveData<String> removeFileEvent = new MutableLiveData<>();
    @Override
    public LiveData<String> onRemove() {
        return removeFileEvent;
    }
    private final MutableLiveData<String> playFileEvent = new MutableLiveData<>();
    @Override
    public LiveData<String> onPlay() {
        return playFileEvent;
    }

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

        swipeController = new SwipeController(this,new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                removeFileEvent.postValue(rowsAdapter.itemData(position));
                rowsAdapter.deleteItem(position);
            }

            @Override
            public void onLeftClicked(int position) {
                playFileEvent.postValue(rowsAdapter.itemData(position));
            }
        });

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(binding.recycler);

        binding.recycler.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });

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
        binding.soundEnergy.setText(null);
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

        reloadItem.setVisible(false);
        binding.startStopBtn.setBackgroundResource(R.drawable.stop);
    }

    @Override
    public void stop() {
        reloadItem.setVisible(true);
        binding.startStopBtn.setBackgroundResource(R.drawable.record);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        reloadItem = menu.findItem(R.id.reload);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reload:
                rowsAdapter.flush();
                flushEvent.postValue(null);
                return true;

            default:
                return false;

        }
    }



}
