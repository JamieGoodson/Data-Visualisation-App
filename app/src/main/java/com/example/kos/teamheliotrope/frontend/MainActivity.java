package com.example.kos.teamheliotrope.frontend;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.example.kos.teamheliotrope.R;
import com.example.kos.teamheliotrope.backend.Countries;
import com.example.kos.teamheliotrope.backend.Country;
import com.example.kos.teamheliotrope.backend.DataRetrieverThread;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    BarChart chart;
    String output;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chart = (BarChart) findViewById(R.id.mainChart);
        textView = (TextView) findViewById(R.id.textView);
        output = "";

        setupChart();
        initData();
    }

    private void setupChart() {

        // X values
        ArrayList<String> xVals = new ArrayList<>(Arrays.asList(
                "Year 1",
                "Year 2",
                "Year 3"
            )
        );

        // Y values
        ArrayList<BarEntry> yValsCountry1 = new ArrayList<>(Arrays.asList(
                new BarEntry(98f, 0), // Year 1
                new BarEntry(121f, 1), // Year 2
                new BarEntry(42f, 2) // Year 3
            )
        );

        ArrayList<BarEntry> yValsCountry2 = new ArrayList<>(Arrays.asList(
                new BarEntry(201f, 0), // Year 1
                new BarEntry(92f, 1), // Year 2
                new BarEntry(156f, 2) // Year 3
            )
        );

        BarDataSet country1 = new BarDataSet(yValsCountry1, "England");
        country1.setColor(Color.HSVToColor(new float[]{0, 40, 100}));
        BarDataSet country2 = new BarDataSet(yValsCountry2, "France");
        country2.setColor(Color.HSVToColor(new float[]{212, 40, 100}));

        // Combine X + Y values
        BarData barData = new BarData(new ArrayList<>(xVals), Arrays.asList(country1, country2));

        // Set data
        chart.setData(barData);
        chart.invalidate(); // Refresh
    }

    private void initData(){

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
                for (Country country : Countries.getCountries()) {
                    outputLine(country.getName());
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
