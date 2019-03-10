package lestera.me.mypproject.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import lestera.me.mypproject.R;
import lestera.me.mypproject.model.Plant;
import lestera.me.mypproject.viewmodel.PlantAdapter;
import lestera.me.mypproject.viewmodel.PlantViewModel;

public class CardListFragment extends Fragment implements OnItemClickListener {

    public CardListFragment() {}

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static CardListFragment newInstance() {
        return new CardListFragment();
    }

    private PlantViewModel plantViewModel;
    private PlantAdapter adapter;
    private RecyclerView recyclerView;
    private PlantFragmentUpdateListener listener;
    private int position = -1;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PlantFragmentUpdateListener) {
            listener = (PlantFragmentUpdateListener) context;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        plantViewModel = ViewModelProviders.of(getActivity()).get(PlantViewModel.class);
        plantViewModel.getAllPlants().observe(getViewLifecycleOwner(), adapter::setPlants);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new PlantAdapter();
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        ViewCompat.setNestedScrollingEnabled(recyclerView, false);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        position = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        Log.e("POSITION", String.valueOf(position));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (position != RecyclerView.NO_POSITION) {
            recyclerView.scrollToPosition(position);
            Log.e("MOVED", String.valueOf(position));
        }
    }

    @Override
    public void onItemClick(int position, View view) {
        switch (view.getId()) {
            case R.id.card_button_delete:
                confirmDeletion("Delete this plant?", () -> {
                    Toast.makeText(getActivity(), "Plant \"" + adapter.getPlantAt(position).getName() + "\" deleted", Toast.LENGTH_SHORT).show();
                    plantViewModel.delete(adapter.getPlantAt(position));
                });
                break;
            case R.id.card_button_share:
                break;
            case R.id.card_layout:
                listener.onItemClicked(adapter.getPlantAt(position));
                break;
        }
    }

    public void deleteAllElements() {
        confirmDeletion("Delete all plants?",
                () -> plantViewModel.deleteAllPlants());
    }

    private void confirmDeletion(String message, Runnable onSuccess) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message).setPositiveButton("OK", (d, i) -> {
            if (i == DialogInterface.BUTTON_POSITIVE) {
                onSuccess.run();
                d.dismiss();
            }
        }).setNegativeButton("CANCEL", (d, i) -> d.dismiss()).show();
    }

    public interface PlantFragmentUpdateListener {
        void onItemClicked(Plant plant);
    }
}
