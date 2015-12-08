package com.example.kos.teamheliotrope.backend;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class Country implements Serializable{
    public static final String TAG = "COUNTRY";
    String name;
    String id;
    ArrayList<Indicator> indicators;
    int nullValueCount = 0;

    public Country() {
        name = "";
        id = "";
        indicators = new ArrayList<>();
    }

    public void addIndicator(Indicator indicator) {
        boolean contains = false;

        // Check if indicator already exists
        for (int i =0; i < indicators.size(); ++i){
            if (indicators.get(i).getId().equals(indicator.getId())){
                contains = true;
                break;
            }
        }

        // If indicator doesn't exist, add the indicator
        if (contains == false){
            indicators.add(indicator);
        }
    }

    public Indicator getIndicator(String id) {
        for (int i = 0; i < indicators.size(); ++i) {
            if (indicators.get(i).getId().equals(id)) return indicators.get(i);
        }
        return null; // Does not exist
    }

    public ArrayList<Indicator> getIndicators() {
        return indicators;
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

    public int getNullValueCount() {
        return nullValueCount;
    }
}
