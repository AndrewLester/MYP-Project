package lestera.me.mypproject.model;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = Plant.class, version = 2)
@TypeConverters({Converters.class})
public abstract class PlantDatabase extends RoomDatabase {

    private static PlantDatabase instance;

    // Code for this method autogenerated by Room database builder.
    public abstract PlantDao plantDao();

    public static synchronized PlantDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    PlantDatabase.class, "plant_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }

        return instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private PlantDao plantDao;

        private PopulateDbAsyncTask(PlantDatabase db) {
            plantDao = db.plantDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            plantDao.insert(new Plant("Plant", "None", (short) 500, (byte) 1));
            return null;
        }
    }
}
