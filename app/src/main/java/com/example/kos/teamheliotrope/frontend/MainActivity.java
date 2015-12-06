package com.example.kos.teamheliotrope.frontend;

import com.example.kos.teamheliotrope.backend.CountryInfoThread;
import com.example.kos.teamheliotrope.backend.DataRetrieverThread;

import android.app.AlertDialog;
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
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.example.kos.teamheliotrope.R;
import com.example.kos.teamheliotrope.backend.Countries;
import com.example.kos.teamheliotrope.backend.Country;
import com.example.kos.teamheliotrope.backend.Indicator;
import com.example.kos.teamheliotrope.backend.Value;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
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
            Color.parseColor("#009688"), // teal
            Color.parseColor("#CDDC39"), // lime
            Color.parseColor("#FFEB3B"), // yellow
            Color.parseColor("#E91E63"), // pink
            Color.parseColor("#9C27B0"), // purple
            Color.parseColor("#9E9E9E"), // grey
            Color.parseColor("#3F51B5") // indigo
    };
    public static final String TAG = "MAIN_ACTIVITY";
    public static final String countryQuery = "http://api.worldbank.org/country?per_page=300&region=WLD&format=json";
    private final String[] indicatorIds = {
            "1.1_TOTAL.FINAL.ENERGY.CONSUM", // Total final energy consumption (TFEC) (TJ)
            "EG.FEC.RNEW.ZS", // Renewable energy consumption (% of total final energy consumption)
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
            "EG.USE.COMM.CL.ZS" // Alternative and nuclear energy (% of total energy use)
    };

    protected boolean hasInternetConnection;

    //Views from activity_main.xml that we need reference to
    PieChart chart;
    EnergyButton[] energyButtons;
    Spinner spCountries,spYear,spIndicators;
    TextView tvTotalEnergyConsumption,tvRenewableEnergyConsumption,tvFossilFuelEnergyConsumptionPanel,tvOtherEnergyConsumptionPanel;
    TextView tvIndicator;

    public AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        hideSystemUi();

        hasInternetConnection = false;

        chart = (PieChart) findViewById(R.id.mainChart);

        energyButtons = new EnergyButton[] {
                new EnergyButton((Button) findViewById(R.id.btnFossil), "EG.USE.COMM.FO.ZS"),
                new EnergyButton((Button) findViewById(R.id.btnNuclear), "EG.USE.COMM.CL.ZS"),
                new EnergyButton((Button) findViewById(R.id.btnMarine), "2.1.10_SHARE.MARINE"),
                new EnergyButton((Button) findViewById(R.id.btnBiofuel), "2.1.4_SHARE.BIOFUELS"),
                new EnergyButton((Button) findViewById(R.id.btnHydro), "2.1.3_SHARE.HYDRO"),
                new EnergyButton((Button) findViewById(R.id.btnWind), "2.1.5_SHARE.WIND"),
                new EnergyButton((Button) findViewById(R.id.btnSolar), "2.1.6_SHARE.SOLAR"),
                new EnergyButton((Button) findViewById(R.id.btnGeothermal), "2.1.7_SHARE.GEOTHERMAL"),
                new EnergyButton((Button) findViewById(R.id.btnWaste), "2.1.8_SHARE.WASTE"),
                new EnergyButton((Button) findViewById(R.id.btnBiogas), "2.1.9_SHARE.BIOGAS")
        };

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

        loadingDialog = new AlertDialog.Builder(this).setTitle("Loading...").setMessage("Initialising...").setCancelable(false).show();

        new Thread() {
            @Override
            public void run() {
                // Check if it has Internet connection
                if (hasInternetConnection){
                    initCountries();
                    initData();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initSpinners();
                            setupChart();

                            chart.getLegend().setEnabled(false);
                            chart.setDescription("*values are % of TFEC");
                            chart.setDescriptionTextSize(20);
                            //chart.setCenterText("Test");
                            //chart.setCenterTextSize(40);

                            loadingDialog.dismiss();
                        }
                    });
                }

                //displayData(); // Comment out when not debugging
            }
        }.start();
    }

    /**
     * Toggles button by adjusting transparency (but does not actually disable). Chart is then reinitialised.
     * @param v A button
     */
    public void toggleButton(View v) {
        Button button = (Button) v;
        if (button.getAlpha() == 1) {
            button.setAlpha(0.5f);
        } else {
            button.setAlpha(1);
        }
        setupChart();
    }

    private void initSpinners(){
        List<String> spinnerArrayCountry =  new ArrayList<String>();

        for (Country country : Countries.getCountries()){
            spinnerArrayCountry.add(country.getName());
        }

        addDataToSpinner(spCountries, spinnerArrayCountry);
        spCountries.setSelection(getSpinnerIndexOf(spCountries, "United Kingdom")); // Set default selection

        spCountries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstLoad = true;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Country selectedCountry = Countries.getCountry(position);

                updateIndicatorSpinner(selectedCountry);

                updateData(selectedCountry, spYear.getSelectedItemPosition() + 1990);

                if (firstLoad) {
                    firstLoad = false;
                } else {
                    setupChart();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        List<String> spinnerArrayYear = new ArrayList<String>();

        for (int i = 1990; i <= 2012; ++i){
            spinnerArrayYear.add(String.valueOf(i));
        }

        addDataToSpinner(spYear,spinnerArrayYear);
        spYear.setSelection(getSpinnerIndexOf(spYear, "2009")); // Set default selection

        spYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstLoad = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateData(Countries.getCountry(spCountries.getSelectedItemPosition()), spYear.getSelectedItemPosition() + 1990);

                if (firstLoad) {
                    firstLoad = false;
                } else {
                    setupChart();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    private void updateData(Country selectedCountry, int year){
        float totalValue = getValueOfIndicatorCountry(selectedCountry, "1.1_TOTAL.FINAL.ENERGY.CONSUM",year);
        if (totalValue != -1) {
            tvTotalEnergyConsumption.setText(String.format("%d", Math.round(totalValue)) + " kJ");
        } else {
            tvTotalEnergyConsumption.setText("No data");
        }
        float renewableValue = getValueOfIndicatorCountry(selectedCountry,"EG.FEC.RNEW.ZS",year);
        if (renewableValue != -1) {
            tvRenewableEnergyConsumption.setText(String.format("%.2f", renewableValue) + "%");
        } else {
            tvRenewableEnergyConsumption.setText("No data");
        }
        float fossilValue = getValueOfIndicatorCountry(selectedCountry,"EG.USE.COMM.FO.ZS",year);
        if (fossilValue != -1) {
            tvFossilFuelEnergyConsumptionPanel.setText(String.format("%.2f", fossilValue) + "%");
        } else {
            tvFossilFuelEnergyConsumptionPanel.setText("No data");
        }
        float otherValue = 100;
        if (renewableValue != -1){
            otherValue -= renewableValue;
        }
        if (fossilValue != -1){
            otherValue -= fossilValue;
        }

        if (otherValue >= 0){
            tvOtherEnergyConsumptionPanel.setText(String.format("%.2f", otherValue) + "%");
        }else{
            tvOtherEnergyConsumptionPanel.setText("0%");
        }

        //TODO: Fill in PieChart
    }

    private void addDataToSpinner(Spinner spinner, List<String> spinnerArray) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.title_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private int getSpinnerIndexOf(Spinner spinner, String value) {
        SpinnerAdapter adapter = spinner.getAdapter();

        for (int i=0; i<adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(value)) {
                return i;
            }
        }

        return -1;
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

    private void setupChartTest() {
        // X values
        String[] xVals = {
                "Protein",
                "Carbohydrates",
                "Fats"
        };

        int[] dummyValues = {10,50,30};

        // Y values
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i=0; i<xVals.length; i++) {

            entries.add(new Entry(dummyValues[i], i));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Test data set");
        dataSet.setColors(colors);
        PieData data = new PieData(xVals, dataSet);
        chart.setData(data);
    }

    private void setupChart() {
        Country country = Countries.getCountry(spCountries.getSelectedItem().toString());
        Log.d(TAG, "Country: " + country.getName());
        String date = spYear.getSelectedItem().toString();
        Log.d(TAG, "Date: "+ date);

        // Build a list of all selected indicator ids
        ArrayList<String> selectedIndicatorIds = new ArrayList<>();
        for (EnergyButton energyButton : energyButtons) {
            if (energyButton.getButton().getAlpha() == 1) {
                selectedIndicatorIds.add(energyButton.getIndicatorId());
            }
        }

        // X values - Setup list of indicators we want to use for chart
        //TODO: Add segment to pie chart that displays percentage we didn't have data for (null values)
        //TODO: Don't bother displaying values equal to 0.

        ArrayList<Float> values = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();
        for (Indicator indicator : country.getIndicators()) {
            String indicatorId = indicator.getId();
            if (selectedIndicatorIds.contains(indicatorId)) {
                String indicatorTitle = indicator.getTitle();
                Value indicatorValue = indicator.getValue(date);

                indicatorTitle = indicator.getTitle();

                if (indicatorTitle.length() > 12) {
                    xVals.add(indicatorTitle.substring(0, 12) + "...");
                } else {
                    xVals.add(indicatorTitle);
                }

                // Skip null values
                float value = 0f;
                if (indicatorValue != null) {
                    value = indicatorValue.getValue();
                    values.add(value);
                } else {
                    values.add(value);
                    Log.d(TAG, "Null value found. setting to 0.");
                }

                Log.d(TAG, String.format("Indicator: %s | Value at this date: %f | Number of values: %d", indicatorTitle, value, indicator.getValues().size()));
            }
        }

        // Y values - Get values for each indicator
        float total = 0f;
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i=0; i<xVals.size(); i++) {
            float value = values.get(i);
            entries.add(new Entry(value, i));

            total += value;
        }

        Log.d(TAG, "Total value for pie chart: " + total);

        PieDataSet dataSet = new PieDataSet(entries, "Test data set");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(14);
        PieData data = new PieData(xVals, dataSet);
        chart.setData(data);
        //chart.invalidate(); // Refresh
        chart.animateXY(500, 500); // Animates and refreshes
    }

    private void initData(){

        // Create a pool of threads - limits number of threads to avoid JVM crashes
        ExecutorService executor = Executors.newFixedThreadPool(40);
        long startTime = System.nanoTime();

        // Iterate through each country
        Log.d(TAG, "Iterating through each country....");
        for (int i = 0; i < Countries.getCountries().size();++i) {

            final Country country = Countries.getCountry(i);

            // Iterate through each indicator for this country
            for (String indicatorId : indicatorIds) {
                Runnable dataRetrieverThread = new DataRetrieverThread(this, country, country.getId(), indicatorId);
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

    public void hideSystemUi() {
        findViewById(android.R.id.content).setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
}
