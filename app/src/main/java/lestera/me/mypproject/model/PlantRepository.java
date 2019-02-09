package lestera.me.mypproject.model;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

import androidx.lifecycle.LiveData;

public class PlantRepository {
    private PlantDao plantDao;
    private LiveData<List<Plant>> allPlants;

    public PlantRepository(Application application) {
        PlantDatabase plantDatabase = PlantDatabase.getInstance(application);
        plantDao = plantDatabase.plantDao();
        allPlants = plantDao.getAllPlants();
    }

    public void insert(Plant plant) {
        // executor.execute(() -> plantDao.insert(plant));
        new InsertPlantAsyncTask(plantDao).execute(plant);
    }

    public void update(Plant plant) {
        new UpdatePlantAsyncTask(plantDao).execute(plant);
    }

    public void delete(Plant plant) {
        new DeletePlantAsyncTask(plantDao).execute(plant);
    }

    public void deleteAllPlants() {
        new DeleteAllPlantsAsyncTask(plantDao).execute();
    }

    public LiveData<List<Plant>> getAllPlants() {
        return allPlants;
    }

    private static class InsertPlantAsyncTask extends AsyncTask<Plant, Void, Void> {
        private PlantDao plantDao;

        private InsertPlantAsyncTask(PlantDao plantDao) {
            this.plantDao = plantDao;
        }

        @Override
        protected Void doInBackground(Plant... plants) {
            plantDao.insert(plants[0]);
            return null;
        }
    }

    private static class UpdatePlantAsyncTask extends AsyncTask<Plant, Void, Void> {
        private PlantDao plantDao;

        private UpdatePlantAsyncTask(PlantDao plantDao) {
            this.plantDao = plantDao;
        }

        @Override
        protected Void doInBackground(Plant... plants) {
            plantDao.update(plants[0]);
            return null;
        }
    }

    private static class DeletePlantAsyncTask extends AsyncTask<Plant, Void, Void> {
        private PlantDao plantDao;

        private DeletePlantAsyncTask(PlantDao plantDao) {
            this.plantDao = plantDao;
        }

        @Override
        protected Void doInBackground(Plant... plants) {
            plantDao.delete(plants[0]);
            return null;
        }
    }

    private static class DeleteAllPlantsAsyncTask extends AsyncTask<Void, Void, Void> {
        private PlantDao plantDao;

        private DeleteAllPlantsAsyncTask(PlantDao plantDao) {
            this.plantDao = plantDao;
        }

        @Override
        protected Void doInBackground(Void... plants) {
            plantDao.deleteAllPlants();
            return null;
        }
    }

}
