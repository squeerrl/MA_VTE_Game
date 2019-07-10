package com.squeerrl.magdalena.mmm_test;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Objects;
import java.util.Set;

public class ScreenActivity extends AppCompatActivity {

    private BTConnectionHandler btHandler;
    ListView lvPairedDevices;
    ArrayAdapter adapterPairedDevices;
    Set<BluetoothDevice> setPairedDevices;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);

        btHandler = new BTConnectionHandler(this, getApplicationContext());

        initializeLayout();
        btHandler.startAcceptingConnection();
        initializeClicks();
    }


    public void initializeClicks()
    {
        lvPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Object[] objects = setPairedDevices.toArray();
                BluetoothDevice device = (BluetoothDevice) Objects.requireNonNull(objects)[position];
                btHandler.connect(device);
            }
        });
    }


    public void initializeLayout()
    {
        lvPairedDevices = findViewById(R.id.lv_paired_devices);
        adapterPairedDevices = new ArrayAdapter(getApplicationContext(),R.layout.support_simple_spinner_dropdown_item);
        lvPairedDevices.setAdapter(adapterPairedDevices);

        setPairedDevices = btHandler.initializeBluetooth();

        if (setPairedDevices.size() > 0) {

            for (BluetoothDevice device : setPairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                adapterPairedDevices.add(deviceName + "\n" + deviceHardwareAddress);
            }
        }
    }
}