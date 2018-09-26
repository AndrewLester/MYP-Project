package lestera.me.mypproject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Optional;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH_ENABLE = 1;

    Button initButton, onButton, offButton, disableButton;

    Toolbar actionBar;

    BluetoothAdapter bluetoothAdapter;
    BluetoothMessenger service;
    ProgressBar initProgress;

    /*
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

        }
    };*/ //Used to communicate with other threads through a message posting system.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) return;

        service = new BluetoothMessenger(this, null /* Previously for the handler */);

        actionBar = findViewById(R.id.toolbar);
        actionBar.setTitle(""); // The action bar needs a default value set before being used.
        actionBar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(actionBar);
        getSupportActionBar().setTitle(R.string.app_name);

        initProgress = findViewById(R.id.progressBar);
        initButton = findViewById(R.id.init_button);
        onButton = findViewById(R.id.on_button);
        offButton = findViewById(R.id.off_button);
        disableButton = findViewById(R.id.cancel_button);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (service != null) {
            service.disable();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BLUETOOTH_ENABLE && resultCode == RESULT_OK) {
            Optional<BluetoothDevice> bluetoothDevice = bluetoothAdapter.getBondedDevices().stream().filter((device) -> device.getName().equals("Watering-Can")).findFirst();
            if (!bluetoothDevice.isPresent())
                Toast.makeText(this, "Device not paired", Toast.LENGTH_LONG).show();

            bluetoothDevice.ifPresent(d -> service.connect(d, false));
        }

        initProgress.clearAnimation();
        initProgress.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }



    }

    public void init(View view) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH_ENABLE);
        initProgress.setVisibility(View.VISIBLE);
        initProgress.animate();
    }

    public void on(View view) {
        service.write(new byte[] {0x00, 0x01, 0x01});
    }

    public void off(View view) {
        service.write(new byte[] {0x00, 0x01, 0x00});
    }

    public void disable(View view) {
        service.disable();
    }
}