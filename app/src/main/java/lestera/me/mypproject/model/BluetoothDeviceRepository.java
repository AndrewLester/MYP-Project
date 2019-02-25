package lestera.me.mypproject.model;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import lestera.me.mypproject.BluetoothMessengerService;
import lestera.me.mypproject.R;

public class BluetoothDeviceRepository {
    private Application application;
    private MutableLiveData<List<BluetoothDevice>> devices;
    private MutableLiveData<BluetoothDevice> selectedDevice;

    public BluetoothDeviceRepository(Application application) {
        this.application = application;
        this.devices = new MutableLiveData<>();
        this.selectedDevice = new MutableLiveData<>();
        notifyAllDevicesChanged();
    }

    public void setSelectedDevice(BluetoothDevice device) {
        selectedDevice.setValue(device);

        SharedPreferences preferences = application.getSharedPreferences(
                application.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        preferences.edit().putString(BluetoothMessengerService.PREFERENCES_DEVICE_KEY, device.getAddress()).apply();
    }

    public LiveData<BluetoothDevice> getSelectedDevice() {
        if (selectedDevice.getValue() == null) {
            SharedPreferences preferences = application.getSharedPreferences(
                    application.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

            String address = preferences.getString(BluetoothMessengerService.PREFERENCES_DEVICE_KEY, "NONE");

            Optional<BluetoothDevice> device = BluetoothAdapter.getDefaultAdapter().getBondedDevices().stream().filter(d -> d.getAddress().equals(address)).findFirst();

            selectedDevice.setValue(device.orElse(null));
        }

        return selectedDevice;
    }

    public void notifyAllDevicesChanged() {
        devices.setValue(new ArrayList<>(BluetoothAdapter.getDefaultAdapter().getBondedDevices()));
    }

    public LiveData<List<BluetoothDevice>> getAllDevices() {
        return devices;
    }
}
