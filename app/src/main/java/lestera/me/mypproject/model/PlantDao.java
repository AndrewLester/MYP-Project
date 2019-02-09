package lestera.me.mypproject.model;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface PlantDao {

    @Insert
    void insert(Plant plant);

    @Update
    void update(Plant plant);

    @Delete
    void delete(Plant plant);

    @Query("DELETE FROM plant_table")
    void deleteAllPlants();

    @Query("SELECT * FROM plant_table ORDER BY id DESC")
    LiveData<List<Plant>> getAllPlants();
}
