package lestera.me.mypproject.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.Optional;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import lestera.me.mypproject.BluetoothMessengerService;
import lestera.me.mypproject.R;
import lestera.me.mypproject.packets.BluetoothPacket;
import lestera.me.mypproject.packets.IncomingHumidityDataPacket;
import lestera.me.mypproject.packets.OutgoingLEDPacket;

public class MainActivity extends AppCompatActivity implements BluetoothMessengerService.Reader {

    private static final int REQUEST_BLUETOOTH_ENABLE = 1;

    public static Intent mainNavigationMenuSelect(Activity from, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_main:
                return new Intent(from, MainActivity.class);
            case R.id.nav_bluetooth:
                return new Intent(from, BluetoothActivity.class);
            case R.id.nav_plants:
                return new Intent(from, PlantsActivity.class);
            default:
                return null;
        }
    }

    private Button initButton, onButton, offButton, disableButton;
    private Toolbar actionBar;
    private ProgressBar initProgress;

    private BluetoothMessengerService service;

    private DrawerLayout drawerLayout;

    private BluetoothAdapter bluetoothAdapter;
    private boolean bound = false;
    private Intent drawerIntent = null;
    private String selectedDeviceAddress = null;
    private NavigationView navigationView;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MainActivity.this.service = ((BluetoothMessengerService.BluetoothBinder) service).getService();
            MainActivity.this.service.setReader(MainActivity.this);
            bound = true;

            if (MainActivity.this.service.getSelectedDevice() == null) {
                SharedPreferences preferences = getApplication().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                if (preferences.contains(BluetoothMessengerService.PREFERENCES_DEVICE_KEY)) {
                    String deviceAddress = preferences.getString(BluetoothMessengerService.PREFERENCES_DEVICE_KEY, "none");
                    Optional<BluetoothDevice> device = bluetoothAdapter.getBondedDevices().stream().filter(d -> d.getAddress().equals(deviceAddress)).findFirst();
                    device.ifPresent(MainActivity.this.service::setSelectedDevice);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            bound = false;
            MainActivity.this.service.setReader(null);
            MainActivity.this.service = null;
        }
    };

    private BroadcastReceiver receiever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra("type", 0)) {
                case BluetoothMessengerService.MessageConstants.TO_CONNECTION_FAILURE:
                case BluetoothMessengerService.MessageConstants.TO_CONNECTION_SUCCESS:
                    initProgress.clearAnimation();
                    initProgress.setVisibility(View.INVISIBLE);
                    if (intent.hasExtra("data")) {
                        Toast.makeText(MainActivity.this,
                                getString(R.string.no_device_selected),
                                Toast.LENGTH_LONG).show();
                        break;
                    }
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this,
                                intent.getIntExtra("type", 0) == 0
                                ? getString(R.string.toast_connection_success)
                                : getString(R.string.toast_connection_failure),
                                Toast.LENGTH_LONG).show();
                    });
                    break;
                case BluetoothMessengerService.MessageConstants.TO_MESSAGE_TYPE_TOAST:
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
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);

        initProgress = findViewById(R.id.progressBar);
        initButton = findViewById(R.id.init_button);
        initButton.setOnClickListener(this::init);
        onButton = findViewById(R.id.on_button);
        onButton.setOnClickListener(this::on);
        offButton = findViewById(R.id.off_button);
        offButton.setOnClickListener(this::off);
        disableButton = findViewById(R.id.cancel_button);
        disableButton.setOnClickListener(this::disable);
        drawerLayout = findViewById(R.id.drawer_layout);

        TextView soilMoistureData = findViewById(R.id.moistureSensorData);
        Optional.ofNullable(savedInstanceState).map(state -> state.getString("savedMoistureData")).ifPresent(soilMoistureData::setText);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, actionBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if (drawerIntent != null) {
                    startActivity(drawerIntent);
                    drawerIntent = null;
                }
            }
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.setDrawerSlideAnimationEnabled(false);

        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this::navigationMenuSelect);
        navigationView.setCheckedItem(R.id.nav_main);

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
    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putString("savedMoistureData", "" + this.<TextView>findViewById(R.id.moistureSensorData).getText());
        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BLUETOOTH_ENABLE) {
            if (resultCode != RESULT_OK) {
                initProgress.clearAnimation();
                initProgress.setVisibility(View.INVISIBLE);
            }

            if (service.getSelectedDevice() != null) {
                service.connect(false);
                return;
            }

            Optional<BluetoothDevice> bluetoothDevice = bluetoothAdapter.getBondedDevices().stream().findFirst();
            if (!bluetoothDevice.isPresent())
                Toast.makeText(this, "Device not paired", Toast.LENGTH_LONG).show();

            bluetoothDevice.ifPresent(d -> service.connect(d, false));
        }
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
                Intent intent = new Intent(this, Main2Activity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public boolean navigationMenuSelect(MenuItem item) {
        drawerIntent = MainActivity.mainNavigationMenuSelect(this, item);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void init(View view) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH_ENABLE);
        initProgress.setVisibility(View.VISIBLE);
        initProgress.animate();
    }

    private void on(View view) {
        if (!bound || !service.isConnected()) return;

        service.write(new OutgoingLEDPacket(true));
    }

    private void off(View view) {
        if (!bound || !service.isConnected()) return;

        service.write(new OutgoingLEDPacket(false));
    }

    private void disable(View view) {
        if (!bound || !service.isConnected()) return;

        service.disable();
    }

    public synchronized void bluetoothRead(BluetoothPacket packet) {

        if (packet == null) {
            Log.e("BLUETOOTH", "Unknown packet received");
            return;
        }

        if (packet instanceof IncomingHumidityDataPacket) {
            short moisture = ((IncomingHumidityDataPacket) packet).getSensorData();
            TextView view = findViewById(R.id.moistureSensorData);
            runOnUiThread(() -> view.setText(String.valueOf(moisture)));
        }
    }
}