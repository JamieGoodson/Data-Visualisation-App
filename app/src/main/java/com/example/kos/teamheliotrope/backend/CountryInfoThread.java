package com.example.kos.teamheliotrope.backend;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by tuffail on 03/12/15.
 * Retrieves JSON Array from the World Data Bank and processes the JSON information, producing Country objects
 */
public class CountryInfoThread extends Thread {

    String query;
    JSONArray jsonArray;

    /**
     * The website query that the countries should be retrieved from
     * @param query website containing country data
     */
    public CountryInfoThread(String query){
        this.query = query;
    }

    /**
     * Starts the thread, retrieves JSON data and creates Country objects
     */
    @Override
    public void run() {
        jsonArray = fetchJSONArray();
        processJSONArray();
    }

    private void processJSONArray() {
        try {
            JSONArray dataArray = jsonArray.getJSONArray(1);
            JSONObject countryData;

            for (int i = 0; i < dataArray.length(); i++){
                countryData = dataArray.getJSONObject(i);
                String countryLongitude = countryData.getString("longitude");
                String countryLatitude = countryData.getString("latitude");

                if (countryLatitude.length() != 0 && countryLongitude.length() != 0){
                    Country country = new Country();
                    country.setId(countryData.getString("iso2Code"));
                    country.setName(countryData.getString("name"));
                    Countries.addCountry(country);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // TODO: Handle what happens when device isn't connected to internet. Currently causes app to crash.
    private JSONArray fetchJSONArray() {
        // open connection
        URL url = null;

        try {
            url = new URL(query);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        InputStream inStream = null;
        try {
            inStream = url.openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        DataInputStream dataInStream = null;
        if (inStream != null) {
            dataInStream = new DataInputStream(inStream);
        }

        // buffer to hold chunks as they are read
        byte[] buffer = new byte[1024];
        int bufferLength;

        // string builder to hold output
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // read from stream
        try{
            while ((bufferLength = dataInStream.read(buffer)) > 0){
                // write buffer into output
                output.write(buffer,0,bufferLength);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        try {
            return new JSONArray(output.toString("UTF-8"));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
            return null;
        }
    }

}
