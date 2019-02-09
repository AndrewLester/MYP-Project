package lestera.me.mypproject.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import lestera.me.mypproject.BluetoothMessengerService;
import lestera.me.mypproject.R;
import lestera.me.mypproject.fragments.CardListFragment;
import lestera.me.mypproject.fragments.ListFragmentInteractionListener;
import lestera.me.mypproject.fragments.NoConnectionFragment;
import lestera.me.mypproject.model.Plant;
import lestera.me.mypproject.packets.BluetoothPacket;
import lestera.me.mypproject.packets.IncomingPlantDataPacket;
import lestera.me.mypproject.packets.IncomingPlantNumberPacket;

public class PlantsActivity extends AppCompatActivity implements
        ListFragmentInteractionListener<Plant>,
        BluetoothMessengerService.Reader {

    private static final String FRAGMENT_TAG = "card_list_fragment";

    private DrawerLayout drawerLayout;
    private Toolbar actionBar;
    private Intent drawerIntent;
    private NavigationView navigationView;
    private FragmentManager fragmentManager;
    private BluetoothMessengerService service;
    private CardListFragment cardListFragment;
    private boolean bound = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            PlantsActivity.this.service = ((BluetoothMessengerService.BluetoothBinder) service).getService();
            PlantsActivity.this.service.setReader(PlantsActivity.this);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            bound = false;
            PlantsActivity.this.service.setReader(null);
            PlantsActivity.this.service = null;
        }
    };

    private BroadcastReceiver receiever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra("type", 0)) {
                case BluetoothMessengerService.MessageConstants.TO_MESSAGE_TYPE_TOAST:
                    runOnUiThread(() -> {
                        Toast.makeText(PlantsActivity.this, intent.getStringExtra("data"), Toast.LENGTH_LONG).show();
                    });
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plants);

        actionBar = findViewById(R.id.activity_plants_toolbar);
        actionBar.setTitle("");
        setSupportActionBar(actionBar);
        getSupportActionBar().setTitle("Plants");

        drawerLayout = findViewById(R.id.activity_plants_drawer_layout);
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
        toggle.setDrawerSlideAnimationEnabled(false);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.activity_plants_nav_view);
        navigationView.setNavigationItemSelectedListener(this::navigationMenuSelect);
        navigationView.setCheckedItem(R.id.nav_plants);

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (savedInstanceState == null) { // Only add fragments if loading Activity for the first time.
            if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                fragmentTransaction.add(R.id.plants_constraint_layout, new CardListFragment(), FRAGMENT_TAG);
            } else {
                fragmentTransaction.add(R.id.plants_constraint_layout, new NoConnectionFragment());
            }
            fragmentTransaction.commit();
        }

        Intent serviceIntent = new Intent(this, BluetoothMessengerService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
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
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.plants_constraint_layout, new CardListFragment(), FRAGMENT_TAG);
                fragmentTransaction.commit();
            }
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
    public void bluetoothRead(BluetoothPacket packet) {
        CardListFragment fragment = (CardListFragment) fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if (packet instanceof IncomingPlantNumberPacket) {
            IncomingPlantNumberPacket plantNumberPacket = (IncomingPlantNumberPacket) packet;


            //fragment.updatePlantNumber(plantNumberPacket.getPlantNumber());
        } else if (packet instanceof IncomingPlantDataPacket) {
            IncomingPlantDataPacket plantDataPacket = (IncomingPlantDataPacket) packet;
        }
    }

    @Override
    public void onInteraction(Plant element, int position) {
        //TODO: OPEN PLANT SPECIFIC VIEW
    }
}
