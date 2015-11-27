package com.example.kos.teamheliotrope;

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
import java.text.DecimalFormat;

/**
 * Created by jamiegoodson on 26/11/2015.
 */
public class DataRetrieverThread extends Thread {
    final static String TAG = "RETRIEVER_THREAD";
    MainActivity mainActivity;
    JSONArray jsonArray;
    Country country;
    String query;
    String countryCode;
    String indicatorCode;


    public DataRetrieverThread(MainActivity mainActivity, Country country, String countryCode, String indicatorCode) {
        this.mainActivity = mainActivity;
        this.country = country;
        this.indicatorCode = indicatorCode;
        this.countryCode = countryCode;
        this.query = "http://api.worldbank.org/countries/" + countryCode + "/indicators/" + indicatorCode + "?per_page=100&date=1960:2015&format=json";
    }

    @Override
    public void run() {
        Log.d(TAG, "Starting thread...");
        jsonArray = generateJSONArray();
        mainActivity.displayData(jsonArray);
    }

    private JSONArray generateJSONArray() {
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
        }catch (IOException e){
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

    public void addDataToCountry() {
        try {
            JSONArray dataArray = jsonArray.getJSONArray(1);
            JSONObject dataForThisYear;
            String year;
            String value;

            for (int i = 0; i < dataArray.length(); ++i) {
                dataForThisYear = dataArray.getJSONObject(i);

                if (indicatorCode.equals("EN.ATM.CO2E.PC")) {
                    country.addCO2Value(Float.parseFloat(dataForThisYear.getString("value")));
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
