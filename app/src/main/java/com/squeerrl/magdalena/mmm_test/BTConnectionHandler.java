package com.squeerrl.magdalena.mmm_test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class BTConnectionHandler {

    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final int MESSAGE_READ=0;
    public static final int MESSAGE_WRITE=1;
    public static final int CONNECTING=2;
    public static final int CONNECTED=3;
    public static final int NO_SOCKET_FOUND=4;

    public static final int REQUEST_ENABLE_BT=1;
    Set<BluetoothDevice> set_pairedDevices;

    private Context mContext;
    private Activity mActivity;
    private BluetoothAdapter bluetoothAdapter;
    String bluetooth_message="00";

    @SuppressLint("HandlerLeak")
    Handler mHandler=new Handler()
    {
        @Override
        public void handleMessage(Message msg_type) {
            super.handleMessage(msg_type);

            switch (msg_type.what){
                case MESSAGE_READ:

                    byte[] readbuf=(byte[])msg_type.obj;
                    String string_recieved=new String(readbuf);

                    //do some task based on recieved string

                    break;
                case MESSAGE_WRITE:

                    if(msg_type.obj!=null){
                        ConnectedThread connectedThread=new ConnectedThread((BluetoothSocket)msg_type.obj);
                        connectedThread.write(bluetooth_message.getBytes());

                    }
                    break;

                case CONNECTED:
                    Toast.makeText(mContext,"Connected",Toast.LENGTH_SHORT).show();
                    break;

                case CONNECTING:
                    Toast.makeText(mContext,"Connecting...",Toast.LENGTH_SHORT).show();
                    break;

                case NO_SOCKET_FOUND:
                    Toast.makeText(mContext,"No socket found",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    BTConnectionHandler(Activity activity, Context context) {
        mContext = context;
        mActivity = activity;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        startAcceptingConnection();
    }

    public Set<BluetoothDevice> initializeBluetooth()
    {
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(mContext,"Your Device doesn't support bluetooth.",Toast.LENGTH_SHORT).show();
            mActivity.finish();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return null;
        } else {
            return bluetoothAdapter.getBondedDevices();
        }
    }


    public void startAcceptingConnection()
    {
        //call this on button click as suited by you
        AcceptThread acceptThread = new AcceptThread();
        acceptThread.start();
        Toast.makeText(mContext,"accepting",Toast.LENGTH_SHORT).show();
    }

    public void connect(BluetoothDevice device){

        ConnectThread connectThread = new ConnectThread(device);
        connectThread.start();

        Toast.makeText(mContext,"device choosen "+device.getName(),Toast.LENGTH_SHORT).show();
    }


    public class AcceptThread extends Thread
    {
        private final BluetoothServerSocket serverSocket;

        AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("NAME",MY_UUID);
            } catch (IOException e) {
                Toast.makeText(mContext,"Filed to connect!",Toast.LENGTH_SHORT).show();
            }
            serverSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    break;
                }

                // If a connection was accepted
                if (socket != null)
                {
                    // Do work to manage the connection (in a separate thread)
                    mHandler.obtainMessage(CONNECTED).sendToTarget();
                }
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        /**
         * @param device
         */
        ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Toast.makeText(mContext,"Filed to connect!",Toast.LENGTH_SHORT).show();
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mHandler.obtainMessage(CONNECTING).sendToTarget();

                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Toast.makeText(mContext,"Socket disconnected!",Toast.LENGTH_SHORT).show();
                }
            }

            // Do work to manage the connection (in a separate thread)
            // bluetooth_message = "Initial message"
            // mHandler.obtainMessage(MESSAGE_WRITE,mmSocket).sendToTarget();
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[2];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();

                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}