package lestera.me.mypproject;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class BluetoothMessenger {
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private Activity activity;
    private Handler mHandler; // handler that gets info from Bluetooth service
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter mAdapter;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        int MESSAGE_READ = 0;
        int MESSAGE_WRITE = 1;
        int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }

    public BluetoothMessenger(Activity activity, Handler mHandler) {
        this.mHandler = mHandler;
        this.activity = activity;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public synchronized void connect(BluetoothDevice device, boolean secure) {
        Log.d(TAG, "connect to: " + device);


        // Cancel any thread currently running a connection

        if (mConnectedThread != null) {

            mConnectedThread.cancel();

            mConnectedThread = null;

        }



        // Start the thread to connect with the given device

        mConnectThread = new ConnectThread(device, secure);

        mConnectThread.start();

        // Update UI title


    }

    public void disable() {
        // Create temporary object

        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread

        synchronized (BluetoothMessenger.this) {

            r = mConnectedThread;

        }

        // Perform the write unsynchronized
        if (r != null) r.cancel();

        mConnectedThread = null;
    }

    public void write(byte[] out) {

        // Create temporary object

        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread

        synchronized (BluetoothMessenger.this) {

            r = mConnectedThread;

        }

        // Perform the write unsynchronized

        r.write(out);

    }

    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;

        private final BluetoothDevice mmDevice;

        private String mSocketType;



        public ConnectThread(BluetoothDevice device, boolean secure) {

            mmDevice = device;

            BluetoothSocket tmp = null;

            mSocketType = secure ? "Secure" : "Insecure";



            // Get a BluetoothSocket for a connection with the

            // given BluetoothDevice

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            } catch (IOException e) {

                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);

            }

            mmSocket = tmp;


        }



        public void run() {

            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);

            setName("ConnectThread" + mSocketType);



            // Always cancel discovery because it will slow down a connection

            mAdapter.cancelDiscovery();



            // Make a connection to the BluetoothSocket

            try {

                // This is a blocking call and will only return on a

                // successful connection or an exception

                mmSocket.connect();

            } catch (IOException e) {

                // Close the socket
                Log.e(TAG, "IOException");
                try {

                    mmSocket.close();

                } catch (IOException e2) {

                    Log.e(TAG, "unable to close() " + mSocketType +

                            " socket during connection failure", e2);

                }

                BluetoothMessenger.this.activity.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                BluetoothMessenger.this.activity.runOnUiThread(() -> {
                    Toast.makeText(BluetoothMessenger.this.activity, "Can't connect to device", Toast.LENGTH_LONG).show();
                });
                return;

            }



            // Reset the ConnectThread because we're done

            synchronized (this) {

                mConnectThread = null;

            }



            // Start the connected thread

            connected(mmSocket, mmDevice, mSocketType);

        }



        public void cancel() {

            try {

                mmSocket.close();

            } catch (IOException e) {

                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);

            }

        }

    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice

            device, final String socketType) {

        Log.d(TAG, "connected, Socket Type:" + socketType);



        // Cancel the thread that completed the connection

        if (mConnectThread != null) {

            mConnectThread.cancel();

            mConnectThread = null;

        }



        // Cancel any thread currently running a connection

        if (mConnectedThread != null) {

            mConnectedThread.cancel();

            mConnectedThread = null;

        }


        // Start the thread to manage the connection and perform transmissions

        mConnectedThread = new ConnectedThread(socket);

        mConnectedThread.start();



        // Send the name of the connected device back to the UI Activity


        // Update UI title


    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = mHandler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                Log.d(TAG, "Bytes written");

                // Share the sent message with the UI activity.
                Message writtenMsg = mHandler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                mHandler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}