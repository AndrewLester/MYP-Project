package lestera.me.mypproject.viewmodel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import lestera.me.mypproject.ImagePicker;
import lestera.me.mypproject.R;
import lestera.me.mypproject.fragments.OnItemClickListener;
import lestera.me.mypproject.model.Plant;

public class PlantAdapter extends ListAdapter<Plant, PlantAdapter.PlantHolder> {

    private static final DiffUtil.ItemCallback<Plant> DIFF_CALLBACK = new DiffUtil.ItemCallback<Plant>() {
        @Override
        public boolean areItemsTheSame(@NonNull Plant oldItem, @NonNull Plant newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Plant oldItem, @NonNull Plant newItem) {
            boolean imagesSame = false;
            if (oldItem.getImageUri() == null || newItem.getImageUri() == null) {
                if (oldItem.getImageUri() == newItem.getImageUri()) {
                    imagesSame = true;
                }
            } else {
                imagesSame = oldItem.getImageUri().equals(newItem.getImageUri());
            }

            return oldItem.getId() == newItem.getId() &&
                    oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getDescription().equals(newItem.getDescription()) &&
                    oldItem.getWaterLimit() == newItem.getWaterLimit() &&
                    oldItem.getWateringTime() == newItem.getWateringTime() &&
                    imagesSame;
        }
    };

    private OnItemClickListener listener;

    public PlantAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public PlantHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_item, parent, false);
        return new PlantHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantHolder holder, int position) {
        Plant currentPlant = getItem(position);
        holder.thumbnail.setImageURI(null);
        if (currentPlant.getImageUri() != null) {
            holder.thumbnail.setImageDrawable(null);
            Picasso.with(holder.thumbnail.getContext())
                    .load(currentPlant.getImageUri())
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_add_a_photo_black_24dp)
                    .into(holder.thumbnail);
        }
        holder.title.setText(currentPlant.getName());
        holder.description.setText(currentPlant.getDescription());
    }

    public Plant getPlantAt(int position) {
        return getItem(position);
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
            viewButton = itemView.findViewById(R.id.card_button_delete);

            View.OnClickListener clickListener = v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getAdapterPosition(), v);
                }
            };

            viewButton.setOnClickListener(clickListener);
            itemView.setOnClickListener(clickListener);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
