package lestera.me.mypproject.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "plant_table")
public class Plant {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "image_uri")
    private Uri imageUri;
    private String name;
    private String description;

    public Plant(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
