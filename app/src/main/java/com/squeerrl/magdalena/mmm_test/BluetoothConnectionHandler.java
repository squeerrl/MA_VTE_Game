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
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnectionHandler {

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int MESSAGE_READ = 0;
    private static final int MESSAGE_WRITE = 1;
    private static final int CONNECTING = 2;
    private static final int CONNECTED = 3;
    private static final int NO_SOCKET_FOUND = 4;
    private static final int REQUEST_ENABLE_BT = 1;

    private Context mContext;
    private Activity mActivity;
    private BluetoothAdapter bluetoothAdapter;
    private String bluetooth_message = "00";

    private ConnectedThread connectedThread;
    private AcceptThread acceptThread;
    private IMessageCallback messageCallback;

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg_type) {
            super.handleMessage(msg_type);

            switch (msg_type.what) {
                case MESSAGE_READ:

                    byte[] readbuf = (byte[]) msg_type.obj;
                    String stringReceived = new String(readbuf);

                    //do some task based on recieved string
                    Log.d("[Connection Handler]", "message received!!" + stringReceived);
                    if (messageCallback != null) {
                        messageCallback.onMessageReceived(stringReceived);
                    }

                    break;
                case CONNECTED:
                    Toast.makeText(mContext, "Connected", Toast.LENGTH_SHORT).show();
                    write("Hallo".getBytes());
                    break;

                case CONNECTING:
                    Toast.makeText(mContext, "Connecting...", Toast.LENGTH_SHORT).show();
                    break;

                case NO_SOCKET_FOUND:
                    Toast.makeText(mContext, "No socket found", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    BluetoothConnectionHandler(Activity activity, Context context) {
        mContext = context;
        mActivity = activity;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // start accepting connections
        acceptThread = new AcceptThread();
        acceptThread.start();
        Toast.makeText(mContext, "accepting", Toast.LENGTH_SHORT).show();
    }


    public Set<BluetoothDevice> getPairedBluetoothDevices() {
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(mContext, "Your Device doesn't support bluetooth.", Toast.LENGTH_SHORT).show();
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

    public void connect(BluetoothDevice device) {

        ConnectThread connectThread = new ConnectThread(device);
        connectThread.start();

        Toast.makeText(mContext, "device choosen " + device.getName(), Toast.LENGTH_SHORT).show();
    }

    public void onMessageReceived(IMessageCallback callback) {
        messageCallback = callback;
    }

    public void write(byte[] msg) {
        if (connectedThread != null) {
            connectedThread.write(msg);
        }
    }

    public void destory() {
        connectedThread.cancel();
        acceptThread.cancel();
    }


    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;
        private boolean isRunning = true;

        AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("NAME", MY_UUID);
            } catch (IOException e) {
                Toast.makeText(mContext, "Filed to connect!", Toast.LENGTH_SHORT).show();
            }
            serverSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (isRunning) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    connectedThread = new ConnectedThread(socket);
                    connectedThread.start();
                }
            }
        }

        private void cancel() {
            isRunning = false;
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
                Toast.makeText(mContext, "Filed to connect!", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(mContext, "Socket disconnected!", Toast.LENGTH_SHORT).show();
                }
            }

            // Do work to manage the connection (in a separate thread)
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.start();
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean isRunning = true;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mHandler.obtainMessage(CONNECTED).sendToTarget();
        }

        public void run() {
            byte[] buffer = new byte[2];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (isRunning) {
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
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            this.isRunning = false;
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}