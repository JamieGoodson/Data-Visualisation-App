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
    DataThread dataThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        dataThread = new DataThread(this, "http://api.worldbank.org/countries/indicators/2.0.cov.Ele?per_page=300&date=1960:2015&format=json");
        dataThread.start();
        JSONArray data = dataThread.getData();
    }

    protected void init(){
        textView = (TextView) findViewById(R.id.textView);
        output = "";
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
