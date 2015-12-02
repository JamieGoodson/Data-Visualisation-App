package com.example.kos.teamheliotrope.backend;

import java.util.ArrayList;

public class Countries {
    static ArrayList<Country> countries = new ArrayList<>();

    static public void addCountry(Country country) {
        countries.add(country);
    }

    static public Country getCountry(int i) {
        return countries.get(i);
    }

    static public Country getCountry(String id) {
        for (Country country : countries) {
            if (country.getId().equals(id)) return country;
        }
        return null;
    }

    static public ArrayList<Country> getCountries() {
        return countries;
    }
}
