package com.example.kos.teamheliotrope.backend;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by firasAltayeb on 08/12/2015.
 */
public class IndicatorTest {

    Indicator indicator;
    Value value;

    /**
     * this method initializes an indicator with an Id and title
     * this method also initializes a value object with a data and value
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        indicator = new Indicator("testId","testTitle");
        value = new Value("August",(float) 100);
        indicator.addValue(value);
    }

    /**
     * this method check whether getValue works by checking whether the returned
     * value's float value is equal to the float value it was assigned
     */
    @Test
    public void testGetValue() throws Exception {
        assertEquals(indicator.getValue("August").getValue() == 100, true);
    }

    /**
     * this methods checks whether getValues() works by checking whether the method is
     * capable of returning an arrayList.
     */
    @Test
    public void testGetValues() throws Exception {
        assertNotNull(indicator.getValues());
    }

    /**
     * this method checks whether addValue works by checking whether the values arrayList
     * size increase from 1 to 2 when a value is added using the method.
     */
    @Test
    public void testAddValue() throws Exception {
        indicator.addValue(value);
        assertEquals(indicator.getValues().size() == 2, true);
    }

    /**
     * this method checks whether getId works by checking whether the value
     * retrieved from indicator object is the same as the value it was assigned to.
     */
    @Test
    public void testGetId() throws Exception {
        assertEquals(indicator.getId().equals("testId"), true);
    }

    /**
     * this method checks whether getTitle works by checking whether the value
     * retrieved from indicator object is the same as the value it was assigned to.
     */
    @Test
    public void testGetTitle() throws Exception {
        assertEquals(indicator.getTitle().equals("testTitle"), true);
    }
}