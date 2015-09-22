package com.friendlyarm.android.Demo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.bluetooth.BluetoothDevice;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.friendlyarm.android.Demo.R;


public class BluetoothTestMainActivity extends Activity {
    private static String NANOPI_BLE_SERVICE_UUID = "09fc95c0-c111-11e3-9904-0002a5d5c51b";
    private static String CHARACTERISTIC_TX_UUID = "16fe0d80-c111-11e3-b8c8-0002a5d5c51b";
    
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;
    private static final String DEVICE_NAME = "NanoPi";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt; 
    private BluetoothGattService mBluetoothGattService; 
    private static List<BluetoothDevice> mDevices = new ArrayList<BluetoothDevice>();
    private BluetoothDevice mDevice;

    private TextView mainText;
    private Timer mTimer;
    private Button buttonRescan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainText = (TextView) findViewById(R.id.mainText);
        mainText.setText("Hello Bluetooth LE!");
        
        buttonRescan = (Button) findViewById(R.id.buttonRescan);
        buttonRescan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
            	mainText.setText("");
            	searchForDevices ();
            }
        });
        
        mTimer = new Timer();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
        }
        statusUpdate("BLE supported on this device");

        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "BLE not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth disabled", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        searchForDevices();
    }

    private void searchForDevices ()
    {
        statusUpdate("Searching for devices ...");

        if(mTimer != null) {
            mTimer.cancel();
        }

        scanLeDevice();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                statusUpdate("Search complete");
                findNanoPiBLE();
            }
        }, SCAN_PERIOD);
    }

    private void findNanoPiBLE()
    {
        if(mDevices == null || mDevices.size() == 0)
        {
            statusUpdate("No BLE devices found");
            return;
        }
        else if(mDevice == null)
        {
            statusUpdate("Unable to find NanoPi BLE");
            return;
        }
        else
        {
            statusUpdate("Found BLE");
            statusUpdate("Address: " + mDevice.getAddress());
            connectDevice();
        }
    }

    private boolean connectDevice ()
    {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDevice.getAddress());
        if (device == null) {
            statusUpdate("Unable to connect");
            return false;
        }
        
        statusUpdate("Connecting ...");
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        return true;
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                statusUpdate("Connected");
                statusUpdate("Searching for services");
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                statusUpdate("Device disconnected");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> gattServices = mBluetoothGatt.getServices();

                for(BluetoothGattService gattService : gattServices) {
                    statusUpdate("Service discovered: " + gattService.getUuid());
                    if(NANOPI_BLE_SERVICE_UUID.equals(gattService.getUuid().toString()))
                    {
                        mBluetoothGattService = gattService;
                        statusUpdate("Found communication Service");
                        sendMessage();
                    }
                }
            } else {
                statusUpdate("onServicesDiscovered received: " + status);
            }
        }
    };

    private void sendMessage ()
    {
        if (mBluetoothGattService == null)
            return;

        statusUpdate("Finding Characteristic...");
        BluetoothGattCharacteristic gattCharacteristic =
                mBluetoothGattService.getCharacteristic(UUID.fromString(CHARACTERISTIC_TX_UUID));

        if(gattCharacteristic == null) {
            statusUpdate("Couldn't find TX characteristic: " + CHARACTERISTIC_TX_UUID);
            return;
        }

        statusUpdate("Found TX characteristic: " + CHARACTERISTIC_TX_UUID);

        statusUpdate("Sending message 'Hello NanoPi BLE'");

        String msg = "Hello NanoPi BLE";

        byte b = 0x00;
        byte[] temp = msg.getBytes();
        byte[] tx = new byte[temp.length + 1];
        tx[0] = b;

        for(int i = 0; i < temp.length; i++)
            tx[i+1] = temp[i];

        gattCharacteristic.setValue(tx);
        mBluetoothGatt.writeCharacteristic(gattCharacteristic);
    }

    private void scanLeDevice() {
        new Thread() {

            @Override
            public void run() {
                mBluetoothAdapter.startLeScan(mLeScanCallback);

                try {
                    Thread.sleep(SCAN_PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }.start();
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                             byte[] scanRecord) {
            if (device != null) {
                if (mDevices.indexOf(device) == -1)//to avoid duplicate entries
                {
                    if (DEVICE_NAME.equals(device.getName())) {
                        mDevice = device;//we found our device!
                    }
                    mDevices.add(device);
                    statusUpdate("Found device " + device.getName());
                }
            }
        }
    };

    //output helper method
    private void statusUpdate (final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.w("BLE", msg);
                mainText.setText(mainText.getText() + "\r\n" + msg);
            }
        });
    }
}
