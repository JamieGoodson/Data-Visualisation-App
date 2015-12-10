package com.example.kos.teamheliotrope.backend;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.support.annotation.Nullable;
import android.view.Display;

import com.example.kos.teamheliotrope.frontend.MainActivity;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Created by firasAltayeb on 10/12/2015.
 */
public class InternalStorageTest {

    Context mainActivity;
    String COUNTRYKEY;
    Country UK;
    Country US;

    /**
     * this method initializes the CountryKey, declares a country
     * and saves an object to the cache.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        COUNTRYKEY = "COUNTRIES_DATA";
        UK = new Country();
        InternalStorage.writeObject(mainActivity,COUNTRYKEY,US);
    }

    /**
     * this test checks whether the writeObject() method works by checking whether it is capable of
     * storing an object to the cache
     * @throws Exception
     */
    @Test
    public void testWriteObject() throws Exception {
        InternalStorage.writeObject(mainActivity,COUNTRYKEY,UK);
        assertEquals(InternalStorage.cacheExists(mainActivity, COUNTRYKEY), true);
    }

    /**
     * this test checks whether the readObject() method works by checking whether it is capable of
     * returning the initialized object from the cache
     * @throws Exception
     */
    @Test
    public void testReadObject() throws Exception {
        assertNotNull(InternalStorage.readObject(mainActivity,COUNTRYKEY));
    }

    /**
     * this test checks whether the cacheExists() method works by checking whether the
     * initialized object exists in the cache
     * @throws Exception
     */
    @Test
    public void testCacheExists() throws Exception {
        assertNotNull(InternalStorage.cacheExists(mainActivity,COUNTRYKEY));
    }
}