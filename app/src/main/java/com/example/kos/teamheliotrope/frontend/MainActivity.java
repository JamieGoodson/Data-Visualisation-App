package com.example.kos.teamheliotrope.frontend;

import com.example.kos.teamheliotrope.backend.CountryInfoThread;
import com.example.kos.teamheliotrope.backend.DataRetrieverThread;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.kos.teamheliotrope.R;
import com.example.kos.teamheliotrope.backend.Countries;
import com.example.kos.teamheliotrope.backend.Country;
import com.example.kos.teamheliotrope.backend.Indicator;
import com.example.kos.teamheliotrope.backend.Value;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.json.JSONArray;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    public static final String countryQuery = "http://api.worldbank.org/country?per_page=300&region=WLD&format=json";

    protected boolean hasInternetConnection;

    //Views from activity_main.xml that we need reference to
    PieChart chart;
    Button btnMarine,btnBiofuel,btnHydro,btnWind,btnSolar,btnGeothermal,btnWaste,btnBiogas;
    Spinner spCountries,spYear,spIndicators;
    TextView tvTotalEnergyConsumption,tvRenewableEnergyConsumption,tvFossilFuelEnergyConsumptionPanel,tvOtherEnergyConsumptionPanel;
    TextView tvIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        hasInternetConnection = false;

        chart = (PieChart) findViewById(R.id.mainChart);
        btnMarine = (Button) findViewById(R.id.btnMarine);
        btnBiofuel = (Button) findViewById(R.id.btnBiofuel);
        btnHydro = (Button) findViewById(R.id.btnHydro);
        btnWind = (Button) findViewById(R.id.btnWind);
        btnSolar = (Button) findViewById(R.id.btnSolar);
        btnGeothermal = (Button) findViewById(R.id.btnGeothermal);
        btnWaste = (Button) findViewById(R.id.btnWaste);
        btnBiogas = (Button) findViewById(R.id.btnBiogas);

        spCountries = (Spinner) findViewById(R.id.spCountries);
        spYear = (Spinner) findViewById(R.id.spYear);
        spIndicators = (Spinner) findViewById(R.id.spIndicators);

        tvTotalEnergyConsumption = (TextView) findViewById(R.id.tvTotalEnergyConsumption);
        tvRenewableEnergyConsumption = (TextView) findViewById(R.id.tvRenewableEnergyConsumption);
        tvFossilFuelEnergyConsumptionPanel = (TextView) findViewById(R.id.tvFossilFuelEnergyConsumptionPanel);
        tvOtherEnergyConsumptionPanel = (TextView) findViewById(R.id.tvOtherEnergyConsumptionPanel);

        tvIndicator = (TextView) findViewById(R.id.tvIndicator);

        //Testing internet connection
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                hasInternetConnection = hasActiveInternetConnection(MainActivity.this);
            }

            public boolean hasActiveInternetConnection(Context context) {
                if (isNetworkAvailable(context)) {
                    try {
                        HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                        urlc.setRequestProperty("User-Agent", "Test");
                        urlc.setRequestProperty("Connection", "close");
                        urlc.setConnectTimeout(1500);
                        urlc.connect();
                        return (urlc.getResponseCode() == 200);
                    } catch (IOException e) {
                        Log.d(TAG, "Error checking internet connection", e);
                    }
                } else {
                    Log.d(TAG, "No network available!");
                }
                return false;
            }

            private boolean isNetworkAvailable(Context context) {
                ConnectivityManager connectivityManager
                        = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check if it has Internet connection
        if (hasInternetConnection){
            initCountries();
            initData();
            initSpinners();
        }

        displayData(); // Comment out when not debugging

        //------------------------ATTENTION COMMENTED OUT AS IT WAS USING A BARCHART AND CAUSED A COMPILATION ERROR--------------------
        //------------------------------------------------------FIX WHEN POSSIBLE------------------------------------------------------
        //setupChart();
    }

    private void initSpinners(){
        List<String> spinnerArrayCountry =  new ArrayList<String>();

        for (Country country : Countries.getCountries()){
            spinnerArrayCountry.add(country.getName());
        }

        addDataToSpinner(spCountries, spinnerArrayCountry);

        spCountries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Country selectedCountry = Countries.getCountry(position);

                updateIndicatorSpinner(selectedCountry);

                updateData(selectedCountry, spYear.getSelectedItemPosition() + 1960);

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        List<String> spinnerArrayYear = new ArrayList<String>();

        for (int i = 1960; i <= 2015; ++i){
            spinnerArrayYear.add(String.valueOf(i));
        }

        addDataToSpinner(spYear,spinnerArrayYear);

        spYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateData(Countries.getCountry(spCountries.getSelectedItemPosition()), spYear.getSelectedItemPosition() + 1960);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    private void updateData(Country selectedCountry, int year){
        float totalValue = getValueOfIndicatorCountry(selectedCountry, "1.1_TOTAL.FINAL.ENERGY.CONSUM",year);
        if (totalValue != -1) {
            tvTotalEnergyConsumption.setText(String.valueOf(totalValue) + " KJ");
        } else {
            tvTotalEnergyConsumption.setText("No data available");
        }
        float renewableValue = getValueOfIndicatorCountry(selectedCountry,"EG.FEC.RNEW.ZS",year);
        if (renewableValue != -1) {
            tvRenewableEnergyConsumption.setText(String.valueOf(renewableValue) + "%");
        } else {
            tvRenewableEnergyConsumption.setText("No data available");
        }
        float fossilValue = getValueOfIndicatorCountry(selectedCountry,"EG.USE.COMM.FO.ZS",year);
        if (fossilValue != -1) {
            tvFossilFuelEnergyConsumptionPanel.setText(String.valueOf(fossilValue) + "%");
        } else {
            tvFossilFuelEnergyConsumptionPanel.setText("No data available");
        }
        float otherValue = 100;
        if (renewableValue != -1){
            otherValue -= renewableValue;
        }
        if (fossilValue != -1){
            otherValue -= fossilValue;
        }

        if (otherValue >= 0){
            tvOtherEnergyConsumptionPanel.setText(String.valueOf(otherValue) + "%");
        }else{
            tvOtherEnergyConsumptionPanel.setText("0%");
        }

        //TODO: Fill in PieChart
    }

    private void addDataToSpinner(Spinner spinner, List<String> spinnerArray) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void updateIndicatorSpinner(Country selectedCountry){
        List<String> spinnerArray = new ArrayList<String>();

        ArrayList<Indicator> countryIndicators = selectedCountry.getIndicators();

        for (int i = 0; i < countryIndicators.size(); ++i) {
            spinnerArray.add(countryIndicators.get(i).getTitle());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spIndicators.setAdapter(adapter);
    }

    private float getValueOfIndicatorCountry(Country selectedCountry,String indicatorID, int year){
        Indicator indicator = selectedCountry.getIndicator(indicatorID);
        if (indicator != null){
            Value value = indicator.getValue(String.valueOf(year));
            if (value != null){
                return value.getValue();
            }
        }
        return -1;
    }

    private void initCountries(){
        CountryInfoThread countryInfoThread = new CountryInfoThread(countryQuery);
        countryInfoThread.start();
        try {
            countryInfoThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * A test chart using hard-code values.
     */
    /* ------------------------ATTENTION COMMENTED OUT AS IT WAS USING A BARCHART AND CAUSED A COMPILATION ERROR--------------------
     * ------------------------------------------------------FIX WHEN POSSIBLE------------------------------------------------------
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

        // Combine X + Y values
        BarData barData = new BarData(new ArrayList<>(xVals), barDataSets);

        // Set data
        chart.setDescription(Countries.getCountry(0).getIndicator(indicatorId).getTitle());
        chart.setDescriptionTextSize(12);
        chart.setData(barData);
        chart.invalidate(); // Refresh
    }*/

    private void initData(){

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
        };

        // Create a pool of threads - limits number of threads to avoid JVM crashes
        ExecutorService executor = Executors.newFixedThreadPool(50);
        long startTime = System.nanoTime();

        // Iterate through each country
        Log.d(TAG, "Iterating through each country....");
        for (int i = 0; i < Countries.getCountries().size();++i) {

            Country country = Countries.getCountry(i);

            // Iterate through each indicator for this country
            for (String indicatorCode : indicatorCodes) {
                Runnable dataRetrieverThread = new DataRetrieverThread(country, country.getId(), indicatorCode);
                executor.execute(dataRetrieverThread);
            }
        }
        executor.shutdown();

        while (!executor.isTerminated()) {
            // Waits until all executor threads finished
        }

        Log.d(TAG, String.format("All threads finished (took %fs).", (System.nanoTime() - startTime) / Math.pow(10, 9)));
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
