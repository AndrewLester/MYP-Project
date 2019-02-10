package lestera.me.mypproject.viewmodel;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import lestera.me.mypproject.R;
import lestera.me.mypproject.fragments.OnItemClickListener;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.BluetoothDeviceHolder> {

    private List<BluetoothDevice> devices = new ArrayList<>();
    private OnItemClickListener<BluetoothDevice> listener;

    @NonNull
    @Override
    public BluetoothDeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new BluetoothDeviceHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BluetoothDeviceHolder holder, int position) {
        BluetoothDevice currentDevice = devices.get(position);
        holder.title.setText(currentDevice.getName());
        holder.subtitle.setText(currentDevice.getAddress());
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void setBluetoothDevices(List<BluetoothDevice> devices) {
        this.devices = devices;
        notifyDataSetChanged();
    }

    class BluetoothDeviceHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView subtitle;
        private Button button;

        public BluetoothDeviceHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.list_item_text);
            subtitle = itemView.findViewById(R.id.list_item_subtext);
            button = itemView.findViewById(R.id.list_item_button);

            button.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(devices.get(getAdapterPosition()));
                }
            });
        }
    }

    public void setOnItemClickListener(OnItemClickListener<BluetoothDevice> listener) {
        this.listener = listener;
    }
}
