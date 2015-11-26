package com.example.kos.teamheliotrope;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by jamiegoodson on 26/11/2015.
 */
public class DataThread extends Thread {
    MainActivity mainActivity;
    JSONArray JSONWebsiteData;
    String website;

    public DataThread(MainActivity mainActivity, String website) {
        this.mainActivity = mainActivity;
        this.website = website;
    }

    @Override
    public void run() {
        JSONWebsiteData = generateWebsiteData();
        mainActivity.displayData(JSONWebsiteData);
    }

    private JSONArray generateWebsiteData() {
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

    public JSONArray getData() {
        return JSONWebsiteData;
    }
}
