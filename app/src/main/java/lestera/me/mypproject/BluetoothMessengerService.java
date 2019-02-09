package lestera.me.mypproject;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import lestera.me.mypproject.packets.BluetoothPacket;

public class BluetoothMessengerService extends Service {
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final String TAG = "MY_APP_DEBUG_TAG";
    public static final String PREFERENCES_DEVICE_KEY = "selected_bluetooth_device";
    public static final String PREFERENCES_DEVICES_COUNT = "devices_number";

    public static class MessageConstants {
        public static final String INTENT_ACTION = "mypproject.broadcast.intent";
        public static final int TO_CONNECTION_SUCCESS = 0;
        public static final int TO_CONNECTION_FAILURE = 1;
        public static final int FROM_MSG_REGISTER_CLIENT = 2;
        public static final int FROM_MESSAGE_WRITE = 3;
        public static final int TO_MESSAGE_READ = 4;
        public static final int TO_MESSAGE_TYPE_TOAST = 5;
        public static final int TO_MESSAGE_TYPE_WRITE = 6;
    }

    public interface Reader {
        void bluetoothRead(BluetoothPacket packet);
    }

    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private Reader reader;
    private BluetoothDevice selectedDevice;
    private final IBinder binder = new BluetoothBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        disable();
    }

    @Override
    public int onStartCommand(Intent intent, int id, int startFlags) {
        return super.onStartCommand(intent, id, startFlags);
    }

    public synchronized void connect(boolean secure) {
        if (selectedDevice == null) {
            Intent intent = new Intent(MessageConstants.INTENT_ACTION);
            intent.putExtra("type", MessageConstants.TO_CONNECTION_FAILURE);
            intent.putExtra("data", getString(R.string.no_device_selected));
            LocalBroadcastManager.getInstance(BluetoothMessengerService.this.getApplicationContext()).sendBroadcast(intent);
            return;
        }
        connect(selectedDevice, secure);
    }

    public synchronized void connect(BluetoothDevice device, boolean secure) {
        Log.d(TAG, "connect to: " + device);
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        connectThread = new ConnectThread(device, secure);
        connectThread.start();
    }

    public void disable() {
        ConnectedThread r;

        synchronized (this) {
            r = connectedThread;
        }

        if (r != null) r.cancel();

        connectedThread = null;

        stopSelf();
    }

    public void write(BluetoothPacket packet) {
        write(packet.toByteArray());
    }

    public void write(byte[] out) {
        ConnectedThread r;

        synchronized (this) {
            if (!isConnected()) {
                Intent intent = new Intent(MessageConstants.INTENT_ACTION);
                intent.putExtra("type", MessageConstants.TO_MESSAGE_TYPE_TOAST);
                intent.putExtra("data", "Action Failed. Device not connected.");
                LocalBroadcastManager.getInstance(BluetoothMessengerService.this.getApplicationContext()).sendBroadcast(intent);
                return;
            }
            r = connectedThread;
        }

        r.write(out);
    }

    public boolean isConnected() {
        return connectedThread != null;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public void setSelectedDevice(BluetoothDevice device) {
        this.selectedDevice = device;
    }

    public BluetoothDevice getSelectedDevice() {
        return this.selectedDevice;
    }

    public class BluetoothBinder extends Binder {
        public BluetoothMessengerService getService() {
            return BluetoothMessengerService.this;
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;
        private String socketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            this.device = device;
            BluetoothSocket tmp = null;
            socketType = secure ? "Secure" : "Insecure";

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + socketType + "create() failed", e);
            }

            this.socket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + socketType);
            setName("ConnectThread" + socketType);

            bluetoothAdapter.cancelDiscovery();

            try {
                socket.connect();
            } catch (IOException e) {
                // Close the socket
                Log.e(TAG, "IOException");
                try {
                    socket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + socketType + " socket during connection failure", e2);
                }

                Intent intent = new Intent(MessageConstants.INTENT_ACTION);
                intent.putExtra("type", MessageConstants.TO_CONNECTION_FAILURE);
                LocalBroadcastManager.getInstance(BluetoothMessengerService.this.getApplicationContext()).sendBroadcast(intent);
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (this) {
                connectThread = null;
            }

            connected(socket, device, socketType);
        }

        public synchronized void connected(BluetoothSocket socket, BluetoothDevice device,
                                           final String socketType) {
            Log.d(TAG, "connected, Socket Type:" + socketType);
            Intent intent = new Intent(MessageConstants.INTENT_ACTION);
            intent.putExtra("type", MessageConstants.TO_CONNECTION_SUCCESS);
            LocalBroadcastManager.getInstance(BluetoothMessengerService.this.getApplicationContext()).sendBroadcast(intent);

            if (connectThread != null) {
               connectThread.cancel();
              connectThread = null;
            }

            if (connectedThread != null) {
               connectedThread.cancel();
               connectedThread = null;
            }

            connectedThread = new ConnectedThread(socket);
            connectedThread.start();
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + socketType + " socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final BufferedInputStream inputStream;
        private final BufferedOutputStream outputStream;
        private byte[] buffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

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

            this.inputStream = new BufferedInputStream(tmpIn);
            this.outputStream = new BufferedOutputStream(tmpOut);
        }

        public void run() {
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.

                    if (inputStream.available() > 2) {
                        byte type = (byte) inputStream.read();
                        byte length = (byte) inputStream.read();
                        byte[] data = new byte[length + 2];

                        data[0] = type;
                        data[1] = length;

                        inputStream.read(data, 2, length);

                        if (reader != null) {
                            reader.bluetoothRead(BluetoothPacket.obtain(data));
                        }
                    }

                } catch (IOException | ArrayIndexOutOfBoundsException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
                outputStream.flush();

                // Share the sent message with the UI activity.
                Intent sentData = new Intent(MessageConstants.INTENT_ACTION);
                sentData.putExtra("type", MessageConstants.TO_MESSAGE_TYPE_WRITE);
                sentData.putExtra("data", bytes);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sentData);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Intent sentData = new Intent(MessageConstants.INTENT_ACTION);
                sentData.putExtra("type", MessageConstants.TO_MESSAGE_TYPE_TOAST);
                sentData.putExtra("data", "Couldn't send data to other device");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sentData);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}
