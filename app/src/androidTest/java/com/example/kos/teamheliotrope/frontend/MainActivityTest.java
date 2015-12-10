package com.example.kos.teamheliotrope.frontend;

import android.test.ActivityInstrumentationTestCase2;


public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    MainActivity mainActivity;

    public MainActivityTest() {
        super(MainActivity.class);

    }

    public void testDpToPx(){
        mainActivity = getActivity();

        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                //assertNotNull(mainActivity.dpToPx(10));
            }
        });
    }

}