package com.example.kos.teamheliotrope.backend;

//~ JDK/Android Imports ========================================

import java.io.Serializable;

/**
 * Class that maintains Value information for specific year
 */
public class Value implements Serializable {
    public static final String TAG = "VALUE";
    String date;
    Float value;

    /**
     * Constructor
     * Initialises String date to a passed parameter
     * Initialises Float value to a passed parameter
     * @param date String date
     * @param value Float value
     */
    public Value(String date, Float value) {
        this.date = date;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%s | %s", date, value.toString());
    }

    /**
     * Returns the date of the Value object
     * @return String date
     */
    public String getDate() {
        return date;
    }

    /**
     * Returns the float value of the Value object
     * @return Float value
     */
    public Float getValue() {
        return value;
    }
}
