package com.example.kos.teamheliotrope;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity {

    JSONArray JSONWebsiteData;
    String output;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        new APIData("http://api.worldbank.org/countries/indicators/2.0.cov.Ele?per_page=300&date=1960:2015&format=json").start();

    }

    protected void init(){
        textView = (TextView) findViewById(R.id.textView);
        output = "";
    }

    protected void clearOutput() {
        output = "";
        textView.setText("");
    }

    protected void outputLine(String s) {
        output += " <strong>" + s + "</strong><br/>";
        textView.setText(Html.fromHtml(output));
    }

    private class APIData extends Thread{

        String website;

        public APIData(String website){
            this.website = website;
        }

        @Override
        public void run() {

            JSONWebsiteData = getWebsiteData();
            displayData(JSONWebsiteData);

        }

        private void displayData(final JSONArray websiteData) {

            if (websiteData == null) return;

            // try to read through data
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try{

                        JSONArray electricityList = websiteData.getJSONArray(1);
                        JSONObject electricity;
                        JSONObject country;
                        String year,value;

                        for (int i = 0; i < electricityList.length(); ++i){
                            electricity = electricityList.getJSONObject(i);

                            country = electricity.getJSONObject("country");
                            year = electricity.getString("date");
                            value = electricity.getString("value");

                            // output name
                            if (!value.equals("null")){
                                double val = Double.parseDouble(value);
                                DecimalFormat df = new DecimalFormat("##.00");
                                outputLine("Country: " + country.getString("value") + " - Year: " + year + "  - Value: " + df.format(val));
                            }

                        }

                    }catch (JSONException e){
                        // something went wrong!
                        e.printStackTrace();
                        outputLine("Something went wrong!");
                    }
                }
            });
        }

        private JSONArray getWebsiteData(){
            // open connection
            URL url = null;
            try {
                url = new URL(website);
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
            if (inStream != null){
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

    }
}
