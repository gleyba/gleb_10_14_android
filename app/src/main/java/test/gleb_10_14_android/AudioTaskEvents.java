package test.gleb_10_14_android;

import android.arch.lifecycle.LiveData;

public interface AudioTaskEvents {
    LiveData<Float> soundEnergy();
    LiveData<Void> stopped();
}
