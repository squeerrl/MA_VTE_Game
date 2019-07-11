package com.squeerrl.magdalena.mmm_test;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magneticField;

    public TextView accelerometerX;
    public TextView accelerometerY;
    public TextView accelerometerMove;

    private int accelY;
    private int accelY_previous=0;
    float min_threshold = 0;
    float max_threshold = 0;
    private float rangeOfMotion = 0;

    boolean topOfThreshold;
    boolean bottomOfThreshold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        accelerometerX = (TextView) findViewById(R.id.accelerometerX);
        accelerometerY = (TextView) findViewById(R.id.accelerometerY);
        accelerometerMove = (TextView) findViewById(R.id.move);

    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        String sensorName = sensorEvent.sensor.getName();
        //Log.d("[from onSensorChanged]", sensorName + ": X: " + sensorEvent.values[0] + "; Y: " + sensorEvent.values[1] + "; Z: " + sensorEvent.values[2] + ";");
        //Log.d("[from onSensorChanged]", "X: " + sensorEvent.values[0] + "; Y: " + sensorEvent.values[1] + "; Z: " + sensorEvent.values[2] + ";");
        //Log.d("[from onSensorChanged]", "Name: " + sensorName + " X: " + sensorEvent.values[0] + "; Y: " + sensorEvent.values[1] + "; Z: " + sensorEvent.values[2] + ";");
        accelerometerY.setText(String.valueOf(accelY));

        //---------------------
        //Get Value
        accelY = (int)sensorEvent.values[1];

        //Setting Range of Motion
        if (accelY < min_threshold) {
            min_threshold = accelY;
        }
        if (accelY > max_threshold) {
            max_threshold = accelY;
        }
        rangeOfMotion = Math.abs(max_threshold) + Math.abs(min_threshold);

        // Begrenzung/Deckelung der Range of Motion (mehr ist nicht notwenig)
        if (rangeOfMotion > 5){
            rangeOfMotion = 5;
        }

        //---------------------------
        //Bewegung
        //Falls sich der Wert veränder hat, Bewege
        if(accelY != accelY_previous)
        {
            if(Math.abs(accelY - accelY_previous) > (rangeOfMotion/2))
            {
                //Bewegung
                Log.d("[from speedCalculator]", "Y: " + accelY + " previous Y: " + accelY_previous);
                accelerometerMove.setText(String.valueOf(Math.abs(accelY - accelY_previous)));

                //Bewegung ausklingen lassen?
            }

        }
        accelY_previous = accelY;
    }

    private void calculateSpeed(float accel, float accel_previous) {
        float speed = Math.abs(accel-accel_previous);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
