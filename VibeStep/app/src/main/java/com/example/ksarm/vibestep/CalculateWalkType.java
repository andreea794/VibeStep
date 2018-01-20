package com.example.ksarm.vibestep;

import android.os.Handler;
import android.util.Log;

/**
 * Created by Anik on 20/01/2018.
 */


public class CalculateWalkType {

    private Walk currentWalkType = Walk.STATIONARY;
    private int initialStepCount;
    final MainActivity main;
    private Handler mHandler = new Handler();

    public CalculateWalkType(MainActivity m){
        main = m;
    }

    public void initTimer() {
        Runnable mUpdateTask = new Runnable() {
            @Override
            public void run() {

                int m = main.getStepCount();

                int changeInStep = main.getStepCount()- initialStepCount;
                Log.e("a",Integer.toString(changeInStep));
                if (changeInStep == 0)
                    currentWalkType = Walk.STATIONARY;
                else if (changeInStep < 5)
                    currentWalkType = Walk.SLOW_WALK;
                else if (changeInStep < 10)
                    currentWalkType = Walk.FAST_WALK;
                else if (changeInStep < 15)
                    currentWalkType = Walk.RUN;
                else
                    currentWalkType = Walk.SPRINT;

                initialStepCount = m;
                main.updateTextView(currentWalkType.toString() + "; " + changeInStep);
            }
        };

        mHandler.removeCallbacks(mUpdateTask);
        mHandler.postDelayed(mUpdateTask, 1000);
    }




}