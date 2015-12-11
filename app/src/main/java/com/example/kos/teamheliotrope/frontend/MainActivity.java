package com.example.kos.teamheliotrope.frontend;

//~ JDK/Android Imports ========================================

import com.example.kos.teamheliotrope.backend.CountryInfoThread;
import com.example.kos.teamheliotrope.backend.DataRetrieverThread;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kos.teamheliotrope.R;
import com.example.kos.teamheliotrope.backend.Countries;
import com.example.kos.teamheliotrope.backend.Country;
import com.example.kos.teamheliotrope.backend.Indicator;
import com.example.kos.teamheliotrope.backend.InternalStorage;
import com.example.kos.teamheliotrope.backend.Value;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//~ Non-JDK/Android Imports ====================================

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PieChartView;

/**
 * The main activity. Responsible for visualisation of the app.
 */
public class MainActivity extends AppCompatActivity {
    public static final String COUNTRYKEY = "COUNTRIES_DATA";
    public static final String TAG = "MAIN_ACTIVITY";
    public static final int dateMin = 1990;
    public static final int dateMax = 2012;
    public static final String countryQuery = "http://api.worldbank.org/country?per_page=300&region=WLD&format=json";
    public final String[] indicatorIds = {
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
    public static int grey = Color.parseColor("#616161");

    protected boolean hasInternetConnection;

    //Views from activity_main.xml that we need reference to
    PieChartView pieChart;
    LineChartView lineChart;
    ArrayList<IndicatorButton> indicatorButtons = new ArrayList<>();
    Spinner spCountries,spYear,spIndicators;
    TextView in,overTime,tvTotalEnergyConsumption,tvRenewableEnergyConsumption,tvFossilFuelEnergyConsumptionPanel,tvOtherEnergyConsumptionPanel;
    LinearLayout mainChartAndStatsLayout, secondaryChartLayout, indicatorPanel;
    ScrollView contentScrollView;

    public AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setContentView(R.layout.activity_main);
        hideSystemUi();

        // === BEGIN DEFINES
        hasInternetConnection = false;

        pieChart = (PieChartView) findViewById(R.id.mainChart);
        lineChart = (LineChartView) findViewById(R.id.secondaryChart);

        lineChart.setInteractive(false);

        spCountries = (Spinner) findViewById(R.id.spCountries);
        spYear = (Spinner) findViewById(R.id.spYear);

        in = (TextView) findViewById(R.id.in);
        overTime = (TextView) findViewById(R.id.overTime);

        tvTotalEnergyConsumption = (TextView) findViewById(R.id.tvTotalEnergyConsumption);
        tvRenewableEnergyConsumption = (TextView) findViewById(R.id.tvRenewableEnergyConsumption);
        tvFossilFuelEnergyConsumptionPanel = (TextView) findViewById(R.id.tvFossilFuelEnergyConsumptionPanel);
        tvOtherEnergyConsumptionPanel = (TextView) findViewById(R.id.tvOtherEnergyConsumptionPanel);

        mainChartAndStatsLayout = (LinearLayout) findViewById(R.id.mainChartAndStatsLayout);
        secondaryChartLayout = (LinearLayout) findViewById(R.id.secondaryChartLayout);
        indicatorPanel = (LinearLayout) findViewById(R.id.indicatorPanel);
        contentScrollView = (ScrollView) findViewById(R.id.contentScrollView);
        // === END DEFINES


        setupIndicatorPanel();
        setHeightOfMainArea();
        setupScrollView();

        // Keep UI hidden
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    // System UI is visible, hide it after delay
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hideSystemUi();
                        }
                    }, 1000);
                }
            }
        });


        // Test internet connection
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

        loadingDialog = new AlertDialog.Builder(this).setTitle(getString(R.string.please_wait)).setMessage(getString(R.string.initialising)).setCancelable(false).show();

        // Setup app
        final Handler toastHandler = new Handler();
        new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "Checking for cache file...");
                if (InternalStorage.cacheExists(MainActivity.this, COUNTRYKEY)) { // If cache exists, read data from it
                    Log.d(TAG, "Cache file found. Attempting to read data from it...");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialog.setMessage(getString(R.string.reading_from_cache));
                        }
                    });

                    try {
                        ArrayList<Country> cachedCountries = (ArrayList<Country>) InternalStorage.readObject(MainActivity.this, COUNTRYKEY);
                        if (cachedCountries.size() > 0){
                            Countries.setCountries(cachedCountries);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (hasInternetConnection) { // Else if connected to internet, fetch data
                    Log.d(TAG, "No cache file found. Performing first time setup.");
                    initCountries();
                    initData();
                    displayCountriesNullCounts(); // Debug
                } else { // No internet connection or cache file
                    Log.d(TAG, "No cache file found, and no internet connection!");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialog.setMessage(getString(R.string.no_internet_connection));
                        }
                    });

                    while (true) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.setMessage(getString(R.string.initialising_user_interface));

                        initSpinners();
                        setupPieChart();
                        updateIndicatorButtons();
                        setupLineChart();

                        loadingDialog.dismiss();
                    }
                });

                toastHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(MainActivity.this, getString(R.string.scroll_down_for_more), Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });
                    }
                }, 5000);
            }
        }.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart triggered.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume triggered.");
    }

    /**
     * Sets height of the main content area (workaround for not being able to set this in XML)
     */
    private void setHeightOfMainArea() {
        ViewTreeObserver viewTreeObserver = indicatorPanel.getViewTreeObserver();

        // Wait until indicator panel has been drawn (so we can use its height value)
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                LinearLayout.LayoutParams params;
                int height = indicatorPanel.getHeight();

                // Set height of main-chart-and-stats layout to height of screen
                params = (LinearLayout.LayoutParams) mainChartAndStatsLayout.getLayoutParams();
                params.height = height;
                mainChartAndStatsLayout.setLayoutParams(params);

                // Set height of secondary chart layout to height of screen
                params = (LinearLayout.LayoutParams) secondaryChartLayout.getLayoutParams();
                params.height = height;
                secondaryChartLayout.setLayoutParams(params);
            }
        });
    }

    /**
     * Setup the scroll view.
     * Adds a listener to scroll views that triggers whenever the user scrolls - gradually fades in/out
     * information that is relevant to the pie/line chart.
     */
    private void setupScrollView() {
        contentScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int posY = contentScrollView.getScrollY();
                float percent = ((float) posY / contentScrollView.getHeight()) * 100;
                float alpha = (100 - percent) / 100;

                spYear.setAlpha(alpha);
                in.setAlpha(alpha);
                overTime.setAlpha(1 - alpha);

                if (percent >= 90) {
                    spYear.setEnabled(false);
                } else {
                    spYear.setEnabled(true);
                }

                for (IndicatorButton indicatorButton : indicatorButtons) {
                    indicatorButton.getTextView().setAlpha(alpha);
                }

                Log.d(TAG, String.format("Scroll changed: %d (%f%%)", posY, percent));
            }
        });
    }

    /**
     * Programatically creates indicator buttons and adds them to the indicator panel.
     */
    private void setupIndicatorPanel() {
        String[] indicatorTitles = {
                getString(R.string.fossil_fuels),
                getString(R.string.nuclear),
                getString(R.string.marine),
                getString(R.string.biofuel),
                getString(R.string.hydro),
                getString(R.string.wind),
                getString(R.string.solar),
                getString(R.string.geothermal),
                getString(R.string.waste),
                getString(R.string.biogas)
        };

        String[] indicators = {
                "EG.USE.COMM.FO.ZS", // fossil
                "EG.USE.COMM.CL.ZS", // nuclear
                "2.1.10_SHARE.MARINE",
                "2.1.4_SHARE.BIOFUELS",
                "2.1.3_SHARE.HYDRO",
                "2.1.5_SHARE.WIND",
                "2.1.6_SHARE.SOLAR",
                "2.1.7_SHARE.GEOTHERMAL",
                "2.1.8_SHARE.WASTE",
                "2.1.9_SHARE.BIOGAS"
        };

        int[] colors = {
                // Android color guidelines: https://www.google.com/design/spec/style/color.html
                Color.parseColor("#795548"), // brown
                Color.parseColor("#FF9800"), // orange
                Color.parseColor("#009688"), // teal
                Color.parseColor("#4CAF50"), // green
                Color.parseColor("#2196F3"), // blue
                Color.parseColor("#CDDC39"), // lime
                Color.parseColor("#FFEB3B"), // yellow
                Color.parseColor("#F44336"), // red
                Color.parseColor("#9E9E9E"), // grey
                Color.parseColor("#3F51B5"), // indigo
        };

        int[] icons = {
                R.drawable.fossil,
                R.drawable.nuclear,
                R.drawable.marine,
                R.drawable.biofuel,
                R.drawable.hydro,
                R.drawable.wind,
                R.drawable.solar,
                R.drawable.geothermal,
                R.drawable.waste,
                R.drawable.biogas
        };

        LinearLayout.LayoutParams params;
        int dp;

        // Create a row (as LinearLayout) that contains two indicator buttons
        for (int i=0; i<10; i+=2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            row.setLayoutParams(params);

            for (int c=0; c<2; c++) {
                // Column (layout also acts as button)
                LinearLayout column = new LinearLayout(this);
                column.setOrientation(LinearLayout.HORIZONTAL);
                column.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v.getAlpha() == 1) {
                            v.setAlpha(0.4f);
                        } else {
                            v.setAlpha(1);
                        }
                        updatePieChart();
                        setupLineChart();
                    }
                });

                params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                dp = dpToPx(4);
                params.setMargins(dp, dp, dp, dp);
                column.setLayoutParams(params);


                // Icon
                LinearLayout iconLayout = new LinearLayout(this);
                ImageView icon = new ImageView(this);
                icon.setImageResource(icons[i + c]);
                dp = dpToPx(10);
                icon.setPadding(dp, dp, dp, dp);
                iconLayout.addView(icon);

                params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                iconLayout.setLayoutParams(params);


                // Description
                LinearLayout descLayout = new LinearLayout(this);
                dp = dpToPx(10);
                descLayout.setPadding(dp, dp, dp, dp);
                descLayout.setOrientation(LinearLayout.VERTICAL);
                descLayout.setGravity(Gravity.CENTER);
                //descLayout.setBackgroundColor(Color.BLACK); // test
                TextView value = new TextView(this);
                value.setTextColor(Color.WHITE);
                value.setGravity(Gravity.LEFT);
                value.setTextSize(15);
                value.setText(getString(R.string.zero_percent));
                TextView title = new TextView(this);
                title.setTextColor(Color.WHITE);
                //title.setBackgroundColor(Color.RED); // test
                title.setGravity(Gravity.LEFT);
                title.setText(indicatorTitles[i + c]);

                descLayout.addView(value);
                descLayout.addView(title);


                params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.8f);
                descLayout.setLayoutParams(params);

                // Create indicator button object
                IndicatorButton indicatorButton = new IndicatorButton(this, column, value, indicators[i+c], indicatorTitles[i+c], colors[i+c]);
                indicatorButtons.add(indicatorButton);

                column.addView(iconLayout);
                column.addView(descLayout);
                row.addView(column);
            }

            indicatorPanel.addView(row);
        }

        TextView valueDec = new TextView(this);
        valueDec.setText(R.string.value_desc);
        indicatorPanel.addView(valueDec);
    }

    /**
     * Populate the country spinner with a selection of countries (from the Countries class).
     * Populate the year spinner with a range of years.
     */
    private void initSpinners(){
        List<String> spinnerArrayCountry =  new ArrayList<String>();

        for (Country country : Countries.getCountries()){
            spinnerArrayCountry.add(country.getName());
        }

        addDataToSpinner(spCountries, spinnerArrayCountry);
        spCountries.setSelection(getSpinnerIndexOf(spCountries, getString(R.string.united_kingdom))); // Set default selection

        spCountries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstLoad = true;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Country selectedCountry = Countries.getCountry(position);

                //updateIndicatorSpinner(selectedCountry);

                updateData(selectedCountry, spYear.getSelectedItemPosition() + dateMin);

                if (firstLoad) {
                    firstLoad = false;
                } else {
                    updatePieChart();
                    setupLineChart();
                    updateIndicatorButtons();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        List<String> spinnerArrayYear = new ArrayList<String>();

        for (int i = dateMin; i <= dateMax; ++i){
            spinnerArrayYear.add(String.valueOf(i));
        }

        addDataToSpinner(spYear,spinnerArrayYear);
        spYear.setSelection(getSpinnerIndexOf(spYear, "2009")); // Set default selection

        spYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstLoad = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateData(Countries.getCountry(spCountries.getSelectedItemPosition()), spYear.getSelectedItemPosition() + dateMin);

                if (firstLoad) {
                    firstLoad = false;
                } else {
                    updatePieChart();
                    updateIndicatorButtons();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    /**
     * Update stats in the left panel.
     * Should be called whenever the user selects a new country/year.
     * @param selectedCountry The selected country
     * @param year The selected year
     */
    private void updateData(Country selectedCountry, int year){
        float totalValue = getValueOfIndicatorCountry(selectedCountry, "1.1_TOTAL.FINAL.ENERGY.CONSUM",year);
        if (totalValue != -1) {
            tvTotalEnergyConsumption.setText(String.format("%d", Math.round(totalValue)) + " " + getString(R.string.tera_joules));
        } else {
            tvTotalEnergyConsumption.setText(getString(R.string.not_applicable));
        }
        float renewableValue = getValueOfIndicatorCountry(selectedCountry,"EG.FEC.RNEW.ZS",year);
        if (renewableValue != -1) {
            tvRenewableEnergyConsumption.setText(String.format("%.2f", renewableValue) + getString(R.string.percentage));
        } else {
            tvRenewableEnergyConsumption.setText(getString(R.string.not_applicable));
        }
        float fossilValue = getValueOfIndicatorCountry(selectedCountry,"EG.USE.COMM.FO.ZS",year);
        if (fossilValue != -1) {
            tvFossilFuelEnergyConsumptionPanel.setText(String.format("%.2f", fossilValue) + getString(R.string.percentage));
        } else {
            tvFossilFuelEnergyConsumptionPanel.setText(getString(R.string.not_applicable));
        }
        float otherValue = 100;
        if (renewableValue != -1){
            otherValue -= renewableValue;
        }
        if (fossilValue != -1){
            otherValue -= fossilValue;
        }

        if (otherValue >= 0){
            tvOtherEnergyConsumptionPanel.setText(String.format("%.2f", otherValue) + getString(R.string.percentage));
        }else{
            tvOtherEnergyConsumptionPanel.setText(getString(R.string.zero_percent));
        }
    }

    /**
     * Populate the given spinner with the given list of strings
     * @param spinner
     * @param spinnerArray
     */
    private void addDataToSpinner(Spinner spinner, List<String> spinnerArray) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.title_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    /**
     * Get index of given String value in spinner adapter
     * @param spinner The spinner to search
     * @param value The value to get the index for
     * @return
     */
    public int getSpinnerIndexOf(Spinner spinner, String value) {
        SpinnerAdapter adapter = spinner.getAdapter();

        for (int i=0; i<adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(value)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Get the value of the given indicator, for a given year, for the given country
     * @param selectedCountry The country to get the indicator for
     * @param indicatorID The indicator to get the value for
     * @param year The year to get the value for
     * @return
     */
    public float getValueOfIndicatorCountry(Country selectedCountry,String indicatorID, int year){
        Indicator indicator = selectedCountry.getIndicator(indicatorID);
        if (indicator != null){
            Value value = indicator.getValue(String.valueOf(year));
            if (value != null){
                return value.getValue();
            }
        }
        return -1;
    }

    /**
     * Calls a method which retrieves a list of Country objects from the World Bank
     */
    private void initCountries(){
        CountryInfoThread countryInfoThread = new CountryInfoThread(this,countryQuery);
        countryInfoThread.start();
        try {
            countryInfoThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the indicator buttons with values for the current selected country and date
     */
    protected void updateIndicatorButtons() {
        Country country = Countries.getCountry(spCountries.getSelectedItem().toString());
        String date = spYear.getSelectedItem().toString();

        for (IndicatorButton indicatorButton : indicatorButtons) {
            TextView valueTextView = indicatorButton.getTextView();
            Value value = country.getIndicator(indicatorButton.getIndicatorId()).getValue(date);

            if(indicatorButton.getIndicatorId().equals("2.1.4_SHARE.BIOFUELS")){
                Log.d("TEST",country.getName() + ", " + date);
                float tempValue = 0;

                if (value != null){
                    Log.d("TEST","AV" + value.getValue());
                    tempValue += value.getValue();
                }
                Value tradBio = country.getIndicator("2.1.1_SHARE.TRADBIO").getValue(date);
                if (tradBio != null){
                    Log.d("TEST","TB" + tradBio.getValue());
                    tempValue += tradBio.getValue();
                }
                Value modernBio = country.getIndicator("2.1.2_SHARE.MODERNBIO").getValue(date);
                if(modernBio != null){
                    Log.d("TEST","MB" + modernBio.getValue());
                    tempValue += modernBio.getValue();
                }
                if (tempValue != 0){
                    Log.d("TEST","NV" + tempValue);
                    value = new Value(date,tempValue);
                }
            }

            if (value != null) {
                valueTextView.setText(String.format("%.2f%%", value.getValue()));
            } else {
                valueTextView.setText(getString(R.string.not_applicable));
            }
        }
    }

    /**
     * Do first-time setup of the pie chart.
     * Gets data from the selected country and year and adds it to the pie chart.
     */
    protected void setupPieChart() {
        Country country = Countries.getCountry(spCountries.getSelectedItem().toString());
        Log.d(TAG, "Country: " + country.getName());
        String date = spYear.getSelectedItem().toString();
        Log.d(TAG, "Date: "+ date);

        // Define values for chart
        ArrayList<SliceValue> slices = new ArrayList<>();
        for (IndicatorButton indicatorButton : indicatorButtons) {
            Value value = country.getIndicator(indicatorButton.getIndicatorId()).getValue(date);

            if(indicatorButton.getIndicatorId().equals("2.1.4_SHARE.BIOFUELS")){
                Log.d("TEST",country.getName() + ", " + date);
                float tempValue = 0;

                if (value != null){
                    Log.d("TEST","AV" + value.getValue());
                    tempValue += value.getValue();
                }
                Value tradBio = country.getIndicator("2.1.1_SHARE.TRADBIO").getValue(date);
                if (tradBio != null){
                    Log.d("TEST","TB" + tradBio.getValue());
                    tempValue += tradBio.getValue();
                }
                Value modernBio = country.getIndicator("2.1.2_SHARE.MODERNBIO").getValue(date);
                if(modernBio != null){
                    Log.d("TEST","MB" + modernBio.getValue());
                    tempValue += modernBio.getValue();
                }
                if (tempValue != 0){
                    Log.d("TEST","NV" + tempValue);
                    value = new Value(date,tempValue);
                }
            }

            // Set null value slices to 0 (but include in slices array as the updatePieChart() method is dependent on slices order)
            SliceValue slice;
            if (value != null) {
                slice = new SliceValue(value.getValue(), indicatorButton.getColor());
            } else {
                slice = new SliceValue(0, 0);
            }

            slices.add(slice);
        }

        // Set chart data
        PieChartData chartData = new PieChartData();
        chartData.setValues(slices);
        chartData.setHasCenterCircle(true);
        chartData.setSlicesSpacing(0);

        pieChart.setPieChartData(chartData); // Also refreshes chart
    }

    /**
     * Update the pie chart with new values for the selected indicators/country/year.
     * Also animates the chart to show change in values.
     * For any disabled indicators, their values in the chart are set to 0 (rather than removing them)
     * so the chart understands how to animate with the new dataset.
     */
    protected void updatePieChart() {
        Country country = Countries.getCountry(spCountries.getSelectedItem().toString());
        Log.d(TAG, "Country: " + country.getName());
        String date = spYear.getSelectedItem().toString();
        Log.d(TAG, "Date: "+ date);

        PieChartData chartData = pieChart.getPieChartData();
        List<SliceValue> slices = chartData.getValues();

        IndicatorButton indicatorButton;
        Value value;
        for (int i=0; i<slices.size(); i++) {
            indicatorButton = indicatorButtons.get(i);
            SliceValue slice = slices.get(i);

            value = country.getIndicator(indicatorButton.getIndicatorId()).getValue(date);

            if(indicatorButton.getIndicatorId().equals("2.1.4_SHARE.BIOFUELS")){
                Log.d("TEST",country.getName() + ", " + date);
                float tempValue = 0;

                if (value != null){
                    Log.d("TEST","AV" + value.getValue());
                    tempValue += value.getValue();
                }
                Value tradBio = country.getIndicator("2.1.1_SHARE.TRADBIO").getValue(date);
                if (tradBio != null){
                    Log.d("TEST","TB" + tradBio.getValue());
                    tempValue += tradBio.getValue();
                }
                Value modernBio = country.getIndicator("2.1.2_SHARE.MODERNBIO").getValue(date);
                if(modernBio != null){
                    Log.d("TEST","MB" + modernBio.getValue());
                    tempValue += modernBio.getValue();
                }
                if (tempValue != 0){
                    Log.d("TEST","NV" + tempValue);
                    value = new Value(date,tempValue);
                }
            }

            if (indicatorButton.isEnabled() && (value != null)) {
                Log.d("TEST","SETV" + value.getValue());
                slice.setTarget(value.getValue());
            } else {
                slice.setTarget(0);
            }
        }

        pieChart.startDataAnimation();
    }

    /**
     * (Re)Generate the line chart with values for all years for the currently selected indicators
     */
    protected void setupLineChart() {
        Country country = Countries.getCountry(spCountries.getSelectedItem().toString());

        // Define lines for chart
        ArrayList<Line> lines = new ArrayList<>();
        ArrayList<PointValue> pointValues;
        for (IndicatorButton indicatorButton : indicatorButtons) {
            // Only get values for enabled indicators
            if (!indicatorButton.isEnabled()) {
                continue;
            }

            // Define points for this line
            pointValues = new ArrayList<>();
            for (int i=dateMin; i<dateMax; i++) {
                Value value = country.getIndicator(indicatorButton.getIndicatorId()).getValue(String.valueOf(i));

                if(indicatorButton.getIndicatorId().equals("2.1.4_SHARE.BIOFUELS")){
                    Log.d("TEST",country.getName() + ", " + String.valueOf(i));
                    float tempValue = 0;

                    if (value != null){
                        Log.d("TEST","AV" + value.getValue());
                        tempValue += value.getValue();
                    }
                    Value tradBio = country.getIndicator("2.1.1_SHARE.TRADBIO").getValue(String.valueOf(i));
                    if (tradBio != null){
                        Log.d("TEST","TB" + tradBio.getValue());
                        tempValue += tradBio.getValue();
                    }
                    Value modernBio = country.getIndicator("2.1.2_SHARE.MODERNBIO").getValue(String.valueOf(i));
                    if(modernBio != null){
                        Log.d("TEST","MB" + modernBio.getValue());
                        tempValue += modernBio.getValue();
                    }
                    if (tempValue != 0){
                        Log.d("TEST","NV" + tempValue);
                        value = new Value(String.valueOf(i),tempValue);
                    }
                }

                if (value != null) {
                    pointValues.add(new PointValue(i, value.getValue()));
                }
            }

            lines.add(new Line(pointValues).setColor(indicatorButton.getColor()));
        }

        // Set chart data
        LineChartData chartData = new LineChartData();
        chartData.setLines(lines);
        chartData.setAxisXBottom(new Axis().setHasLines(true).setTextColor(grey));
        chartData.setAxisYLeft(new Axis().setTextColor(grey));

        lineChart.setLineChartData(chartData); // Also refreshes chart
    }

    /**
     * Retrieve all data from the World Bank and create a cache file with this data.
     */
    private void initData(){

        // Create a pool of threads - limits number of threads to avoid JVM crashes
        int maxThreadCount;
        if (Build.FINGERPRINT.contains("generic")) {
            Log.d(TAG, "Emulator device detected. Using low max thread count to prevent crashes.");
            maxThreadCount = 1;
        } else {
            Log.d(TAG, "Non-emulator device detected.");
            maxThreadCount = 30;
        }

        ExecutorService executor = Executors.newFixedThreadPool(maxThreadCount);
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


        // Save data to cache
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingDialog.setMessage(getString(R.string.saving_data_to_cache));
            }
        });

        try {
            Log.d(TAG, "Saving countries to cache...");
            InternalStorage.writeObject(MainActivity.this,COUNTRYKEY,Countries.getCountries());
            Log.d(TAG,"Countries cached");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Display all data (in the logcat) for all countries
     * Only use when debugging as takes some time to print all data.
     */
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

    /**
     * For debugging.
     * Displays the null counts (no data found) for all countries.
     */
    public void displayCountriesNullCounts() {
        Log.d(TAG, "====== Country null value counts ======");
        Log.d(TAG, "Total countries: " + Countries.getCountries().size());
        for (Country country : Countries.getCountries()) {
            Log.d(TAG, String.format("%s: %d", country.getName(), country.getNullValueCount()));
        }
    }

    /**
     * @return An ArrayList of all Indicator Button objects
     */
    public ArrayList<IndicatorButton> getIndicatorButtons() {
        return indicatorButtons;
    }

    /**
     * Hide the system UI
     */
    public void hideSystemUi() {
        findViewById(android.R.id.content).setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    /**
     * Convert the given DP value to a pixel value.
     * @param dp The DP value to convert.
     * @return The converted value in pixels.
     */
    public static int dpToPx(int dp) {
        float scale = Resources.getSystem().getDisplayMetrics().density;

        return ((int) (dp*scale + 0.5f));
    }

    public void showHintDialog(View view){
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage(
                "- " + getString(R.string.dialog_box_year_and_country) +"\n\n" +
                "- " + getString(R.string.dialog_box_toggle_indicator) +"\n\n" +
                "- "+ getString(R.string.dialog_box_long_press_indicator) +"\n\n" +
                "- " + getString(R.string.dialog_box_line_graph));
        dlgAlert.setTitle(getString(R.string.how_to_use));
        dlgAlert.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss the dialog
                    }
                });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }
}
