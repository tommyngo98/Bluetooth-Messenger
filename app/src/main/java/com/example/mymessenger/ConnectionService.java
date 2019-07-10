package com.example.mymessenger;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.renderscript.ScriptGroup;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class ConnectionService {
    private static final String TAG = "ConnecttionService";

    private static final String appName = "DecentMessenger";
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private AcceptThread mInsecureAcceptThread;

    private ConnectThread mConnectThread;
    private BluetoothDevice mDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;

    public ConnectionService(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            try{
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            mServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;
            try{
                socket = mServerSocket.accept();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            if (socket != null) {
                connected(socket, mDevice);
            }
        }

        public void cancel() {
            try {
                mServerSocket.close();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectThread extends Thread {

        private BluetoothSocket mSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mDevice = device;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;

            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        +MY_UUID_INSECURE );
                tmp = mDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSocket = tmp;
            mBluetoothAdapter.cancelDiscovery();
            try {
                mSocket.connect();

                Log.d(TAG, "run: ConnectThread connected.");

            } catch (IOException e) {
                try {
                    mSocket.close();
                }
                catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
            }

            connected(mSocket, mDevice);
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void start() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    public void startClient(BluetoothDevice device, UUID uuid) {

        mProgressDialog = ProgressDialog.show(mContext, "Connecting Bluetooth", "Please Wait...", true);
        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();

    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInput;
        private final OutputStream mOutput;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            mProgressDialog.dismiss();

            try {
                tmpIn = mSocket.getInputStream();
                tmpOut = mSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mInput = tmpIn;
            mOutput = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while(true) {
                try {
                    bytes = mInput.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d("Messenger", "eingehende Nachricht " + incomingMessage);

                    Intent inComingMessageIntent = new Intent("incomingMessage");
                    inComingMessageIntent.putExtra("theMessage", incomingMessage);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(inComingMessageIntent);

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            try {
                mOutput.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                mSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void connected(BluetoothSocket socket, BluetoothDevice mDevice) {
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
    }

    public void write(byte[] out) {
        ConnectedThread r;
        mConnectedThread.write(out);
    }
}
