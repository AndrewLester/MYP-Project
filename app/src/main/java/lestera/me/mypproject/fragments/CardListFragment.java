package lestera.me.mypproject.fragments;

import android.content.Context;
import android.os.Parcel;
import android.support.v4.app.Fragment;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;

import lestera.me.mypproject.BluetoothMessengerService;
import lestera.me.mypproject.Plant;
import lestera.me.mypproject.R;
import lestera.me.mypproject.activities.PlantsActivity;
import lestera.me.mypproject.packets.OutgoingLEDPacket;

public class CardListFragment extends ItemListFragment<Plant> {

    private static final String PLANT_FILE_PREFIX = "plant";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CardListFragment() {}

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static CardListFragment newInstance(int columnCount) {
        return new CardListFragment();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent, int layout, Function<Integer, Plant> getItem) {
        PlantViewHolder plantViewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);

            PlantViewHolder viewHolder = new PlantViewHolder();
            viewHolder.thumbnail = convertView.findViewById(R.id.card_image);
            viewHolder.thumbnail.setImageURI(getItem.apply(position).getImageUri());
            viewHolder.title = convertView.findViewById(R.id.card_title);
            viewHolder.title.setText(getItem.apply(position).getName());
            viewHolder.viewButton.setOnClickListener(v -> notifyInteraction(getItem.apply(position), position));

            convertView.setTag(viewHolder);
        } else {
            plantViewHolder = (PlantViewHolder) convertView.getTag();
            plantViewHolder.title.setText(getItem.apply(position).getName());
            plantViewHolder.thumbnail.setImageURI(getItem.apply(position).getImageUri());
        }

        return convertView;
    }

    private Optional<List<Plant>> getPlantsFromCache() {
        List<Plant> plantList = new ArrayList<>();
        File cacheDir = getContext().getCacheDir();
        for (File file : cacheDir.listFiles((d, n) -> n.startsWith(PLANT_FILE_PREFIX))) {
            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file))) {
                plantList.add((Plant) inputStream.readObject());
            } catch (IOException | ClassNotFoundException e) {
                return Optional.empty();
            }
        }

        return Optional.of(plantList);
    }

    public void updatePlantNumber(int plantNumber) {
        for (int i = 0; i < plantNumber; i++) {
            list.add(new Plant("SkeletonPlant" + i, "SkeletonPlant" + i));
        }
    }

    private List<Plant> requestPlantData() {
        ((PlantsActivity) getActivity()).requestPlantNumber();
        return new ArrayList<>();
    }

    @Override
    public List<Plant> getPopulatingList() {
        Optional<List<Plant>> optionalPlants = getPlantsFromCache();
        List<Plant> plants = optionalPlants.orElseGet(this::requestPlantData);
        plants.sort(Comparator.comparingInt(Plant::getId));
        return plants;
    }

    public void plantLoaded(Plant plant) {

        // TODO: Hide view skeleton for plant
        // TODO: Change view contents for plant
        // TODO: Cache plant
    }

    @Override
    public int getListViewItem() {
        return R.layout.card_item;
    }

    public class PlantViewHolder {
        ImageView thumbnail;
        TextView title;
        Button shareButton;
        Button viewButton;
    }
}
