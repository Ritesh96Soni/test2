package com.example.rites.test2;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import android.os.Handler;
import android.os.PowerManager;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    /* constants */
    private static final int POLL_INTERVAL = 300;

    /** running state **/
    private boolean mRunning = false;

    /** config state **/
    private int mThreshold;

    private PowerManager.WakeLock mWakeLock;

    private Handler mHandler = new Handler();

    /* References to view elements */
    private TextView mStatusView;
    private SoundLevel mDisplay;

    /* data source */
    private SoundLevelChecker mSensor;

    private Runnable mSleepTask = new Runnable() {
        public void run() {
            //Log.i("Noise", "runnable mSleepTask");

            start();
        }
    };
    private Runnable mPollTask = new Runnable() {
        public void run() {

            double amp = mSensor.getAmplitude();
            updateDisplay("Monitoring Voice...", amp);

            if ((amp > mThreshold)) {
                callForHelp();
            }

            mHandler.postDelayed(mPollTask, POLL_INTERVAL);

        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Defined SoundLevelView in main.xml file
        setContentView(R.layout.activity_main);
        mStatusView = (TextView) findViewById(R.id.status);

        // Used to record voice
        mSensor = new SoundLevelChecker();
        mDisplay = (SoundLevel) findViewById(R.id.volume);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NoiseAlert");
    }


    @Override
    public void onResume() {
        super.onResume();
        //Log.i("Noise", "==== onResume ===");

        initializeApplicationConstants();
        mDisplay.setLevel(0, mThreshold);

        if (!mRunning) {
            mRunning = true;
            start();
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        // Log.i("Noise", "==== onStop ===");

        //Stop noise monitoring
        stop();

    }

    private void start() {
        mSensor.start();
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
        mHandler.postDelayed(mPollTask, POLL_INTERVAL);
    }

    private void stop() {
        Log.i("Noise", "==== Stop Noise Monitoring===");
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        mHandler.removeCallbacks(mSleepTask);
        mHandler.removeCallbacks(mPollTask);
        mSensor.stop();
        mDisplay.setLevel(0,0);
        updateDisplay("stopped...", 0.0);
        mRunning = false;

    }


    private void initializeApplicationConstants() {
        // Set Noise Threshold
        mThreshold = 8;

    }

    private void updateDisplay(String status, double signalEMA) {
        mStatusView.setText(status);
        //
        mDisplay.setLevel((int)signalEMA, mThreshold);
    }


    private void callForHelp() {
        Toast.makeText(getApplicationContext(), "Noise Thersold Crossed, do here your stuff.",
                Toast.LENGTH_LONG).show();
    }

}

