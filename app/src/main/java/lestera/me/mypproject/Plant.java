package lestera.me.mypproject;

import android.media.Image;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.io.Serializable;

public class Plant implements Parcelable, Serializable {

    private Uri imageUri;
    private String name;
    private String description;
    private int id;

    public Plant(String name, String description) {
        this.name = name;
        this.description = description;
    }

    protected Plant(Parcel in) {
        name = in.readString();
        description = in.readString();
    }

    public static final Creator<Plant> CREATOR = new Creator<Plant>() {
        @Override
        public Plant createFromParcel(Parcel in) {
            return new Plant(in);
        }

        @Override
        public Plant[] newArray(int size) {
            return new Plant[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (imageUri != null) {
            Uri.writeToParcel(dest, imageUri);
        }
        dest.writeString(name);
        dest.writeString(description);
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }
}
