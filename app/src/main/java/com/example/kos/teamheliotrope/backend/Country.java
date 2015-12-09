package com.example.kos.teamheliotrope.backend;

//~ JDK/Android Imports ========================================

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class that maintains information about a specific country
 */
public class Country implements Serializable {
    public static final String TAG = "COUNTRY";
    String name;
    String id;
    ArrayList<Indicator> indicators;
    int nullValueCount = 0;

    /**
     * Constructor
     * Initialises String name and id as well as ArrayList of indicators
     */
    public Country() {
        name = "";
        id = "";
        indicators = new ArrayList<>();
    }

    /**
     * Adds an indicator object relevant to the country object
     * @param indicator Indicator to be added
     */
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

    /**
     * Returns Indicator object by its id
     * @param id Indicator id as it appears in json
     * @return Indicator object. Returns null if the object doesn't exist
     */
    public Indicator getIndicator(String id) {
        for (int i = 0; i < indicators.size(); ++i) {
            if (indicators.get(i).getId().equals(id)) return indicators.get(i);
        }
        return null; // Does not exist
    }

    /**
     * Returns an ArrayList of Indicator objects
     * @return ArrayList of Indicator Objects
     */
    public ArrayList<Indicator> getIndicators() {
        return indicators;
    }

    /**
     * Sets the Name of a specific country for the Country object
     * @param name String name as it appears in json
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets Id, which is 2-letter ISO 3166-1 alpha-2 code, for a specific country, eg. "US"
     * @param id String id as it appears in json
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the Name of the Country
     * @return String name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the Id,2-letter ISO code, of the Country
     * @return String id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the amount of null values that appear in json
     * @return Integer value representing amount of null values
     */
    public int getNullValueCount() {
        return nullValueCount;
    }
}
