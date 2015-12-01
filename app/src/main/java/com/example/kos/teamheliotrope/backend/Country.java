package com.example.kos.teamheliotrope.backend;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Jamie on 27/11/2015.
 */
public class Country {
    public static final String TAG = "COUNTRY";
    String name;
    String id;
    ArrayList<DateIndicatorValue> dateIndicatorValues;

    public Country() {
        dateIndicatorValues = new ArrayList<>();
    }

    public void addDateIndicatorValue(DateIndicatorValue dateIndicatorValue) {
        dateIndicatorValues.add(dateIndicatorValue);
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

    public void logDateIndicatorValues() {
        String string = "";

        for (DateIndicatorValue div : dateIndicatorValues) {
            string += String.format("%s\n", div.toString());
        }

        Log.d(TAG, string);
    }
}
