package lestera.me.mypproject.viewmodel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import lestera.me.mypproject.R;
import lestera.me.mypproject.fragments.OnItemClickListener;
import lestera.me.mypproject.model.Plant;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantHolder> {

    private List<Plant> plants = new ArrayList<>();
    private OnItemClickListener<Plant> listener;

    @NonNull
    @Override
    public PlantHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_item, parent, false);
        return new PlantHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantHolder holder, int position) {
        Plant currentPlant = plants.get(position);
        holder.thumbnail.setImageURI(currentPlant.getImageUri());
        holder.title.setText(currentPlant.getName());
        holder.description.setText(currentPlant.getDescription());
    }

    @Override
    public int getItemCount() {
        return plants.size();
    }

    public void setPlants(List<Plant> plants) {
        this.plants = plants;
        notifyDataSetChanged();
    }

    class PlantHolder extends RecyclerView.ViewHolder {
        private ImageView thumbnail;
        private TextView title;
        private TextView description;
        private Button shareButton;
        private Button viewButton;

        public PlantHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.card_image);
            title = itemView.findViewById(R.id.card_title);
            description = itemView.findViewById(R.id.card_description);
            shareButton = itemView.findViewById(R.id.card_button_share);
            viewButton = itemView.findViewById(R.id.card_button_view);

            View.OnClickListener clickListener = v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(plants.get(getAdapterPosition()));
                }
            };

            viewButton.setOnClickListener(clickListener);
            itemView.setOnClickListener(clickListener);
        }
    }

    public void setOnItemClickListener(OnItemClickListener<Plant> listener) {
        this.listener = listener;
    }
}
