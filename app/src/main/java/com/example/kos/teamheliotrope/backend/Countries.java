package com.example.kos.teamheliotrope.backend;

import java.util.ArrayList;

/**
 * Created by Jamie on 27/11/2015.
 */
public class Countries {
    static ArrayList<Country> countries = new ArrayList<>();

    static public void addCountry(Country country) {
        countries.add(country);
    }

    static public Country getCountry(int i) {
        return countries.get(i);
    }

    static public ArrayList<Country> getCountries() {
        return countries;
    }
}
