package com.example.kos.teamheliotrope.frontend;

import com.example.kos.teamheliotrope.backend.DataRetrieverThread;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

import com.example.kos.teamheliotrope.R;
import com.example.kos.teamheliotrope.backend.Countries;
import com.example.kos.teamheliotrope.backend.Country;
import com.example.kos.teamheliotrope.backend.Indicator;
import com.example.kos.teamheliotrope.backend.Value;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    public static final int[] colors = {
            // Android color guidelines: https://www.google.com/design/spec/style/color.html
            Color.parseColor("#F44336"), // red
            Color.parseColor("#4CAF50"), // green
            Color.parseColor("#2196F3"), // blue
            Color.parseColor("#FF9800"), // orange
            Color.parseColor("#795548"), // brown
    };
    public static final String TAG = "MAIN_ACTIVITY";
    BarChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chart = (BarChart) findViewById(R.id.mainChart);

        initData();
        displayData(); // Comment out when not debugging
        setupChart();
    }

    /**
     * A test chart using hard-code values.
     */
    private void setupChart() {

        //TODO: If using one indicator OR one country, use pie chart. Else use another chart type.

        //TODO: Handle what happens when user requests values that do not exist for that indicator (e.g. no values for year 1993)

        // Get countries
        Log.d(TAG, "" + Countries.getCountries().size());
        Country[] countries = {
                Countries.getCountry("GB"),
                Countries.getCountry("US"),
                Countries.getCountry("CN")
        };

        String indicatorId = "EN.ATM.CO2E.PC"; // The indicator we want to show data for

        // X values
        ArrayList<String> xVals = new ArrayList<>(Arrays.asList(
                "1990",
                "2000",
                "2010"
            )
        );

        // Y values for each country
        ArrayList<BarDataSet> barDataSets = new ArrayList<>();

        for (int c=0; c<countries.length; c++) {
            Country country = countries[c];

            // DEBUG
            Log.d(TAG, country.getId());
            for (Indicator indicator : country.getIndicators()) {
                Log.d(TAG, indicator.getId());
            }

            ArrayList<BarEntry> barEntries = new ArrayList<>();

            for (int i=0; i<xVals.size(); i++) {
                barEntries.add(new BarEntry(country.getIndicator(indicatorId).getValue(xVals.get(i)).getValue(), i));
            }

            BarDataSet barDataSet = new BarDataSet(barEntries, country.getName());
            barDataSet.setColor(colors[c % colors.length]); // If possible, use different color for each country
            barDataSets.add(barDataSet);
        }

/*        ArrayList<BarEntry> yValsCountry2 = new ArrayList<>(Arrays.asList(
                new BarEntry(201f, 0), // Year 1
                new BarEntry(92f, 1), // Year 2
                new BarEntry(156f, 2) // Year 3
            )
        );

        BarDataSet country1 = new BarDataSet(yValsCountry1, "England");
        country1.setColor(Color.HSVToColor(new float[]{0, 40, 100}));
        BarDataSet country2 = new BarDataSet(yValsCountry2, "France");
        country2.setColor(Color.HSVToColor(new float[]{212, 40, 100}));*/

        // Combine X + Y values
        BarData barData = new BarData(new ArrayList<>(xVals), barDataSets);

        // Set data
        chart.setDescription(Countries.getCountry(0).getIndicator(indicatorId).getTitle());
        chart.setDescriptionTextSize(12);
        chart.setData(barData);
        chart.invalidate(); // Refresh
    }

    private void initData(){

        // http://api.worldbank.org/countries/GBR/indicators/5.1.1_TOTAL.CAPACITY?per_page=100&date=1960:2015&format=json

        // Important: Always use country IDs shown in the JSON and NOT from the API query builder
        String[] countryCodes = { // List of country codes to get data for
                "GB",  // UK
                "FR",  // France
                "ES",  // Spain
                "DE",  // Germany
                "US",  // USA
                "CN",  // China
                "RU"   // Russia
        };
        String[] indicatorCodes = {
                "1.1_TOTAL.FINAL.ENERGY.CONSUM", // Total final energy consumption (TFEC) (TJ)
                "2.1.1_SHARE.TRADBIO", // Solid biofuels for traditional uses share of TFEC (%)
                "2.1.10_SHARE.MARINE", // Marine energy share of TFEC (%)
                "2.1.2_SHARE.MODERNBIO", // Solid biofuels for modern uses share of TFEC (%)
                "2.1.3_SHARE.HYDRO", // Hydro energy share of TFEC (%)
                "2.1.4_SHARE.BIOFUELS", // Liquid biofuels share of TFEC (%)
                "2.1.5_SHARE.WIND", // Wind energy share of TFEC (%)
                "2.1.6_SHARE.SOLAR", // Solar energy share of TFEC (%)
                "2.1.7_SHARE.GEOTHERMAL", // Geothermal energy share of TFEC (%)
                "2.1.8_SHARE.WASTE", // Waste energy share of TFEC (%)
                "2.1.9_SHARE.BIOGAS", // Biogas share of TFEC (%)
                "EG.USE.COMM.FO.ZS", // Fossil fuel energy consumption (% of total)
                "EG.FEC.RNEW.ZS", // Renewable energy consumption (% of total final energy consumption)
                "EN.ATM.CO2E.PC" // CO2 emissions (metric tons per capita)
        };


        // Create a pool of threads - limits number of threads to avoid JVM crashes
        ExecutorService executor = Executors.newFixedThreadPool(20);
        long startTime = System.nanoTime();

        // Iterate through each country
        Log.d(TAG, "Iterating through each country....");
        for (String countryCode : countryCodes) {
            Country country = new Country();
            Countries.addCountry(country);

            // Iterate through each indicator for this country
            for (String indicatorCode : indicatorCodes) {
                Runnable dataRetrieverThread = new DataRetrieverThread(this, country, countryCode, indicatorCode);
                executor.execute(dataRetrieverThread);
            }
        }
        executor.shutdown();

        while (!executor.isTerminated()) { // Waits until all executor threads finished
        }

        Log.d(TAG, String.format("All threads finished (took %fs).", (System.nanoTime() - startTime)/Math.pow(10,9)));
    }

    public void displayData() {
        for (Country country : Countries.getCountries()) {
            Log.d(TAG, String.format("====== %s ======", country.getName()));

            for (Indicator indicator : country.getIndicators()) {
                Log.d(TAG, String.format("=== %s ===", indicator.getTitle()));

                for (Value value : indicator.getValues()) {
                    Log.d(TAG, String.format("%s | %s", value.getDate(), value.getValue()));
                }
            }
        }
        Log.d(TAG, "END OF DATA");
    }
}
