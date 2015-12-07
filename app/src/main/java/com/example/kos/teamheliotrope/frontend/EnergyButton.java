package com.example.kos.teamheliotrope.frontend;

import android.os.Handler;
import android.view.View;
import android.widget.Button;

public class EnergyButton {
    MainActivity mainActivity;
    Button button;
    String indicatorId;
    int color;
    int clickCount = 0;

    public EnergyButton(MainActivity activity, Button button, String indicatorId, int color) {
        this.mainActivity = activity;
        this.button = button;
        this.indicatorId = indicatorId;
        this.color = color;

        button.setBackgroundColor(color);
        setOnLongClickListener();
    }


    /**
     * Set onLongClickListener for button.
     * When triggered, all buttons except this one will be disabled.
     */
    private void setOnLongClickListener() {
        this.button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                for (EnergyButton energyButton : mainActivity.getEnergyButtons()) {
                    if (!energyButton.equals(EnergyButton.this)) { // Don't include this button
                        energyButton.getButton().setAlpha(0.5f); // Disable button
                        mainActivity.setupChart();
                    }
                }

                return true;
            }
        });
    }

    /**
     * Set on click listener that caters for single and double clicks
     */
    private void setOnClickListener() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCount++; // Increment click count

                Handler handler = new Handler();

                Runnable singleClick = new Runnable() {
                    @Override
                    public void run() {
                        // Check if click count still equals 1 (i.e. user hasn't performed a double click by this point)
                        if (clickCount == 1) {
                            clickCount = 0;

                            // Do stuff here
                        }
                    }
                };

                if (clickCount == 1) {
                    handler.postDelayed(singleClick, 250);
                } else if (clickCount == 2) { // Double click
                    clickCount = 0;

                    // Do stuff here
                }
            }
        });
    }

    public Button getButton() {
        return button;
    }

    public String getIndicatorId() {
        return indicatorId;
    }

    public int getColor() {
        return color;
    }

    public boolean isEnabled() {
        return (button.getAlpha() == 1);
    }
}
