package lestera.me.mypproject.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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

import lestera.me.mypproject.R;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ItemFragment extends Fragment {
    private static final String DEVICES_KEY = "devices";

    private OnListFragmentInteractionListener mListener;

    private ArrayList<BluetoothDevice> devices = new ArrayList<>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemFragment() {}

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ItemFragment newInstance(int columnCount) {
        ItemFragment fragment = new ItemFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        devices.addAll(savedInstanceState == null ?
                BluetoothAdapter.getDefaultAdapter().getBondedDevices() :
                savedInstanceState.getParcelableArrayList(DEVICES_KEY));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(DEVICES_KEY, devices);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        // Set the adapter
        if (view instanceof ListView) {
            Context context = view.getContext();
            ListView listView = (ListView) view;
            listView.setAdapter(new DeviceListAdapter(context, R.layout.list_item, devices));
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(BluetoothDevice device);
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
                    mListener.onListFragmentInteraction(getItem(position));
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
