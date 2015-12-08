package com.example.kos.teamheliotrope.frontend;

import com.example.kos.teamheliotrope.backend.CountryInfoThread;
import com.example.kos.teamheliotrope.backend.DataRetrieverThread;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.example.kos.teamheliotrope.R;
import com.example.kos.teamheliotrope.backend.Countries;
import com.example.kos.teamheliotrope.backend.Country;
import com.example.kos.teamheliotrope.backend.Indicator;
import com.example.kos.teamheliotrope.backend.InternalStorage;
import com.example.kos.teamheliotrope.backend.Value;


import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.util.AxisAutoValues;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PieChartView;

public class MainActivity extends AppCompatActivity {

    File cDir;
    File fDir;

    public static final String COUNTRYKEY = "COUNTRIES_DATA";
    public static final String TAG = "MAIN_ACTIVITY";
    public static final int dateMin = 1990;
    public static final int dateMax = 2012;
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
    PieChartView pieChart;
    LineChartView lineChart;
    ArrayList<IndicatorButton> indicatorButtons = new ArrayList<>();
    Spinner spCountries,spYear,spIndicators;
    TextView tvTotalEnergyConsumption,tvRenewableEnergyConsumption,tvFossilFuelEnergyConsumptionPanel,tvOtherEnergyConsumptionPanel;
    LinearLayout topPanel, mainChartAndStatsLayout, secondaryChartLayout, indicatorPanel;

    public AlertDialog loadingDialog;

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume triggered.");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        hideSystemUi();

        cDir = getApplicationContext().getCacheDir();
        fDir = getApplicationContext().getCacheDir();

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

        hasInternetConnection = false;

        pieChart = (PieChartView) findViewById(R.id.mainChart);
        lineChart = (LineChartView) findViewById(R.id.secondaryChart);

        spCountries = (Spinner) findViewById(R.id.spCountries);
        spYear = (Spinner) findViewById(R.id.spYear);

        tvTotalEnergyConsumption = (TextView) findViewById(R.id.tvTotalEnergyConsumption);
        tvRenewableEnergyConsumption = (TextView) findViewById(R.id.tvRenewableEnergyConsumption);
        tvFossilFuelEnergyConsumptionPanel = (TextView) findViewById(R.id.tvFossilFuelEnergyConsumptionPanel);
        tvOtherEnergyConsumptionPanel = (TextView) findViewById(R.id.tvOtherEnergyConsumptionPanel);

        topPanel = (LinearLayout) findViewById(R.id.topPanel);
        mainChartAndStatsLayout = (LinearLayout) findViewById(R.id.mainChartAndStatsLayout);
        secondaryChartLayout = (LinearLayout) findViewById(R.id.secondaryChartLayout);
        indicatorPanel = (LinearLayout) findViewById(R.id.indicatorPanel);

