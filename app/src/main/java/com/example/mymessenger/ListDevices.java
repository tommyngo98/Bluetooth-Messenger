package com.example.mymessenger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

public class ListDevices extends Activity implements AdapterView.OnItemClickListener {
    private ListView mListView;
    private DeviceAdapter mAdapter;
    private ArrayList<BluetoothDevice> mDeviceList;
    public static final String TAG = "LISTDEVICES";


    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_devices);
        mDeviceList	= getIntent().getExtras().getParcelableArrayList("device.list");
        mListView = findViewById(R.id.lv_paired);

        mAdapter = new DeviceAdapter(this);
        mAdapter.setData(mDeviceList);
        mAdapter.setListener(new DeviceAdapter.OnPairButtonClickListener() {
            @Override
            public void onPairButtonClick(int position) {
                BluetoothDevice device = mDeviceList.get(position);

                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    unpairDevice(device);
                } else {
                    showToast("Pairing...");

                    pairDevice(device);
                }
            }
        });


        mListView.setAdapter(mAdapter);

        registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        mListView.setOnItemClickListener(ListDevices.this);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mPairReceiver);
        super.onDestroy();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState	 = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    showToast("Paired");
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    showToast("Unpaired");
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    public void startBTConnection(BluetoothDevice device, UUID uuid) {
            Intent i = new Intent (this, MessengerActivity.class);
            i.putExtra("btDevice", device);
            i.putExtra("uuid", MY_UUID_INSECURE);
            startActivity(i);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mDeviceList.get(i).getName();
        String deviceAddress = mDeviceList.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        startBTConnection(mDeviceList.get(i), MY_UUID_INSECURE);
    }
}