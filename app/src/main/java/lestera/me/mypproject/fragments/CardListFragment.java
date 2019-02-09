package lestera.me.mypproject.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import lestera.me.mypproject.R;
import lestera.me.mypproject.activities.PlantsActivity;
import lestera.me.mypproject.model.Plant;
import lestera.me.mypproject.viewmodel.PlantAdapter;
import lestera.me.mypproject.viewmodel.PlantViewModel;

public class CardListFragment extends Fragment {

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

    private PlantViewModel plantViewModel;
    private ListFragmentInteractionListener<Plant> listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        PlantAdapter adapter = new PlantAdapter();
        recyclerView.setAdapter(adapter);

        plantViewModel = ViewModelProviders.of(this).get(PlantViewModel.class);
        plantViewModel.getAllPlants().observe(this, adapter::setPlants);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ListFragmentInteractionListener<Plant>) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(context.toString()
                    + " must use ListFragmentInteractionListener with type Plant.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

}
