package com.example.kos.teamheliotrope.backend;

//~ JDK/Android Imports ========================================

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class that maintains information about specific indicator
 */
public class Indicator implements Serializable {
    public static final String TAG = "INDICATOR";
    String id;
    String title;
    ArrayList<Value> values;

    /**
     * Constructor.
     * Initialises String id and String title to the passed parameters
     * Initialises an empty ArrayList of Value objects
     * @param id Passed string value of indicator id
     * @param title Passed string value of indicator title
     */
    public Indicator(String id, String title) {
        this.id = id;
        this.title = title;
        this.values = new ArrayList<>();
    }

    /**
     * Get value by date (e.g. "2009")
     * @param date Year value
     * @return Value object. Returns null if object is not found
     */
    public Value getValue(String date) {
        for (Value value : values) {
            if (value.getDate().equals(date)) return value;
        }

        return null;
    }

    /**
     * Returns an ArrayList of Value objects
     * @return ArrayList of Value objects
     */
    public ArrayList<Value> getValues() {
        return values;
    }

    /**
     * Adds a
     * @param value
     */
    public void addValue(Value value) {
        values.add(value);
    }

    /**
     * Returns id of an Indicator object
     * @return String Id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns title of an Indicator object
     * @return String title
     */
    public String getTitle() {
        return title;
    }
}
