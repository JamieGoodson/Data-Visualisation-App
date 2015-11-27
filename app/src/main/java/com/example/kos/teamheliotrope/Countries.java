package com.example.kos.teamheliotrope;

import java.util.ArrayList;

/**
 * Created by Jamie on 27/11/2015.
 */
public class Countries {
    ArrayList<Country> countries;

    public Countries() {
        countries = new ArrayList<>();
    }

    public void addCountry(Country country) {
        countries.add(country);
    }
}
