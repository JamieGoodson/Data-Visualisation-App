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
 */
public final class InternalStorage {

    private InternalStorage() {}

    public static void writeObject(Context context, String key, Object object) throws IOException {
        FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        oos.close();
        fos.close();
    }

    public static Object readObject(Context context, String key) throws IOException,
            ClassNotFoundException {
        FileInputStream fis = context.openFileInput(key);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object object = ois.readObject();

        return object;
    }

    public static boolean cacheExists(Context context, String key) {
        File file = context.getFileStreamPath(key);

        if ((file == null || !file.exists())) {
            return false;
        }

        return true;
    }
}
