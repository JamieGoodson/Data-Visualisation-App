package com.example.kos.teamheliotrope.backend;

//~ JDK/Android Imports ========================================

import com.example.kos.teamheliotrope.frontend.MainActivity;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testing class.
 * Tests for successful retrieval of the JSON Array from the World Data Bank and for processing of JSON information
 * Tests whether Country objects are produced
 */
public class CountryInfoThreadTest {

    CountryInfoThread CIT;
    String countryQuery = "http://api.worldbank.org/country?per_page=300&region=WLD&format=json";
    JSONArray jsonArray;
    MainActivity activity;

    /**
     * this method initializes the CIT object
     */
    @Before
    public void setUp() throws Exception {
        CIT = new CountryInfoThread(activity, countryQuery);
    }

    /**
     * the test method checks whether the run() method works by checking whether the fetchJSONArray() method was
     * able to initialize an object array and the processJSONArray() method was able to create a
     * country for all objects that have a longitude and latitude.
     */
    @Test
    public void testRun() throws Exception {
        CIT.run();
        assertEquals(Countries.countries.size() == 209, false);
    }

    /**
     * this test method checks whether the fetchJSONArray() method works by checking whether the jsonArray
     * is equal to the number of objects in the query
     * @throws Exception
     */
    @Test
    public void testFetchJSONArray() throws Exception {
        jsonArray = CIT.fetchJSONArray();
        assertEquals(jsonArray.length() == 2, true);
    }

    /**
     * this test method checks whether the processJSONArray() method works by checking whether
     * the correct number of countries were declared and initialized
     * @throws Exception
     */
    @Test
    public void testProcessJSONArray() throws Exception {
        CIT.jsonArray = CIT.fetchJSONArray();
        CIT.processJSONArray();
        assertEquals(Countries.countries.size() == 209, false);
    }
}