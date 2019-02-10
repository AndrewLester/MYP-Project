package lestera.me.mypproject.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import lestera.me.mypproject.viewmodel.PlantViewModel;
import lestera.me.mypproject.R;

public class PlantFragment extends Fragment {

    private PlantViewModel plantViewModel;

    public static PlantFragment newInstance() {
        return new PlantFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.plant_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //mViewModel = ViewModelProviders.of(this).get(PlantViewModel.class);
        // TODO: Use the ViewModel
    }

}
