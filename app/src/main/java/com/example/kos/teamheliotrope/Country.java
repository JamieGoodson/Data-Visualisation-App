package com.example.kos.teamheliotrope;

import java.util.ArrayList;

/**
 * Created by Jamie on 27/11/2015.
 */
public class Country {
    ArrayList<Float> co2Emissions;

    public Country() {
        co2Emissions = new ArrayList<>();
    }

    public void addCO2Value(float value) {
       co2Emissions.add(value);
    }
}
