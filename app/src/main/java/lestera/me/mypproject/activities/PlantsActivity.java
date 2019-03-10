package lestera.me.mypproject.activities;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import lestera.me.mypproject.BluetoothMessengerService;
import lestera.me.mypproject.R;
import lestera.me.mypproject.fragments.CardListFragment;
import lestera.me.mypproject.fragments.NoConnectionFragment;
import lestera.me.mypproject.fragments.PlantFragment;
import lestera.me.mypproject.model.Plant;
import lestera.me.mypproject.packets.BluetoothPacket;
import lestera.me.mypproject.packets.IncomingPlantDataPacket;
import lestera.me.mypproject.packets.IncomingPlantNumberPacket;
import lestera.me.mypproject.viewmodel.BluetoothDeviceViewModel;
import lestera.me.mypproject.viewmodel.PlantViewModel;

public class PlantsActivity extends AppCompatActivity implements
        BluetoothMessengerService.Reader,
        CardListFragment.PlantFragmentUpdateListener,
        NoConnectionFragment.NoConnectionClickListener {

    private static final String FRAGMENT_TAG = "card_list_fragment";
    private static final String PLANT_FRAGMENT_TAG = "plant_fragment";
    private static final String BLUETOOTH_FRAGMENT_TAG = "bluetooth_fragment";

    private DrawerLayout drawerLayout;
    private Toolbar actionBar;
    private Intent drawerIntent;
    private NavigationView navigationView;
    private LinearLayout addPlantButton;
    private LinearLayout linearLayout;
    private NestedScrollView scrollView;
    private DrawerArrowDrawable drawerArrowDrawable;
    private AppBarLayout appBarLayout;

    private FragmentManager fragmentManager;
    private BluetoothMessengerService service;
    private CardListFragment cardListFragment;
    private NoConnectionFragment noConnectionFragment;
    private ActionBarDrawerToggle toggle;
    private BluetoothDeviceViewModel deviceViewModel;
    private View.OnClickListener navigationIconClickListener;
    private PlantViewModel plantViewModel;
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

        scrollView = findViewById(R.id.plants_scroll_view);
        actionBar = findViewById(R.id.activity_plants_toolbar);
        appBarLayout = findViewById(R.id.plants_appbar);
        actionBar.setTitle("Plants");
        setSupportActionBar(actionBar);
        getSupportActionBar().setTitle("Plants");
        drawerLayout = findViewById(R.id.activity_plants_drawer_layout);
        addPlantButton = findViewById(R.id.add_plant_layout);
        navigationView = findViewById(R.id.activity_plants_nav_view);
        linearLayout = findViewById(R.id.plants_constraint_layout);
        TextView deviceIndicator = navigationView.getHeaderView(0).findViewById(R.id.device_indicator_text);

        toggle = new ActionBarDrawerToggle(
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

        navigationIconClickListener = toggle.getToolbarNavigationClickListener();
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        drawerArrowDrawable = toggle.getDrawerArrowDrawable();
        toggle.setHomeAsUpIndicator(toggle.getDrawerArrowDrawable());
        drawerArrowDrawable.setColor(getColor(R.color.colorIcons));
        ValueAnimator arrowAnimator = ValueAnimator.ofFloat(0f, 1f);
        arrowAnimator.addUpdateListener(animation -> {
            drawerArrowDrawable.setProgress((float) animation.getAnimatedValue());
        });

        navigationView.setNavigationItemSelectedListener(this::navigationMenuSelect);
        navigationView.setCheckedItem(R.id.nav_plants);

        fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(() -> {
            if (arrowAnimator.isRunning()) arrowAnimator.end();
            if (fragmentManager.getBackStackEntryCount() == 1) {
                arrowAnimator.start();
                drawerArrowDrawable.setVerticalMirror(true);
                toggle.setDrawerIndicatorEnabled(false);
                toggle.setToolbarNavigationClickListener(v -> onBackPressed());
            } else {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                arrowAnimator.reverse();
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                getSupportActionBar().setTitle("Plants");
                toggle.setDrawerIndicatorEnabled(true);
                drawerArrowDrawable.setVerticalMirror(false);
                addPlantButton.setVisibility(noConnectionFragment == null ? View.VISIBLE : View.GONE);
            }
        });
        addPlantButton.setOnClickListener(this::openNewPlantDialogue);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (savedInstanceState == null) { // Only add fragments if loading Activity for the first time.
            if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                cardListFragment = new CardListFragment();
                fragmentTransaction.add(R.id.plants_frame_layout, cardListFragment, FRAGMENT_TAG);
                addPlantButton.setVisibility(View.VISIBLE);
            } else {
                noConnectionFragment = new NoConnectionFragment();
                fragmentTransaction.add(R.id.plants_constraint_layout, noConnectionFragment, BLUETOOTH_FRAGMENT_TAG);
                addPlantButton.setVisibility(View.GONE);
                setScrollViewLayoutParams(linearLayout, true);
            }
            fragmentTransaction.commit();
        }

        plantViewModel = ViewModelProviders.of(this).get(PlantViewModel.class);

        deviceViewModel = ViewModelProviders.of(this).get(BluetoothDeviceViewModel.class);
        deviceViewModel.getSelectedDevice().observe(this, d -> deviceIndicator.setText(
                d != null ? "Device: " + d.getName() : getString(R.string.nav_header_subtitle)));

        Intent serviceIntent = new Intent(this, BluetoothMessengerService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setProgress((float) fragmentManager.getBackStackEntryCount());
        if (fragmentManager.getBackStackEntryCount() == 1) {
            drawerArrowDrawable.setVerticalMirror(true);
            toggle.setDrawerIndicatorEnabled(false);
            toggle.setToolbarNavigationClickListener(v -> {
                onBackPressed();
            });
        } else {
            toggle.setDrawerIndicatorEnabled(true);
            getSupportActionBar().setTitle("Plants");
            drawerArrowDrawable.setVerticalMirror(false);
            addPlantButton.setVisibility(BluetoothAdapter.getDefaultAdapter().isEnabled() ? View.VISIBLE : View.GONE);
        }
    }

    public boolean navigationMenuSelect(MenuItem item) {
        drawerIntent = MainActivity.mainNavigationMenuSelect(this, item);
        navigationView.setCheckedItem(item);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NoConnectionFragment.REQUEST_BLUETOOTH_ENABLE) {
            if (resultCode == RESULT_OK) {
                onRetrySuccess();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_plants, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_delete_all:
                if (cardListFragment != null && cardListFragment.isVisible()) {
                    cardListFragment.deleteAllElements();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openNewPlantDialogue(View view) {
        openNewPlantDialogue(PlantFragment.NO_PLANT, null);
    }

    public void openNewPlantDialogue(int id, Plant plant) {
        fragmentManager.beginTransaction()
                .replace(R.id.plants_frame_layout, PlantFragment.newInstance(id, plant))
                .addToBackStack(PLANT_FRAGMENT_TAG)
                .commit();

        addPlantButton.setVisibility(View.GONE);
    }

    @Override
    public void bluetoothRead(BluetoothPacket packet) {
        if (packet instanceof IncomingPlantNumberPacket) {
            IncomingPlantNumberPacket plantNumberPacket = (IncomingPlantNumberPacket) packet;


            //fragment.updatePlantNumber(plantNumberPacket.getPlantNumber());
        } else if (packet instanceof IncomingPlantDataPacket) {
            IncomingPlantDataPacket plantDataPacket = (IncomingPlantDataPacket) packet;
        }
    }

    public void onRetrySuccess() {
        if (fragmentManager.findFragmentByTag(BLUETOOTH_FRAGMENT_TAG) != null) {
            fragmentManager.beginTransaction()
                    .remove(fragmentManager.findFragmentByTag(BLUETOOTH_FRAGMENT_TAG))
                    .commit();
        }
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        cardListFragment = new CardListFragment();
        fragmentTransaction.replace(R.id.plants_frame_layout, cardListFragment, FRAGMENT_TAG);
        fragmentTransaction.commit();
        addPlantButton.setVisibility(View.VISIBLE);
        setScrollViewLayoutParams(linearLayout, false);
    }

    private void setScrollViewLayoutParams(View view, boolean center) {
        NestedScrollView.LayoutParams params = new NestedScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        params.gravity = center ? Gravity.CENTER : Gravity.NO_GRAVITY;
        view.setLayoutParams(params);
    }

    @Override
    public void onItemClicked(Plant plant) {
        openNewPlantDialogue(plant.getId(), plant);
    }
}
