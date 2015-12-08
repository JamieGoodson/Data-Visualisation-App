package com.example.kos.teamheliotrope.backend;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by firasAltayeb on 07/12/2015.
 */
public class CountriesTest {

    Country US;

    /**
     * this method initializes a country and then adds it to an arrayList
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        US = new Country();
        US.setName("US");
        US.setId("AmericaID");
        Countries.addCountry(US);
    }

    /**
     * this method checks whether addCountry works by checking whether the arrayList
     * size increased when the country was added
     */
    @Test
    public void testAddCountry() throws Exception {
        assertEquals(Countries.countries.size() == 1, true);
    }

    /**
     * this methods checks whether getCountry() works by adding a country to the arrayList
     * and then checking whether the index where the country was added in is not null.
     */
    @Test
    public void testGetCountryIndex() throws Exception {
        Countries.addCountry(US);
        assertNotNull(Countries.getCountry(1));
    }

    /**
     * this methods checks whether getCountry() works by checking if the method
     * was able to identify the country by its name and then return the country's Id
     * and by by checking if the method was able to identify the country by its ID and
     * then return the country's name.
     */
    @Test
    public void testGetCountry() throws Exception {
        assertEquals(Countries.getCountry("AmericaID").getName().equals("US"), true);
        assertEquals(Countries.getCountry("US").getId().equals("AmericaID"), true);
    }

    /**
     * this methods checks whether getCountries() works by checking whether the method is
     * capable of returning an arrayList.
     */
    @Test
    public void testGetCountries() throws Exception {
        assertNotNull(Countries.getCountries());
    }
}