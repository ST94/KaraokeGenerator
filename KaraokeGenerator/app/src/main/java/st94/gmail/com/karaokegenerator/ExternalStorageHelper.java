package st94.gmail.com.karaokegenerator;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by Shing on 2016-06-26.
 */
public class ExternalStorageHelper {

    private static final String TAG = "ExternalStorageHelper" ;

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public File createSongFilePath(Context context, String songName) {
        // Get the directory for the app's private pictures directory.
        File file = new File(context.getExternalFilesDir(null), songName);
        if (!file.mkdirs()) {
            Log.i(TAG, "Directory not created");
        }
        return file;
    }
}
