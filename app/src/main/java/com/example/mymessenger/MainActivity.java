package com.example.mymessenger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import android.app.Activity;
import android.app.ProgressDialog;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_DISCOVER_BT = 1;

    private TextView mStatusTv;
    private Button mActivateBtn, mPairedBtn, mScanBtn, mDiscoverableBtn;

    private ProgressDialog mProgressDlg;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private BluetoothAdapter mBluetoothAdapter;

    ConnectionService mConnection;
    BluetoothDevice device;
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStatusTv = findViewById(R.id.tv_status);
        mActivateBtn = findViewById(R.id.btn_enable);
        mPairedBtn 	= findViewById(R.id.btn_view_paired);
        mScanBtn = findViewById(R.id.btn_scan);
        mDiscoverableBtn = findViewById(R.id.discoverable);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mProgressDlg = new ProgressDialog(this);

        mProgressDlg.setMessage("Scanning...");
        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mBluetoothAdapter.cancelDiscovery();
            }
        });

        if (mBluetoothAdapter == null) {
            showUnsupported();
        }
        else {
            mPairedBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                    if (pairedDevices == null || pairedDevices.size() == 0) {
                        showToast("No Paired Devices Found");
                    }
                    else {
                        ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();
                        list.addAll(pairedDevices);
                        Intent intent = new Intent(MainActivity.this, ListDevices.class);
                        intent.putParcelableArrayListExtra("device.list", list);
                        startActivity(intent);
                    }
                }
            });

            mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                    mBluetoothAdapter.startDiscovery();
                }
            });

            mDiscoverableBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!mBluetoothAdapter.isDiscovering()) {
                        showToast("Making your Device Discoverable");
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        intent.putExtra(mBluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                        startActivityForResult(intent, REQUEST_DISCOVER_BT);
                    }
                }
            });

            mActivateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.disable();
                        showDisabled();
                    } else {
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, 1000);
                        showEnabled();
                    }
                }
            });



        }

        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onPause() {
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void showEnabled() {
        mStatusTv.setText("Bluetooth is On");
        mStatusTv.setTextColor(Color.BLUE);

        mActivateBtn.setText("Disable");
        mActivateBtn.setEnabled(true);

        mDiscoverableBtn.setEnabled(true);
        mPairedBtn.setEnabled(true);
        mScanBtn.setEnabled(true);

    }

    private void showDisabled() {
        mStatusTv.setText("Bluetooth is Off");
        mStatusTv.setTextColor(Color.RED);

        mActivateBtn.setText("Enable");
        mActivateBtn.setEnabled(true);

        mDiscoverableBtn.setEnabled(false);
        mPairedBtn.setEnabled(false);
        mScanBtn.setEnabled(false);
    }

    private void showUnsupported() {
        mStatusTv.setText("Bluetooth is unsupported by this device");

        mActivateBtn.setText("Enable");
        mActivateBtn.setEnabled(false);

        mPairedBtn.setEnabled(false);
        mScanBtn.setEnabled(false);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void openMessengerActivity(){
        Intent intent = new Intent(this, MessengerActivity.class);
        startActivity(intent);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                    showToast("Enabled");
                    showEnabled();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList = new ArrayList<BluetoothDevice>();
                mProgressDlg.show();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mProgressDlg.dismiss();
                Intent newIntent = new Intent(MainActivity.this, ListDevices.class);
                newIntent.putParcelableArrayListExtra("device.list", mDeviceList);
                startActivity(newIntent);
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add(device);
                showToast("Found device " + device.getName());
            }
        }
    };

}