        setupIndicatorPanel();
        setHeightOfMainArea();


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
                if (hasInternetConnection) {
                    initCountries();
                    initData();
                } else {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.setMessage("Reading from cache...");
                            }
                        });
                        ArrayList<Country> cachedCountries = (ArrayList<Country>) InternalStorage.readObject(MainActivity.this, COUNTRYKEY);
                        if (cachedCountries.size() > 0){
                            Countries.setCountries(cachedCountries);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initSpinners();
                        setupPieChart();
                        updateIndicatorButtons();
                        setupLineChart();

                        loadingDialog.dismiss();
                    }
                });
            }
        }.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart triggered.");
    }

    private void setHeightOfMainArea() {
        ViewTreeObserver viewTreeObserver = indicatorPanel.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                LinearLayout.LayoutParams params;

                // Set height of main-chart-and-stats layout to height of screen
                params = (LinearLayout.LayoutParams) mainChartAndStatsLayout.getLayoutParams();
                params.height = indicatorPanel.getHeight();
                mainChartAndStatsLayout.setLayoutParams(params);

                // Set height of secondary chart layout to height of screen
                params = (LinearLayout.LayoutParams) secondaryChartLayout.getLayoutParams();
                params.height = indicatorPanel.getHeight();
                secondaryChartLayout.setLayoutParams(params);
            }
        });
    }
    
    private void setupIndicatorPanel() {
        String[] indicatorTitles = {
                "Fossil Fuels",
                "Nuclear",
                "Marine",
                "Biofuel",
                "Hydro",
                "Wind",
                "Solar",
                "Geothermal",
                "Waste",
                "Biogas"
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
                        setupPieChart();
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
                value.setText("0%");
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

                //updateIndicatorSpinner(selectedCountry);

                updateData(selectedCountry, spYear.getSelectedItemPosition() + dateMin);

                if (firstLoad) {
                    firstLoad = false;
                } else {
                    setupPieChart();
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
                    setupPieChart();
                    updateIndicatorButtons();
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
            tvTotalEnergyConsumption.setText("N/A");
        }
        float renewableValue = getValueOfIndicatorCountry(selectedCountry,"EG.FEC.RNEW.ZS",year);
        if (renewableValue != -1) {
            tvRenewableEnergyConsumption.setText(String.format("%.2f", renewableValue) + "%");
        } else {
            tvRenewableEnergyConsumption.setText("N/A");
        }
        float fossilValue = getValueOfIndicatorCountry(selectedCountry,"EG.USE.COMM.FO.ZS",year);
        if (fossilValue != -1) {
            tvFossilFuelEnergyConsumptionPanel.setText(String.format("%.2f", fossilValue) + "%");
        } else {
            tvFossilFuelEnergyConsumptionPanel.setText("N/A");
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

    protected void updateIndicatorButtons() {
        Country country = Countries.getCountry(spCountries.getSelectedItem().toString());
        String date = spYear.getSelectedItem().toString();

        for (IndicatorButton indicatorButton : indicatorButtons) {
            TextView valueTextView = indicatorButton.getTextView();
            Value value = country.getIndicator(indicatorButton.getIndicatorId()).getValue(date);

            if (value != null) {
                valueTextView.setText(String.format("%.2f%%", value.getValue()));
            } else {
                valueTextView.setText("N/A");
            }
        }
    }

    protected void setupPieChart() {
        Country country = Countries.getCountry(spCountries.getSelectedItem().toString());
        Log.d(TAG, "Country: " + country.getName());
        String date = spYear.getSelectedItem().toString();
        Log.d(TAG, "Date: "+ date);

        // Define values for chart
        ArrayList<SliceValue> slices = new ArrayList<>();
        for (IndicatorButton indicatorButton : indicatorButtons) {

            // Only get values for enabled indicators
            if (!indicatorButton.isEnabled()) {
                continue;
            }

            Value value = country.getIndicator(indicatorButton.getIndicatorId()).getValue(date);

            // TODO: Add slice to chart that represents missing data (to prevent misrepresentation of data)
            // Skip null values
            if (value != null) {
                slices.add(new SliceValue(
                        value.getValue(),
                        indicatorButton.getColor()
                ));
            }
        }

        // Set chart data
        PieChartData chartData = new PieChartData();
        chartData.setValues(slices);
        chartData.setHasCenterCircle(true);
        chartData.setSlicesSpacing(0);

        pieChart.setPieChartData(chartData); // Also refreshes chart
        pieChart.animate();
    }

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

                if (value != null) {
                    pointValues.add(new PointValue(i, value.getValue()));
                }
            }

            lines.add(new Line(pointValues).setColor(indicatorButton.getColor()));
        }

        // Set chart data
        LineChartData chartData = new LineChartData();
        chartData.setLines(lines);
        chartData.setAxisXBottom(new Axis().setHasLines(true));
        chartData.setAxisYLeft(new Axis());

        lineChart.setLineChartData(chartData); // Also refreshes chart
        lineChart.animate();
    }

    private void initData(){

        // Create a pool of threads - limits number of threads to avoid JVM crashes
        ExecutorService executor = Executors.newFixedThreadPool(10);
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

        try {
            InternalStorage.writeObject(this,COUNTRYKEY,Countries.getCountries());
            Log.d(TAG,"Countries cached");
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    public PieChartView getPieChart() {
        return pieChart;
    }

    public ArrayList<IndicatorButton> getIndicatorButtons() {
        return indicatorButtons;
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

    public static int dpToPx(int dp) {
        float scale = Resources.getSystem().getDisplayMetrics().density;

        return ((int) (dp*scale + 0.5f));
    }
}
