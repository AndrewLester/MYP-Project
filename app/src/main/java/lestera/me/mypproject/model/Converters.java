package lestera.me.mypproject.model;

import android.net.Uri;

import androidx.room.TypeConverter;

class Converters {
    @TypeConverter
    public static Uri fromString(String string) {
        return string == null ? null : Uri.parse(string);
    }
    @TypeConverter
    public static String uriToString(Uri uri) {
        return uri == null ? null : uri.toString();
    }
}
