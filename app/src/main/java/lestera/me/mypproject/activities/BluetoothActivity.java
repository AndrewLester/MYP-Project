package lestera.me.mypproject.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import lestera.me.mypproject.BluetoothMessengerService;
import lestera.me.mypproject.R;
import lestera.me.mypproject.fragments.BluetoothItemListFragment;
import lestera.me.mypproject.fragments.NoConnectionFragment;
import lestera.me.mypproject.packets.BluetoothPacket;
import lestera.me.mypproject.viewmodel.BluetoothDeviceViewModel;

public class BluetoothActivity extends AppCompatActivity implements
        BluetoothMessengerService.Reader,
        NoConnectionFragment.NoConnectionClickListener {

    private DrawerLayout drawerLayout;
    private FragmentManager fragmentManager;
    private Intent drawerIntent;
    private NavigationView navigationView;
    private BluetoothMessengerService service;
    private boolean bound;
    private TextView deviceIndicator;
    private BluetoothDeviceViewModel deviceViewModel;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothActivity.this.service = ((BluetoothMessengerService.BluetoothBinder) service).getService();
            BluetoothActivity.this.service.setReader(BluetoothActivity.this);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            bound = false;
            BluetoothActivity.this.service.setReader(null);
            BluetoothActivity.this.service = null;
        }
    };

    private BroadcastReceiver receiever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra("type", 0)) {
                case BluetoothMessengerService.MessageConstants.TO_MESSAGE_TYPE_TOAST:
                    runOnUiThread(() -> {
                        Toast.makeText(BluetoothActivity.this, intent.getStringExtra("data"), Toast.LENGTH_LONG).show();
                    });
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Bluetooth Devices");

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if (drawerIntent != null) {
                    startActivity(drawerIntent);
                    drawerIntent = null;
                }
            }
        };
        toggle.setDrawerSlideAnimationEnabled(false);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this::navigationMenuSelect);
        navigationView.setCheckedItem(R.id.nav_bluetooth);

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (savedInstanceState == null) { // Only add fragments if loading Activity for the first time.
            if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                fragmentTransaction.add(R.id.bluetooth_constraint_layout, new BluetoothItemListFragment());
            } else {
                fragmentTransaction.add(R.id.bluetooth_constraint_layout, new NoConnectionFragment());
            }
            fragmentTransaction.commit();
        }

        deviceViewModel = ViewModelProviders.of(this).get(BluetoothDeviceViewModel.class);
        deviceIndicator = navigationView.getHeaderView(0).findViewById(R.id.device_indicator_text);
        deviceViewModel.getSelectedDevice().observe(this, d -> deviceIndicator.setText(
                d != null ? "Device: " + d.getName() : getString(R.string.nav_header_subtitle)));

        Intent serviceIntent = new Intent(this, BluetoothMessengerService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bluetooth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean navigationMenuSelect(MenuItem item) {
        drawerIntent = MainActivity.mainNavigationMenuSelect(this, item);
        navigationView.setCheckedItem(item);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NoConnectionFragment.REQUEST_BLUETOOTH_ENABLE) {
            if (resultCode == RESULT_OK) {
                onRetrySuccess();
            }
        }
    }

    public void onRetrySuccess() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bluetooth_constraint_layout, new BluetoothItemListFragment())
                .commit();
    }

    @Override
    public void bluetoothRead(BluetoothPacket packet) { }
}
