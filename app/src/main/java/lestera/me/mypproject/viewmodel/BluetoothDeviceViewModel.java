package lestera.me.mypproject.viewmodel;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import lestera.me.mypproject.BluetoothMessengerService;
import lestera.me.mypproject.R;
import lestera.me.mypproject.model.BluetoothDeviceRepository;

public class BluetoothDeviceViewModel extends AndroidViewModel {
    private BluetoothDeviceRepository repository;
    private MutableLiveData<List<BluetoothDevice>> allDevices;
    private MutableLiveData<BluetoothDevice> selectedDevice;

    public BluetoothDeviceViewModel(Application application) {
        super(application);
        repository = new BluetoothDeviceRepository(application);
        allDevices = new MutableLiveData<>();
        selectedDevice = new MutableLiveData<>();
        allDevices.setValue(repository.getAllDevices());
        selectedDevice.setValue(repository.getSelectedDevice());
    }

    public LiveData<BluetoothDevice> getSelectedDevice() {
        return selectedDevice;
    }

    public void setSelectedDevice(BluetoothDevice device) {
        repository.setSelectedDevice(device);
        selectedDevice.setValue(device);
    }

    public LiveData<List<BluetoothDevice>> getAllDevices() {
        return allDevices;
    }
}
