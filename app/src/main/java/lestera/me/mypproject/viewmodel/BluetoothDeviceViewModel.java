package lestera.me.mypproject.viewmodel;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class BluetoothDeviceViewModel extends AndroidViewModel {
    private MutableLiveData<List<BluetoothDevice>> allDevices;

    public BluetoothDeviceViewModel(Application application) {
        super(application);
        allDevices = new MutableLiveData<>();
        allDevices.setValue(new ArrayList<>(BluetoothAdapter.getDefaultAdapter().getBondedDevices()));
    }

    public LiveData<List<BluetoothDevice>> getAllDevices() {
        return allDevices;
    }
}
