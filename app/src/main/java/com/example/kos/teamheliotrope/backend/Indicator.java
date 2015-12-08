package com.example.kos.teamheliotrope.backend;

import java.io.Serializable;
import java.util.ArrayList;

public class Indicator implements Serializable {
    public static final String TAG = "INDICATOR";
    String id;
    String title;
    ArrayList<Value> values;

    public Indicator(String id, String title) {
        this.id = id;
        this.title = title;
        this.values = new ArrayList<>();
    }

    /**
     * Get value by date (e.g. "2009")
     * @param date Year value
     * @return Value object (or null if not found)
     */
    public Value getValue(String date) {
        for (Value value : values) {
            if (value.getDate().equals(date)) return value;
        }

        return null;
    }

    public ArrayList<Value> getValues() {
        return values;
    }

    public void addValue(Value value) {
        values.add(value);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
