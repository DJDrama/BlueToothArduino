package com.bluetoothapplication.www;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity implements BluetoothSPP.OnDataReceivedListener, BluetoothSPP.BluetoothConnectionListener {
    private final String TAG = MainActivity.class.getSimpleName();
    private BluetoothSPP bluetoothSPP;
    private Button connectButton;
    private boolean isDeviceConnected;

    private TextView tvLeftLeft, tvLeftRight, tvRightLeft, tvRightRight;
    private ConstraintLayout clFoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothSPP = new BluetoothSPP(this);
        if (!bluetoothSPP.isBluetoothAvailable()) {
            Log.e(TAG, "Unable to initialize Bluetooth");
        } else {
            bluetoothSPP.setBluetoothConnectionListener(this);
            bluetoothSPP.setOnDataReceivedListener(this);
        }

        clFoot = findViewById(R.id.cl_foot);
        tvLeftLeft = findViewById(R.id.tv_left_left);
        tvLeftRight = findViewById(R.id.tv_left_right);
        tvRightLeft = findViewById(R.id.tv_right_left);
        tvRightRight = findViewById(R.id.tv_right_right);

        connectButton = findViewById(R.id.button);
        connectButton.setOnClickListener(view -> {
            if (bluetoothSPP.getServiceState() == BluetoothState.STATE_CONNECTED) {
                bluetoothSPP.disconnect();
            } else {
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
        if (bluetoothSPP == null)
            return;
        if (!bluetoothSPP.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bluetoothSPP.isServiceAvailable()) {
                bluetoothSPP.setupService();
                bluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == RESULT_OK)
                bluetoothSPP.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                bluetoothSPP.setupService();
                bluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
            } else {
                Log.e(TAG, "Bluetooth not Enabled");
            }
        }

    }

    @Override
    public void onDataReceived(byte[] data, String message) {
        if (clFoot.getVisibility() == View.GONE)
            return;
        String received = message.trim();
        if (received.contains("left")) {
            message = message.replace("left", "").trim();
            String[] arr = message.split(",");
            tvLeftLeft.setText(arr[0].trim());
            tvLeftRight.setText(arr[1].trim());
        } else {
            message = message.replace("right", "").trim();
            String[] arr = message.split(",");
            tvRightLeft.setText(arr[0].trim());
            tvRightRight.setText(arr[1].trim());
        }
    }


    @Override
    public void onDeviceConnected(String name, String address) {
        Log.d(TAG, "Bluetooth Connected");
        Toast.makeText(this, "연결되었습니다.", Toast.LENGTH_SHORT).show();
        connectButton.setVisibility(View.INVISIBLE);
        clFoot.setVisibility(View.VISIBLE);
        isDeviceConnected = true;
    }

    @Override
    public void onDeviceDisconnected() {
        Log.d(TAG, "Bluetooth Disconnected");
        isDeviceConnected = false;

    }

    @Override
    public void onDeviceConnectionFailed() {
        Log.d(TAG, "Bluetooth Connection Failed");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isDeviceConnected) {
            clFoot.setVisibility(View.GONE);
            connectButton.setVisibility(View.VISIBLE);
            isDeviceConnected = false;
        }
    }
}