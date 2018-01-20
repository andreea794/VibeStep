package com.example.ksarm.vibestep;

/**
 * Created by Anik on 20/01/2018.
 */

public class ObservableSpeed
{
    private OnSpeedChangeListener listener;
    private Walk value;

    ObservableSpeed () {
        value = Walk.STATIONARY;
    }


    public void setOnSpeedChangeListener(OnSpeedChangeListener listener) {
        this.listener = listener;
    }

    public Walk get() {
        return value;
    }

    public void set(Walk value) {
        if (!this.value.equals(value)) {

            this.value = value;
            if (listener != null) {
                listener.onSpeedChanged(value);
            }
        }
    }
}
