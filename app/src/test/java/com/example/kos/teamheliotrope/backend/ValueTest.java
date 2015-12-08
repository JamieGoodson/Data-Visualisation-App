package com.example.kos.teamheliotrope.backend;

import android.util.Log;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by firasAltayeb on 08/12/2015.
 */
public class ValueTest {

    Value test;

    /**
     * this method initializes a data and a value to the test
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        test = new Value("August 30", (float) 20022020);
    }

    /**
     * can some explain this to me?!?
     * @throws Exception
     */
    @Test
    public void testToString() throws Exception {
        //Log.d("to string method", test.toString());
    }


    /**
     * this method checks whether getData works by checking whether the data
     * retrieved from test is the same as the data test was assigned to.
     */
    @Test
    public void testGetDate() throws Exception {
        assertEquals(test.getDate().equals("August 30"), true);
    }

    /**
     * this method checks whether getValue works by checking whether the value
     * retrieved from test is the same as the value test was assigned to.
     */
    @Test
    public void testGetValue() throws Exception {
        assertEquals(test.getValue() == 20022020, true);
    }
}