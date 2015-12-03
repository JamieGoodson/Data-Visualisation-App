package com.example.kos.teamheliotrope.backend;

import android.util.Log;

import com.example.kos.teamheliotrope.frontend.MainActivity;

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

public class DataRetrieverThread extends Thread {
    final static String TAG = "RETRIEVER_THREAD";
    JSONArray jsonArray;
    Country country;
    String query;
    String countryCode;
    String indicatorCode;


    public DataRetrieverThread(Country country, String countryCode, String indicatorCode) {
        this.country = country;
        this.indicatorCode = indicatorCode;
        this.countryCode = countryCode;
        this.query = "http://api.worldbank.org/countries/" + countryCode + "/indicators/" + indicatorCode + "?per_page=100&date=1960:2015&format=json";
    }

    @Override
    public void run() {
        Log.d(TAG, String.format("Starting thread for %s, %s (%s)...", countryCode, indicatorCode, query));
        jsonArray = fetchJSONArray();
        processJSONArray();
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
            Log.e(TAG, String.format("Error with %s, %s", countryCode, indicatorCode));
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

    public void processJSONArray() {
        try {
            JSONArray dataArray = jsonArray.getJSONArray(1);
            JSONObject dataForThisYear;

            String date;
            String value;

            JSONObject firstObject = dataArray.getJSONObject(0); // Use to get initial data

            // Setup country
            JSONObject jsonCountry = firstObject.getJSONObject("country");
            country.setName(jsonCountry.getString("value"));
            country.setId(jsonCountry.getString("id"));

            // Initialise indicator object + add to country
            JSONObject jsonIndicator = firstObject.getJSONObject("indicator");
            Indicator indicator = new Indicator(jsonIndicator.getString("id"), jsonIndicator.getString("value"));
            country.addIndicator(indicator);

            // Begin adding values
            for (int i = 0; i < dataArray.length(); i++) {
                dataForThisYear = dataArray.getJSONObject(i);
                //Log.d(TAG, dataForThisYear.toString());

                date = dataForThisYear.getString("date");
                value = dataForThisYear.getString("value");

                if (!value.equals("null")) { // Skip null values
                    indicator.addValue(new Value(date, Float.parseFloat(value)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONArray getJsonArray() {
        return jsonArray;
    }
}
