package com.example.pmendes.directionapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class OrientationSource {
    private float xAccel = 0.0f;
    private float yAccel = 0.0f;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SensorEventListener mSensorEventListener;
    private OrientationAnalyzer mOrientationAnalyzer;

    private boolean isReceivingData;

    public OrientationSource(SensorManager sensorManager, OrientationAnalyzer orientationAnalyzer) {
        mOrientationAnalyzer = orientationAnalyzer;
        mSensorManager = sensorManager;
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        PrepareListener();
    }

    private void PrepareListener() {
        mSensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                xAccel = sensorEvent.values[0];
                yAccel = sensorEvent.values[1];
                Log.d("ACCEL", "x-"+xAccel);
                Log.d("ACCEL", "y-"+yAccel);
                mOrientationAnalyzer.updateValues(xAccel, yAccel);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
    }

    public boolean IsReceivingData() {
        return isReceivingData;
    }

    public void Start() {
        mSensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
        isReceivingData = true;
    }

    public void Stop() {
        mSensorManager.unregisterListener(mSensorEventListener);
        isReceivingData = false;
    }
}
