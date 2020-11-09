package com.bluetoothapplication.www;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity implements BluetoothSPP.OnDataReceivedListener, BluetoothSPP.BluetoothConnectionListener {
    private final String TAG = MainActivity.class.getSimpleName();
    private BluetoothSPP bluetoothSPP;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothSPP = new BluetoothSPP(this);
        if(!bluetoothSPP.isBluetoothAvailable()){
            Log.e(TAG, "Unable to initialize Bluetooth");
        }else{
            bluetoothSPP.setBluetoothConnectionListener(this);
            bluetoothSPP.setOnDataReceivedListener(this);
        }

        Button connectButton = findViewById(R.id.button);
        connectButton.setOnClickListener(view -> {
            if(bluetoothSPP.getServiceState() == BluetoothState.STATE_CONNECTED){
                bluetoothSPP.disconnect();
            }else{
                Intent intent = new Intent(this, DeviceList.class);
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothSPP.stopService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!bluetoothSPP.isBluetoothEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        }else{
            if(!bluetoothSPP.isServiceAvailable()){
                bluetoothSPP.setupService();
                bluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE){
            if(resultCode == RESULT_OK)
                bluetoothSPP.connect(data);
        }else if(requestCode == BluetoothState.REQUEST_ENABLE_BT){
            if(resultCode == RESULT_OK){
                bluetoothSPP.setupService();
                bluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
            }else{
                Log.e(TAG, "Bluetooth not Enabled");
            }
        }

    }

    @Override
    public void onDataReceived(byte[] data, String message) {
        Toast.makeText(this, "Recieved : " + data + " " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onDeviceConnected(String name, String address) {
        Log.d(TAG, "Bluetooth Connected");
    }

    @Override
    public void onDeviceDisconnected() {
        Log.d(TAG, "Bluetooth Disconnected");

    }

    @Override
    public void onDeviceConnectionFailed() {
        Log.d(TAG, "Bluetooth Connection Failed");
    }
}