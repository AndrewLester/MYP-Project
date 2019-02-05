package lestera.me.mypproject.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import lestera.me.mypproject.R;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class BluetoothItemListFragment extends ItemListFragment<BluetoothDevice> {
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BluetoothItemListFragment() {}

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static BluetoothItemListFragment newInstance(int columnCount) {
        return new BluetoothItemListFragment();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent, int layout, Function<Integer, BluetoothDevice> getItem) {
        DeviceViewHolder deviceViewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);

            DeviceViewHolder viewHolder = new DeviceViewHolder();
            viewHolder.thumbnail = convertView.findViewById(R.id.list_item_thumbnail);
            viewHolder.title = convertView.findViewById(R.id.list_item_text);
            viewHolder.title.setText(getItem.apply(position).getName());
            viewHolder.subtitle = convertView.findViewById(R.id.list_item_subtext);
            viewHolder.subtitle.setText(getItem.apply(position).getAddress());
            viewHolder.button = convertView.findViewById(R.id.list_item_button);
            viewHolder.button.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Selected: " + getItem.apply(position).getName(), Toast.LENGTH_LONG).show();
                super.notifyInteraction(getItem.apply(position), position);
            });

            convertView.setTag(viewHolder);
        } else {
            deviceViewHolder = (DeviceViewHolder) convertView.getTag();
            deviceViewHolder.title.setText(getItem.apply(position).getName());
            deviceViewHolder.subtitle.setText(getItem.apply(position).getAddress());
        }

        return convertView;
    }

    @Override
    public List<BluetoothDevice> getPopulatingList() {
        return new ArrayList<>(BluetoothAdapter.getDefaultAdapter().getBondedDevices());
    }

    @Override
    public int getListViewItem() {
        return R.layout.list_item;
    }

    public class DeviceViewHolder {
        ImageView thumbnail;
        TextView title;
        TextView subtitle;
        Button button;
    }
}
