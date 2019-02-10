package lestera.me.mypproject.fragments;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
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

public class BluetoothItemListFragment extends Fragment {
    public BluetoothItemListFragment() {}

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static BluetoothItemListFragment newInstance() {
        return new BluetoothItemListFragment();
    }

    private BluetoothDeviceViewModel deviceViewModel;
    private BluetoothDeviceAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        deviceViewModel = ViewModelProviders.of(getActivity()).get(BluetoothDeviceViewModel.class);
        deviceViewModel.getAllDevices().observe(getViewLifecycleOwner(), adapter::setBluetoothDevices);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        adapter = new BluetoothDeviceAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(device -> {
            Toast.makeText(getContext(), "Selected: " + device.getName(), Toast.LENGTH_SHORT).show();
            deviceViewModel.setSelectedDevice(device);
        });
        return view;
    }
}
