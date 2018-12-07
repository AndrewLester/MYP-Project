package lestera.me.mypproject.activities;

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
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Optional;

import lestera.me.mypproject.BluetoothMessengerService;
import lestera.me.mypproject.R;
import lestera.me.mypproject.packets.BluetoothPacket;
import lestera.me.mypproject.packets.IncomingHumidityDataPacket;
import lestera.me.mypproject.packets.OutgoingLEDPacket;

public class MainActivity extends AppCompatActivity implements BluetoothMessengerService.Reader {

    private static final int REQUEST_BLUETOOTH_ENABLE = 1;

    Button initButton, onButton, offButton, disableButton;
    Toolbar actionBar;
    ProgressBar initProgress;

    BluetoothMessengerService service;

    DrawerLayout drawerLayout;

    private BluetoothAdapter bluetoothAdapter;
    private boolean bound = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MainActivity.this.service = ((BluetoothMessengerService.BluetoothBinder) service).getService();
            MainActivity.this.service.setReader(MainActivity.this);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            bound = false;
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
                this, drawerLayout, actionBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.setDrawerSlideAnimationEnabled(false);

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this::navigationMenuSelect);

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

            Optional<BluetoothDevice> bluetoothDevice = bluetoothAdapter.getBondedDevices().stream().filter((device) -> device.getName().equals("Watering-Can")).findFirst();
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public boolean navigationMenuSelect(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_item:
                break;
            case R.id.nav_bluetooth:
                Intent intent = new Intent(this, BluetoothActivity.class);
                startActivity(intent);

                break;
        }

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