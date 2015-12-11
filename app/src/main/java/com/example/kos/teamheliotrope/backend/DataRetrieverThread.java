package com.example.kos.teamheliotrope.backend;

//~ JDK/Android Imports ========================================

import android.content.Intent;
import com.example.kos.teamheliotrope.frontend.MainActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Retrieves JSON Array from the World Data Bank and processes the JSON information on a specific Country
 * for a specific indicator code
 */
public class DataRetrieverThread extends Thread {
    final static String TAG = "RETRIEVER_THREAD";
    MainActivity activity;
    JSONArray jsonArray;
    Country country;
    String query;
    String countryCode;
    String indicatorCode;

    /**
     * Maintains reference to the MainActivity, the Country to get data on, the CountryCode of the
     * country to retrieve data for, Indicator code of the data to retrieve for that country
     * @param activity MainActivity reference
     * @param country Country to fetch data for and store into
     * @param countryCode Country code of the country
     * @param indicatorCode Indicator to fetch data on for that specific country
     */
    public DataRetrieverThread(MainActivity activity, Country country, String countryCode, String indicatorCode) {
        this.activity = activity;
        this.country = country;
        this.indicatorCode = indicatorCode;
        this.countryCode = countryCode;
        // IMPORTANT! Make sure the per page part of the JSONQuery is set to 13888 as that's the maximum number of results we could receive in a page
        this.query = "http://api.worldbank.org/countries/" + this.countryCode + "/indicators/" + this.indicatorCode + "?per_page=13888&date=1960:2015&format=json";
    }

    /**
     * Runs the thread retrieving data for a specific country and indicator
     */
    @Override
    public void run() {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.loadingDialog.setMessage("Fetching data for " + country.getName());
            }
        });

        jsonArray = fetchJSONArray();
        processJSONArray();
    }

    /**
     * This method initializes the JSONArray with the all JSON objects
     * retrived from the query
     * @return
     */
    public JSONArray fetchJSONArray() {
        try{
            // open connection
            URL url = new URL(query);

            InputStream inStream = url.openStream();

            DataInputStream dataInStream = null;

            if (inStream != null) {
                dataInStream = new DataInputStream(inStream);

                // buffer to hold chunks as they are read
                byte[] buffer = new byte[1024];
                int bufferLength;

                // string builder to hold output
                ByteArrayOutputStream output = new ByteArrayOutputStream();

                while ((bufferLength = dataInStream.read(buffer)) > 0){
                    // write buffer into output
                    output.write(buffer,0,bufferLength);
                }
                return new JSONArray(output.toString("UTF-8"));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        restartApplication();
        return null;
    }

    /**
     * this method checks whether the declared country has an indicator and if it does not it creates one
     * for it and then the method creates an object for every object that is in the dataArray and if the
     * created object has a value and a data which is between dataMin and dataMax the current object's
     * value will be stored in the indicator as a Value in the values arrayList otherwise the nullValueCount
     * will increase. if the current country has more then 5 nullCounts for every null value it receive from
     * JSON objects the country will be removed from the countries arrayList.
     */
    public void processJSONArray() {
        try {
            JSONArray dataArray = jsonArray.getJSONArray(1);
            JSONObject dataForThisYear;

            String date;
            String value;

            JSONObject firstObject = dataArray.getJSONObject(0); // Use to get initial data

            // Initialise indicator object + add to country
            JSONObject jsonIndicator = firstObject.getJSONObject("indicator");
            Indicator indicator;
            if (country.getIndicator(jsonIndicator.getString("id")) == null){
                indicator = new Indicator(jsonIndicator.getString("id"), jsonIndicator.getString("value"));
                country.addIndicator(indicator);
            }else{
                indicator = country.getIndicator(jsonIndicator.getString("id"));
            }

            // Begin adding values
            for (int i = 0; i < dataArray.length(); i++) {
                dataForThisYear = dataArray.getJSONObject(i);
                date = dataForThisYear.getString("date");
                value = dataForThisYear.getString("value");

                if (((date.length() == 4) && (Integer.parseInt(date) >= MainActivity.dateMin) && (Integer.parseInt(date) <= MainActivity.dateMax))) { // Skip pre-1990/post-2012 values (to reduces holes in data)
                    if (!value.equals("null")) {
                        indicator.addValue(new Value(date, Float.parseFloat(value)));
                    } else {
                        country.nullValueCount = country.nullValueCount + 1;
                    }
                }

                // Remove country from Countries if it has too many null values.
                if (country.nullValueCount > 5) {
                    Countries.removeCountry(country);
                    break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            restartApplication();
        }catch (Exception e){
            e.printStackTrace();
            restartApplication();
        }
    }

    private void restartApplication(){
        //Restarts application
        Intent i = activity.getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( activity.getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(i);
    }

    /**
     * Returns the JSON Array data
     * @return JSON Array data
     */
    public JSONArray getJsonArray() {
        return jsonArray;
    }
}
