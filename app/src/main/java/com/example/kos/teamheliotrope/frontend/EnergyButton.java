package com.example.kos.teamheliotrope.frontend;

import android.widget.Button;

public class EnergyButton {
    Button button;
    String indicatorId;

    public EnergyButton(Button button, String indicatorId) {
        this.button = button;
        this.indicatorId = indicatorId;
    }

    public Button getButton() {
        return button;
    }

    public String getIndicatorId() {
        return indicatorId;
    }
}
