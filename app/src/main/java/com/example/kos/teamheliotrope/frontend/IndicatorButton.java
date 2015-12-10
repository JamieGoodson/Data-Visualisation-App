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

    /**
     * Construct an IndicatorButton
     * @param activity Activity where the button will lie.
     * @param layout Lyout that the button will use to act as an interactive button.
     * @param valueTextView TextView object that is used to display the value for the associated indicator.
     * @param indicatorId Indicator ID that is to be associated with this button.
     * @param indicatorTitle Title of the indicator to be displayed.
     * @param color Color to set the background of the button to.
     */
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

    /**
     * Get the layout that this object uses as a button.
     * @return Layout.
     */
    public LinearLayout getLayout() {
        return layout;
    }

    /**
     * Get the text view that is used to display the value of the associated indicator.
     * @return Text view object.
     */
    public TextView getTextView() {
        return valueTextView;
    }

    /**
     * Get the indicator ID associated with this object.
     * @return Indicator ID
     */
    public String getIndicatorId() {
        return indicatorId;
    }

    /**
     * Get the color of this object's layout.
     * @return Color.
     */
    public int getColor() {
        return color;
    }

    /**
     * Check if this button is enabled (by checking alpha value)
     * @return True if enabled, false if not.
     */
    public boolean isEnabled() {
        return (layout.getAlpha() == 1);
    }
}
