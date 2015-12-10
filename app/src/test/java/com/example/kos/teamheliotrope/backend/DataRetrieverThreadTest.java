package com.example.kos.teamheliotrope.backend;

import com.example.kos.teamheliotrope.frontend.MainActivity;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by firasAltayeb on 09/12/2015.
 */
public class DataRetrieverThreadTest {

    MainActivity activity;
    JSONArray jsonArray;
    Country  UK;
    String countryCode;
    String indicatorCode;
    DataRetrieverThread DRT;

    /**
     * this method declares a country and initializes both countryCode and the indicatorCode
     * with the appropriate codes and then it declares a DataRetrieverThread object using the
     * initialized variables
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        UK = new Country();
        countryCode = "GP";
        indicatorCode = "EG.USE.SOMM.FO.ZS";
        DRT = new DataRetrieverThread(activity,UK,countryCode,indicatorCode);
    }

    /**
     * the test method checks whether the run() method works by checking whether the fetchJSONArray() method was
     * able to initialize an object array and the processJSONArray() method was able to create an
     * indicator for the current country.
     */
    @Test
    public void testRun() throws Exception {
        DRT.run();
        assertNotNull(UK.getIndicator(indicatorCode));
    }

    /**
     * this test method checks whether the fetchJSONArray() method works by checking whether the jsonArray
     * is equal to the number of objects in the query
     * @throws Exception
     */
    @Test
    public void testFetchJSONArray() throws Exception {
        jsonArray = DRT.fetchJSONArray();
        assertEquals(jsonArray.length() == 2, true);
    }

    /**
     * this test method checks whether the processJSONArray() method works by checking that the
     * method was able to create an indicator for the current country.
     * @throws Exception
     */
    @Test
    public void testProcessJSONArray() throws Exception {
        DRT.jsonArray = DRT.fetchJSONArray();
        DRT.processJSONArray();
        assertNotNull(UK.getIndicator(indicatorCode));
    }
}