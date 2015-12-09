package com.example.kos.teamheliotrope.backend;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;

/**
 * Created by tuffail on 07/12/15.
 * Used for caching data to internal storage
 */
public final class InternalStorage {
    /**
     * Saves an Object to the Cache of the app
     * @param context Context of the Activity
     * @param key Key to be used as a reference to the storage and retrieval of the cached Object
     * @param object Object to be stored
     * @throws IOException
     */
    public static void writeObject(Context context, String key, Object object) throws IOException {
        FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        oos.close();
        fos.close();
    }

    /**
     * Retrieves an Object from internal storage (cache) using the Key and Context of the Activity
     * @param context Context of the Activity
     * @param key Key to be used as a reference to the storage and retrieval of the cached Object
     * @return Object retrieved from internal storage (cache)
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object readObject(Context context, String key) throws IOException,
            ClassNotFoundException {
        FileInputStream fis = context.openFileInput(key);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object object = ois.readObject();

        return object;
    }

    /**
     * Checks if an Object exists in the internal storage (cache)
     * @param context Context of the Activity
     * @param key Key to be used as a reference to the storage and retrieval of the cached Object
     * @return true if it exists in the cache, otherwise false
     */
    public static boolean cacheExists(Context context, String key) {
        File file = context.getFileStreamPath(key);

        if ((file == null || !file.exists())) {
            return false;
        }

        return true;
    }
}
