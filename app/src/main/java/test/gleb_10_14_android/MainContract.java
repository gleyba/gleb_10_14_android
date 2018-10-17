package test.gleb_10_14_android;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;

import java.util.ArrayList;

public interface MainContract {

    interface View {
        LifecycleOwner getOwner();
        void withPermissionsChecked(Runnable r);
        void start(LiveData<Long> soundEnergy);
        void stop();

        interface Adapter {

            void addItems(ArrayList<String> data);
            void addItem(String data);
        }

        Adapter adapter();

        LiveData<Void> onFlush();

        LiveData<String> onRemove();
        LiveData<String> onPlay();
    }

    interface ViewModel {
    }

    interface Model {
        ArrayList<String> getAllOggFiles();
        LiveData<String> newOggFile();
        LiveData<Long> startRecord();
        LiveData<Long> startPlaying(String fileName);
        void stop();
        void removeFile(String fileName);
        void removeAllFiles();
    }
}
