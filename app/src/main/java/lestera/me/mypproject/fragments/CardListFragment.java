package lestera.me.mypproject.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import lestera.me.mypproject.R;
import lestera.me.mypproject.viewmodel.PlantAdapter;
import lestera.me.mypproject.viewmodel.PlantViewModel;

public class CardListFragment extends Fragment {

    public CardListFragment() {}

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static CardListFragment newInstance() {
        return new CardListFragment();
    }

    private PlantViewModel plantViewModel;
    private PlantAdapter adapter;
    private RecyclerView recyclerView;
    private int position = -1;

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
}
