package com.example.kos.teamheliotrope.frontend;

import android.test.ActivityInstrumentationTestCase2;
import com.example.kos.teamheliotrope.backend.Country;


public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    MainActivity mainActivity;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    /**
     * this test method checks whether getSpinnerIndexOf() is capable of taking a spinner,
     *  and a country name as a string and then return  the index of the country
     *  name in the spinner
     */
    public void testGetSpinnerIndexOf() {
        mainActivity = getActivity();

        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                assertNotNull(mainActivity.getSpinnerIndexOf(mainActivity.spCountries, "United Kingdom"));
            }
        });
    }

    /**
     * this test method checks whether getValueOfIndicatorCountry() is capable of taking a country,
     *  indicator and year as parameters and then returning the value of the specific indicators for
     *  the country in the given year as a float value.
     */
    public void testGetValueOfIndicatorCountry(){
        mainActivity = getActivity();
        final String IndicatorID = "1.1_TOTAL.FINAL.ENERGY.CONSUM";
        final Country selectedCountry = new Country();
        final int year = 2000;
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                assertNotNull(mainActivity.getValueOfIndicatorCountry(selectedCountry, IndicatorID, year));
            }
        });

    }

    /**
     * this test method checks whether getIndicatorButtons() returning an IndicatorButton arrayList.
     */
    public void testGetIndicatorButtons(){
        mainActivity = getActivity();

        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                assertNotNull(mainActivity.getIndicatorButtons());
            }
        });
    }

    /**
     * this test method checks whether dpToPx() is capable of taking an int and returning
     *  the value of the int * the resource scale + 0.5
     */
    public void testDpToPx(){
        mainActivity = getActivity();
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                assertNotNull(mainActivity.dpToPx(10));
            }
        });
    }
}