package com.example.kos.teamheliotrope.frontend;

import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IndicatorButton {
    MainActivity mainActivity;
    LinearLayout layout;
    TextView valueTextView;
    String indicatorTitle;
    String indicatorId;
    int color;
    int clickCount = 0;

    public IndicatorButton(MainActivity activity, LinearLayout layout, TextView valueTextView, String indicatorId, String indicatorTitle, int color) {
        this.mainActivity = activity;
        this.layout = layout;
        this.valueTextView = valueTextView;
        this.indicatorId = indicatorId;
        this.indicatorTitle = indicatorTitle;
        this.color = color;

        this.layout.setBackgroundColor(color);
        setOnLongClickListener();
    }

    /**
     * Set onLongClickListener for layout.
     * When triggered, all buttons except this one will be disabled.
     */
    private void setOnLongClickListener() {
        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isEnabled()) {
                    return true;
                }

                for (IndicatorButton indicatorButton : mainActivity.getIndicatorButtons()) {
                    if (!indicatorButton.equals(IndicatorButton.this)) { // Don't include this layout
                        indicatorButton.getLayout().setAlpha(0.5f); // Disable layout
                        mainActivity.updatePieChart();
                        mainActivity.setupLineChart();
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
        layout.setOnClickListener(new View.OnClickListener() {
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

    public LinearLayout getLayout() {
        return layout;
    }

    public TextView getTextView() {
        return valueTextView;
    }

    public String getIndicatorId() {
        return indicatorId;
    }

    public int getColor() {
        return color;
    }

    public boolean isEnabled() {
        return (layout.getAlpha() == 1);
    }
}
