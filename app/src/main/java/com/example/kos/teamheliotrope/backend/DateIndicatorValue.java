package com.example.kos.teamheliotrope.backend;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jamiegoodson on 27/11/2015.
 */
public class DateIndicatorValue {
    Date date;
    String indicator;
    Float value;

    public DateIndicatorValue(Date date, String indicator, Float value) {
        this.date = date;
        this.indicator = indicator;
        this.value = value;
    }

    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
        return String.format("%s | %s | %s", dateFormat.format(date), indicator, (value == null) ? "null" : value.toString());
    }

    public String getIndicator() {
        return indicator;
    }

    public Date getDate() {
        return date;
    }

    public Float getValue() {
        return value;
    }
}
