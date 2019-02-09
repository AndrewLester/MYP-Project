package lestera.me.mypproject.fragments;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import lestera.me.mypproject.R;
import lestera.me.mypproject.viewmodel.BluetoothDeviceAdapter;
import lestera.me.mypproject.viewmodel.BluetoothDeviceViewModel;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link ListFragmentInteractionListener}
 * interface.
 */
public class BluetoothItemListFragment extends Fragment {
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

    private BluetoothDeviceViewModel deviceViewModel;
    private ListFragmentInteractionListener<BluetoothDevice> listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        BluetoothDeviceAdapter adapter = new BluetoothDeviceAdapter();
        recyclerView.setAdapter(adapter);

        deviceViewModel = ViewModelProviders.of(this).get(BluetoothDeviceViewModel.class);
        deviceViewModel.getAllDevices().observe(this, adapter::setBluetoothDevices);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ListFragmentInteractionListener<BluetoothDevice>) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(context.toString()
                    + " must use ListFragmentInteractionListener with type BluetoothDevice.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
