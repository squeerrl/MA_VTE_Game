package com.squeerrl.magdalena.mmm_test;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity  {

    private SensorManager sensorManager;
    private Sensor sensorBeschleunigung;
    private Sensor sensorGyro;
    public TextView info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        sensorBeschleunigung = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


        info = (TextView) findViewById(R.id.textViewInfo);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){

            info.setText("Gefunden " + sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE).getName());

        } else {
            info.setText("Nichts Gefunden");
        }
    }

    /*@Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener((SensorEventListener) this);
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
