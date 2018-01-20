package com.example.ksarm.vibestep;

import android.content.Context;
import android.media.MediaPlayer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by simeon on 20.01.18.
 */

public class HehPlayer {
    private HashMap<Integer, MediaPlayer> mPlayer;
    private float curVolume, curVolumeUp;
    private final float mDELTA_FADE = (float)0.02;

    private Context mBasicContext;

    private int mCurPlaying = -1;

    public HehPlayer(Context c, List<Integer> songs) {

        mPlayer = new HashMap<>();

        mBasicContext = c;
        Iterator<Integer> i = songs.iterator();

        while (i.hasNext()) {
            Integer cur = i.next();
            MediaPlayer mcur = MediaPlayer.create(mBasicContext, cur);
            mcur.setLooping(true);

            mPlayer.put(cur,  mcur);
        }
    }

    private void fadeStep(MediaPlayer mp, float delta){
        curVolume += delta;
        mp.setVolume(curVolume, curVolume);
    }

    private void fadeStepUp(MediaPlayer mp, float delta){
        curVolumeUp += delta;
        mp.setVolume(curVolumeUp, curVolumeUp);
    }

    private void fadeOut(final MediaPlayer mp, final int id) {

        final Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                if (mCurPlaying == -1) {

                    timer.cancel();
                    timer.purge();
                    return;
                }

                if (mCurPlaying == id) {
                    mp.setVolume(1f, 1f);

                    timer.cancel();
                    timer.purge();
                    return;
                }
                if (curVolume < 0.0) {

                    if (mp.isPlaying()) mp.pause();

                    timer.cancel();
                    timer.purge();
                    return;
                }
                fadeStep(mp, -mDELTA_FADE);
            }
        };

        curVolume = (float) 1.0;
        timer.schedule(timerTask, 0, 10);
    }


    private void fadeIn(final MediaPlayer mp) {
        curVolume = (float) 1.0;


        final Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                if (mCurPlaying == -1) {

                    timer.cancel();
                    timer.purge();
                    return;
                }

                if (curVolumeUp >= 1.0) {
                    timer.cancel();
                    timer.purge();

                    mp.setVolume(1f, 1f);

                    return;
                }
                fadeStepUp(mp, mDELTA_FADE);
            }
        };

        curVolumeUp = 0.0f;
        mp.setVolume(0f, 0f);
        mp.start();
        timer.schedule(timerTask, 0, 10);
    }

    public void playSong(Context context, int songId) {

        boolean beginning = false;
        int prevPlaying = mCurPlaying;
        mCurPlaying = songId;

        if (prevPlaying != -1) {
            fadeOut(mPlayer.get(prevPlaying), prevPlaying);
        }

        MediaPlayer cur = mPlayer.get(songId);
        if (beginning) cur.seekTo(0);
        fadeIn(cur);

    }

    public void stop(){
        if (mCurPlaying != -1) {
            //fadeOut(mPlayer.get(mCurPlaying), mCurPlaying);
            mPlayer.get(mCurPlaying).pause();
            mCurPlaying = -1;
        }
    }
}
