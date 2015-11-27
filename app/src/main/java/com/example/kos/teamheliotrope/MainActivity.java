package com.example.kos.teamheliotrope;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    String output;
    TextView textView;
    Countries countries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    protected void init(){
        textView = (TextView) findViewById(R.id.textView);
        output = "";

        // http://api.worldbank.org/countries/GBR/indicators/5.1.1_TOTAL.CAPACITY?per_page=100&date=1960:2015&format=json


        String[] countryCodes = { // List of country codes to get data for
                "GBR",  // UK
                "FRA",  // France
                "ESP",  // Spain
                "DEU",  // Germany
                "USA",  // USA
                "CHN",  // China
                "RUS"   // Russia
        };
        String[] indicatorCodes = {
                "EN.ATM.CO2E.PC" // CO2 emissions (metric tons per capita)
        };
        for (String countryCode : countryCodes) {
            Country country = new Country();
            for (String indicatorCode : indicatorCodes) {
                new DataRetrieverThread(this, country, countryCode, indicatorCode).start();
            }
        }
    }

    public void displayData(final JSONArray websiteData) {
        if (websiteData == null) return;

        // try to read through data
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {

                    JSONArray electricityList = websiteData.getJSONArray(1);
                    JSONObject electricity;
                    JSONObject country;
                    String year, value;

                    for (int i = 0; i < electricityList.length(); ++i) {
                        electricity = electricityList.getJSONObject(i);

                        country = electricity.getJSONObject("country");
                        year = electricity.getString("date");
                        value = electricity.getString("value");

                        // output name
                        if (!value.equals("null")) {
                            double val = Double.parseDouble(value);
                            DecimalFormat df = new DecimalFormat("##.00");
                            outputLine("Country: " + country.getString("value") + " - Year: " + year + "  - Value: " + df.format(val));
                        }

                    }

                } catch (JSONException e) {
                    // something went wrong!
                    e.printStackTrace();
                    outputLine("Something went wrong!");
                }
            }
        });
    }

    public void clearOutput() {
        output = "";
        textView.setText("");
    }

    public void outputLine(String s) {
        output += " <strong>" + s + "</strong><br/>";
        textView.setText(Html.fromHtml(output));
    }
}
