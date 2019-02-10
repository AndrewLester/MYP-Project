package lestera.me.mypproject.model;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lestera.me.mypproject.BluetoothMessengerService;
import lestera.me.mypproject.R;

public class BluetoothDeviceRepository {
    private Application application;

    public BluetoothDeviceRepository(Application application) {
        this.application = application;
    }

    public void setSelectedDevice(BluetoothDevice device) {
        SharedPreferences preferences = application.getSharedPreferences(
                application.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        preferences.edit().putString(BluetoothMessengerService.PREFERENCES_DEVICE_KEY, device.getAddress()).apply();
    }

    public BluetoothDevice getSelectedDevice() {
        SharedPreferences preferences = application.getSharedPreferences(
                application.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String address = preferences.getString(BluetoothMessengerService.PREFERENCES_DEVICE_KEY, "NONE");

        Optional<BluetoothDevice> device = BluetoothAdapter.getDefaultAdapter().getBondedDevices().stream().filter(d -> d.getAddress().equals(address)).findFirst();
        return device.orElse(null);
    }

    public List<BluetoothDevice> getAllDevices() {
        return new ArrayList<>(BluetoothAdapter.getDefaultAdapter().getBondedDevices());
    }
}
