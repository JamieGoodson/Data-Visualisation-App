package com.example.kos.teamheliotrope.backend;

//~ JDK/Android Imports ========================================

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Testing class responsible for testing Country objects
 */
public class CountryTest {

    Country test;
    Indicator indicator;


    /**
     * this method initializes a name and an Id to the test object,
     * this method also declares and initializes an indicator
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        test = new Country();
        test.setName("testName");
        test.setId("testId");
        indicator = new Indicator("testId","testTitle");
    }

    /**
     * this method checks whether addIndicator() is capable of storing an indicator
     * in the arrayList and that the method is capable of storing only one copy
     * of each indicator.
     */
    @Test
    public void testAddIndicator() throws Exception {
        test.addIndicator(indicator);
        test.addIndicator(indicator);
        assertEquals(test.getIndicators().size() == 1, true);
    }

    /**
     * this methods checks whether getIndicator() works by initializing an indicator to
     * the arrayList and then check whether the method is capable of returning the same indicator
     * provided we use the same indicator's Id.
     */
    @Test
    public void testGetIndicator() throws Exception {
        test.addIndicator(indicator);
        assertNotNull(test.getIndicator("testId"));
    }

    /**
     * this methods checks whether getIndicators() works by initializing an indicator to
     * the arrayList and then check whether the method is capable of returning the arrayList.
     */
    @Test
    public void testGetIndicators() throws Exception {
        test.addIndicator(indicator);
        assertNotNull(test.getIndicators());
    }

    /**
     * this methods checks whether setName() works by setting a name to the test
     * object and then checking whether the name the object holds
     * is equal to the name it was assigned to.
     */
    @Test
    public void testSetName() throws Exception {
        test.setName("England");
        assertEquals(test.getName().equals("England"), true);
    }

    /**
     * this methods checks whether setId() works by setting an Id to the test
     * object and then checking whether the Id the object holds
     * is equal to the Id it was assigned to.
     */
    @Test
    public void testSetId() throws Exception {
        test.setId("UK");
        assertEquals(test.getId().equals("UK"),true);
    }

    /**
     * this methods checks whether getName() works by checking whether the name the test object holds
     * is equal to the name it was assigned to.
     * @throws Exception
     */
    @Test
    public void testGetName() throws Exception {
        assertEquals(test.getName().equals("testName"),true);
    }

    /**
     * this methods checks whether getId() works by checking whether the id the test object holds
     * is equal to the id it was assigned to.
     * @throws Exception
     */
    @Test
    public void testGetId() throws Exception {
        assertEquals(test.getId().equals("testId"), true);
    }

    /**
     * this methods checks whether getNullValueCount() works by checking whether the returned
     * value is equal to the assigned value
     */
    @Test
    public void testGetNullValueCount() throws Exception {
        assertEquals(test.getNullValueCount() == 0, true);
    }
}