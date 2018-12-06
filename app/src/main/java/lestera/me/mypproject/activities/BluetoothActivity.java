package lestera.me.mypproject.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lestera.me.mypproject.R;

public class BluetoothActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_BLUETOOTH_ENABLE = 0;

    private ArrayList<BluetoothDevice> devices = new ArrayList<>();

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerSlideAnimationEnabled(false);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            // TODO: Add 2 fragments. 1 for background with no connection, one for background with,
        }

        ListView listView = findViewById(R.id.list_view);

        devices.addAll(BluetoothAdapter.getDefaultAdapter().getBondedDevices());

        listView.setAdapter(new DeviceListAdapter(this, R.layout.list_item, devices));
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

        private int layout;

        private DeviceListAdapter(Context context, int resource, List<BluetoothDevice> objects) {
            super(context, resource, objects);

            layout = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DeviceViewHolder deviceViewHolder = null;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);

                DeviceViewHolder viewHolder = new DeviceViewHolder();
                viewHolder.thumbnail = convertView.findViewById(R.id.list_item_thumbnail);
                viewHolder.title = convertView.findViewById(R.id.list_item_text);
                viewHolder.title.setText(getItem(position).getName());
                viewHolder.button = convertView.findViewById(R.id.list_item_button);
                viewHolder.button.setOnClickListener(v -> {
                    Toast.makeText(getContext(), "Done: " + getItem(position).getAddress(), Toast.LENGTH_LONG).show();
                });

                convertView.setTag(viewHolder);
            } else {
                deviceViewHolder = (DeviceViewHolder) convertView.getTag();
                deviceViewHolder.title.setText(getItem(position).getName());
            }

            return convertView;
        }
    }

    public class DeviceViewHolder {
        ImageView thumbnail;
        TextView title;
        Button button;
    }

}
