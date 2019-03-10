package lestera.me.mypproject.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

import androidx.annotation.Nullable;
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

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return "Plant[" + "name=" + name + ",description=" +
                description + ",imageUri=" + imageUri.toString() + "]";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Plant)) {
            return false;
        }

        Plant plant = (Plant) obj;
        return plant.getId() == getId() && plant.getName().equals(getName()) && plant.getDescription().equals(getDescription()) &&
                plant.getImageUri().equals(getImageUri());
    }
}
