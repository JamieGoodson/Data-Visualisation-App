package com.example.kos.teamheliotrope.backend;

import android.util.Log;

import java.util.ArrayList;

public class Country {
    public static final String TAG = "COUNTRY";
    String name;
    String id;
    ArrayList<Indicator> indicators;

    public Country() {
        name = "";
        id = "";
        indicators = new ArrayList<>();
    }

    public void addIndicator(Indicator indicator) {
        indicators.add(indicator);
    }

    public Indicator getIndicator(String id) {
        for (Indicator indicator : indicators) {
            if (indicator.getId().equals(id)) return indicator;
        }

        return null; // Does not exist
    }

    public ArrayList<Indicator> getIndicators() {
        return indicators;
    }

    public boolean doesIndicatorExist(String id) {
        if (indicators.isEmpty()) return false;

        for (Indicator indicator : indicators) {
            if (indicator.getId().equals(id)) return true;
        }
        return false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
