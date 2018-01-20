package com.example.ksarm.vibestep;

/**
 * Created by Anik on 20/01/2018.
 */

public class ObservableSpeed
{
    private OnSpeedChangeListener listener;

    private int value;
//    private walk value;

    public void setOnSpeedChangeListener(OnSpeedChangeListener listener) {
        this.listener = listener;
    }

    public int get() {
        return value;
    }

    public void set(int value) {
        if (this.value != value) {

            this.value = value;
            if (listener != null) {
                listener.onSpeedChanged(value);
            }
        }
    }
}
