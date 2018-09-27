package lestera.me.mypproject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.NoSuchElementException;
import java.util.Optional;

import lestera.me.mypproject.packets.BluetoothPacket;
import lestera.me.mypproject.packets.IncomingHumidityDataPacket;
import lestera.me.mypproject.packets.OutgoingLEDPacket;

public class MainActivity extends AppCompatActivity implements BluetoothMessengerService.Reader  {

    private static final int REQUEST_BLUETOOTH_ENABLE = 1;

    Button initButton, onButton, offButton, disableButton;
    Toolbar actionBar;
    ProgressBar initProgress;

    BluetoothMessengerService service;

    private BluetoothAdapter bluetoothAdapter;
    private boolean bound = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            MainActivity.this.service = ((BluetoothMessengerService.BluetoothBinder) service).getService();
            MainActivity.this.service.setReader(MainActivity.this);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            MainActivity.this.service = null;
            bound = false;
        }
    };

    private BroadcastReceiver receiever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra("type", 0)) {
                case BluetoothMessengerService.MessageConstants.TO_CONNECTION_FAILURE:
                    findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Can't connect to device", Toast.LENGTH_LONG).show();
                    });
                    break;
                case BluetoothMessengerService.MessageConstants.TO_MESSAGE_TOAST:
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, intent.getStringExtra("data"), Toast.LENGTH_LONG).show();
                    });
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) return;

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

        Intent serviceIntent = new Intent(this, BluetoothMessengerService.class);
        startService(serviceIntent);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bound) {
            unbindService(connection);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFiler = new IntentFilter(BluetoothMessengerService.MessageConstants.INTENT_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiever, intentFiler);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiever);
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
        if (!bound || !service.isConnected()) return;

        service.write(new OutgoingLEDPacket(true));
    }

    public void off(View view) {
        if (!bound || !service.isConnected()) return;

        service.write(new OutgoingLEDPacket(false));
    }

    public void disable(View view) {
        if (!bound || !service.isConnected()) return;

        service.disable();
    }

    public void bluetoothRead(Optional<BluetoothPacket> pack) {
        BluetoothPacket packet;

        if (pack.isPresent()) {
            packet = pack.get();
        } else {
            Log.e("BLUETOOTH", "Unknown packet received");
            return;
        }

        if (packet.getType() == 0x00) {
            short moisture = ((IncomingHumidityDataPacket) packet).getSensorData();

        }
    }
}