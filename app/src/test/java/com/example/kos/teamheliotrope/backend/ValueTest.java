package com.example.kos.teamheliotrope.backend;

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
        test = new Value("August", (float) 36.2);
    }

    /**
     * this method checks whether toString() works by checking
     * whether the result returned by the method is equal to
     * what is expected
     */
    @Test
    public void testToString() throws Exception {
        assertEquals(test.toString().equals("August | 36.2"),true);
    }


    /**
     * this method checks whether getData works by checking whether the data
     * retrieved from test is the same as the data test was assigned to.
     */
    @Test
    public void testGetDate() throws Exception {
        assertEquals(test.getDate().equals("August"), true);
    }

    /**
     * this method checks whether getValue works by checking whether the value
     * retrieved from test is the same as the value test was assigned to.
     */
    @Test
    public void testGetValue() throws Exception {
        assertEquals(test.getValue() == (float) 36.2, true);
    }
}