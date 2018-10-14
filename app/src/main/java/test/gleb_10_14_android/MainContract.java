package test.gleb_10_14_android;

import android.arch.lifecycle.LifecycleOwner;

public interface MainContract {

    interface View {
        LifecycleOwner getOwner();
    }

    interface ViewModel {

    }

    interface Model {

    }
}
