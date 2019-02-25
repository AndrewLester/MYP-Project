package lestera.me.mypproject.viewmodel;

import android.app.Application;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import lestera.me.mypproject.model.Plant;
import lestera.me.mypproject.model.PlantRepository;

public class PlantViewModel extends AndroidViewModel {
    private PlantRepository repository;
    private LiveData<List<Plant>> allPlants;

    public PlantViewModel(Application application) {
        super(application);
        repository = new PlantRepository(application);
        allPlants = repository.getAllPlants();
    }

    public void insert(Plant plant) {
        repository.insert(plant);
    }

    public void update(Plant plant) {
        repository.update(plant);
    }

    public void delete(Plant plant) {
        repository.delete(plant);
    }

    public void deleteAllPlants() {
        repository.deleteAllPlants();
    }

    public LiveData<List<Plant>> getAllPlants() {
        return allPlants;
    }
}
