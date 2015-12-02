package com.example.kos.teamheliotrope.backend;

import java.text.SimpleDateFormat;

public class Value {
    public static final String TAG = "VALUE";
    String date;
    Float value;

    public Value(String date, Float value) {
        this.date = date;
        this.value = value;
    }

    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
        return String.format("%s | %s", dateFormat.format(date), value.toString());
    }

    public String getDate() {
        return date;
    }

    public Float getValue() {
        return value;
    }
}
