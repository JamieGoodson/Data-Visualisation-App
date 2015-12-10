package com.example.kos.teamheliotrope.backend;

//~ JDK/Android Imports ========================================

import android.content.Context;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Testing class.
 * Tests for successful caching of data to the internal storage
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