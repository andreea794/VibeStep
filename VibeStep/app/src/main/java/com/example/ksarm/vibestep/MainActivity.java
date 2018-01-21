package com.example.ksarm.vibestep;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntegerRes;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.jackandphantom.circularprogressbar.CircleProgressbar;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    private GoogleApiClient mApiClient;
    private TextView txt1;
    private int stepValue;
    private ObservableSpeed walk;
    private TextView countTv;
    private Walk currentWalkType;
    private int initial;

    private Date lastRecord = null;
    private int speed;
    private OnDataPointListener dataListener1 = new OnDataPointListener() {
        @Override
        public void onDataPoint(DataPoint dataPoint) {
            for( final Field field : dataPoint.getDataType().getFields() ) {
                final Value value = dataPoint.getValue( field );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        stepValue = value.asInt();
                        Log.e("stepValue: ",Integer.toString(stepValue));
                        int m = stepValue;

                        int changeInStep = stepValue - initial;

                        Toast.makeText(getApplicationContext(), "Field: " + field.getName() + " Value: " + changeInStep, Toast.LENGTH_SHORT).show();
                        speed=changeInStep;

                        Log.i("a",Integer.toString(changeInStep));
                        if (changeInStep == Thresholds.STATIONARY)
                            currentWalkType = Walk.STATIONARY;

                        else if (changeInStep < Thresholds.SLOW_WALK)
                            currentWalkType = Walk.SLOW_WALK;
                        else if (changeInStep < Thresholds.FAST_WALK)
                            currentWalkType = Walk.FAST_WALK;
                        else if (changeInStep < Thresholds.RUN)
                            currentWalkType = Walk.RUN;
                        else
                            currentWalkType = Walk.SPRINT;

                        initial = m;
                        updateTextView(currentWalkType.toString() + "; " + changeInStep);

                        onInformationReceived();
                        walk.set(currentWalkType);
                    }
                });
            }
        }
    };


    HehPlayer player;

    CircleProgressbar circleProgressbar;
    ImageView playButton;

    private boolean mPlaying = false;
    private void changeState() {
        mPlaying = !mPlaying;

        int imageId = (!mPlaying) ? R.drawable.ic_play_arrow_black_48dp : R.drawable.ic_pause_black_48dp;
        playButton.setBackground(ContextCompat.getDrawable(this, imageId));

        if (!mPlaying){
            if (player != null) {
                player.stop();
            }
        }
        else {
            if (player == null) {
                initPlayer();
            }
            player.playSong(this, R.raw.stationary);
        }
    }

    private void initPlayer() {
        LinkedList<Integer> ids = new LinkedList<>();
        ids.addLast(R.raw.running);
        ids.addLast(R.raw.stationary);
        ids.addLast(R.raw.walking);

        player = new HehPlayer(this, ids);
    }
    private void initialization() {

        initPlayer();

        circleProgressbar = (CircleProgressbar) findViewById(R.id.pbProgress);
        circleProgressbar.setProgress(0);
        circleProgressbar.setProgressWithAnimation(100, 2000); // Default duration = 1500ms

        playButton = (ImageView) findViewById(R.id.ivPlay);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeState();
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.music) != null){
            if (savedInstanceState != null){
                return;
            }
        }






        initialization();

        countTv = (TextView) findViewById(R.id.txt1);
        countTv.setText("initial");
        initial = 0;

        walk = new ObservableSpeed();
        walk.set(Walk.STATIONARY);

        walk.setOnSpeedChangeListener(new OnSpeedChangeListener()
        {
            @Override
            public void onSpeedChanged(Walk newValue) {
                // public void onSpeedChanged(walk newValue) {
                //.equals

                Log.i("debug", "called " + newValue);
                if (!mPlaying) return;

                if (newValue == Walk.STATIONARY){
                    player.playSong(MainActivity.this, R.raw.stationary);
                } else if (newValue == Walk.SLOW_WALK){
                    player.playSong(MainActivity.this, R.raw.walking);

                } else if (newValue == Walk.FAST_WALK){
                    player.playSong(MainActivity.this, R.raw.walking);

                } else if (newValue == Walk.RUN){
                    player.playSong(MainActivity.this, R.raw.running);

                }  else if (newValue == Walk.SPRINT){
                    player.playSong(MainActivity.this, R.raw.running);
                }

            }
        });


        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.SENSORS_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public int getStepCount(){
        Log.e("b",Integer.toString(stepValue));
        return stepValue;
    }

    public void updateTextView(String text) {
        countTv.setText(String.format("Walk: %s", text));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mApiClient.connect();
    }
    @Override
    public void onConnected(Bundle bundle) {

        DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                .setDataTypes( DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setDataSourceTypes(DataSource.TYPE_RAW)
                .build();

        ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                for( DataSource dataSource : dataSourcesResult.getDataSources() ) {
                    Log.i("datatype",dataSource.toString());
                    if( DataType.TYPE_STEP_COUNT_CUMULATIVE.equals(dataSource.getDataType()) ) {
                        registerFitnessDataListener(dataSource, DataType.TYPE_STEP_COUNT_CUMULATIVE);
                    }
                }
            }
        };

        Fitness.SensorsApi.findDataSources(mApiClient, dataSourceRequest)
                .setResultCallback(dataSourcesResultCallback);
    }

    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {

        SensorRequest request = new SensorRequest.Builder()
                .setDataSource(dataSource)
                .setDataType(dataType)
                .setSamplingRate(2, TimeUnit.SECONDS)
                .build();

        Fitness.SensorsApi.add(mApiClient, request, dataListener1)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.e("GoogleFit", "SensorApi successfully added");
                        }
                    }
                });
    }


    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if( !authInProgress ) {
            try {
                authInProgress = true;
                connectionResult.startResolutionForResult( MainActivity.this, REQUEST_OAUTH );
            } catch(IntentSender.SendIntentException e ) {

            }
        } else {
            Log.e( "GoogleFit", "authInProgress" );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == REQUEST_OAUTH ) {
            authInProgress = false;
            if( resultCode == RESULT_OK ) {
                if( !mApiClient.isConnecting() && !mApiClient.isConnected() ) {
                    mApiClient.connect();
                }
            } else if( resultCode == RESULT_CANCELED ) {
                Log.e( "GoogleFit", "RESULT_CANCELED" );
            }
        } else {
            Log.e("GoogleFit", "requestCode NOT request_oauth");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        Fitness.SensorsApi.remove( mApiClient, dataListener1 )
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.e("GoogleFit","SensorsAPI disconnected");
                            mApiClient.disconnect();
                        }
                    }
                });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }



    private void onInformationReceived() {
        if (lastRecord == null) {
            lastRecord = Calendar.getInstance().getTime();
            return;
        }

        Date curRecord = Calendar.getInstance().getTime();
        long elapsed = curRecord.getTime() - lastRecord.getTime();
        float stepsPerSecond = (float)speed / ((float)elapsed / 1000f); /// elapsed is in milliseconds

        if (stepsPerSecond == Thresholds.STATIONARY)
            currentWalkType = Walk.STATIONARY;
        else if (stepsPerSecond < Thresholds.SLOW_WALK)
            currentWalkType = Walk.SLOW_WALK;
        else if (stepsPerSecond < Thresholds.FAST_WALK)
            currentWalkType = Walk.FAST_WALK;
        else if (stepsPerSecond < Thresholds.RUN)
            currentWalkType = Walk.RUN;
        else
            currentWalkType = Walk.SPRINT;

        //circleProgressbar.setProgress( Math.min(1f, stepsPerSecond / Thresholds.SPRINT) * 100f );
    }

 }
