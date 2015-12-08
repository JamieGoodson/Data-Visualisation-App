package com.example.kos.teamheliotrope.backend;

import java.io.Serializable;
import java.text.SimpleDateFormat;

public class Value implements Serializable {
    public static final String TAG = "VALUE";
    String date;
    Float value;

    public Value(String date, Float value) {
        this.date = date;
        this.value = value;
    }

    public String toString() {
        return String.format("%s | %s", date, value.toString());
    }

    public String getDate() {
        return date;
    }

    public Float getValue() {
        return value;
    }
}
