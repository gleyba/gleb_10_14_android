package test.gleb_10_14_android;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;

public interface MainContract {

    interface View {
        LifecycleOwner getOwner();
        void withPermissionsChecked(Runnable r);
    }

    interface ViewModel {

    }

    interface Model {
        LiveData<Long> startRecord(String fileName);
        void stop();
    }
}
