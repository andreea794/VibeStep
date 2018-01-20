package com.example.ksarm.vibestep;

import android.content.Context;
import android.media.MediaPlayer;

import java.util.Timer;
import java.util.TimerTask;

import static android.provider.CalendarContract.CalendarCache.URI;

/**
 * Created by simeon on 20.01.18.
 */

public class HehPlayer {
    private static MediaPlayer mCurPlayer, mPrevPlayer;
    private float curVolume;
    private final float mDELTA_FADE = (float)0.02;

    public HehPlayer() {
        mCurPlayer = mPrevPlayer = null;
    }

    private void fadeStep(MediaPlayer mp, float delta){
        curVolume += delta;
        mp.setVolume(curVolume, curVolume);
    }

    private void fadeOut(final MediaPlayer mp) {
        curVolume = (float)1.0;

        final Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                if (curVolume < 0.0) {
                    timer.cancel();
                    timer.purge();

                    mp.stop();
                    return;
                }
                fadeStep(mp, -mDELTA_FADE);
            }
        };

        timer.schedule(timerTask, 50, 50);
    }

    private void fadeIn(final MediaPlayer mp) {
        curVolume = (float)0.0;

        final Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                if (curVolume > 1.0) {
                    timer.cancel();
                    timer.purge();

                    mp.setVolume(1f, 1f);

                    return;
                }
                fadeStep(mp, mDELTA_FADE);
            }
        };

        mp.setVolume(0f, 0f);
        mp.start();

        timer.schedule(timerTask, 50, 50);
    }

    public void playSong(Context context, int songID) {

        MediaPlayer player;
        if (mCurPlayer != null)
        {
            mPrevPlayer = mCurPlayer;
            fadeOut(mPrevPlayer);
        }

        mCurPlayer = MediaPlayer.create(context, songID);

        mCurPlayer.setLooping(true);
        mCurPlayer.start();
    }
    public void playSong(Context context, String songPath) {

        MediaPlayer player;
        if (mCurPlayer != null)
        {
            mPrevPlayer = mCurPlayer;
            fadeOut(mPrevPlayer);
        }

        mCurPlayer = MediaPlayer.create(context, URI.parse(songPath));

        mCurPlayer.setLooping(true);
        mCurPlayer.start();
    }
}
