package com.squeerrl.magdalena.mmm_test;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Objects;
import java.util.Set;

public class ScreenActivity extends AppCompatActivity implements IMessageCallback {

    private BluetoothConnectionHandler btConnectionHandler;
    ListView listViewPairedDevices;
    ArrayAdapter adapterPairedDevices;
    Set<BluetoothDevice> pairedDevices;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);

        btConnectionHandler = new BluetoothConnectionHandler(this, getApplicationContext());
        btConnectionHandler.onMessageReceived(this);

        initializeLayout();
        initializeClicks();
    }

    public void initializeClicks()
    {
        listViewPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Object[] objects = pairedDevices.toArray();
                BluetoothDevice device = (BluetoothDevice) Objects.requireNonNull(objects)[position];
                btConnectionHandler.connect(device);
            }
        });
    }

    public void initializeLayout()
    {
        listViewPairedDevices = findViewById(R.id.lv_paired_devices);
        adapterPairedDevices = new ArrayAdapter(getApplicationContext(),R.layout.support_simple_spinner_dropdown_item);
        listViewPairedDevices.setAdapter(adapterPairedDevices);

        pairedDevices = btConnectionHandler.getPairedBluetoothDevices();

        if (pairedDevices.size() > 0) {

            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                adapterPairedDevices.add(deviceName + "\n" + deviceHardwareAddress);
            }
        }
    }

    @Override
    public void onMessageReceived(String msg) {
        Toast.makeText(this, "Message received: " + msg, Toast.LENGTH_SHORT).show();
    }
}