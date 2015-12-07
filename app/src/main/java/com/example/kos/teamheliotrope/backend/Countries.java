package com.example.kos.teamheliotrope.backend;

import java.util.ArrayList;

/**
 * Static class that maintains a collection of all the Countries getting data for
 */
public class Countries {

   public static ArrayList<Country> countries = new ArrayList<>();

    /**
     * Adds a country to the collection of countries
     * @param country Country to be added
     */
    static public void addCountry(Country country) {
        countries.add(country);
    }

    /**
     * Returns a Country at a specific index
     * @param i index of Country to retrieve
     * @return Country at index i
     */
    static public Country getCountry(int i) {
        return countries.get(i);
    }

    /**
     * Return a Country by it's name or by it's ID
     * @param idOrName Countries name or ID
     * @return Country with that specific name or ID. Returns null if that country doesn't exist
     */
    static public Country getCountry(String idOrName) {
        for (Country country : countries) {
            if (country.getId().equals(idOrName) || country.getName().equals(idOrName)) return country;
        }
        return null;
    }

    /**
     * Returns a collection of all the countries held by the collection
     * @return Collection of Country objects
     */
    static public ArrayList<Country> getCountries() {
        return countries;
    }
}
