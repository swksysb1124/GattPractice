package crop.computer.askey.gattpractice;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public abstract class BaseBluetoothActivity extends AppCompatActivity {

    private static final String TAG = "LOG_TAG_"+BaseBluetoothActivity.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;

    private static final int REQUEST_CODE_OF_LOCATION_PERMISSION_FOR_BT_SCANNING = 0x10;
    private static final int REQUEST_CODE_FOR_ENABLE_BLUETOOTH = 0x20;

    private boolean isScanningBluetoothDevice = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intBluetoothAdapter();
    }

    protected void intBluetoothAdapter() {

        Log.i(TAG, "intBluetoothAdapter()");

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if(bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }

        if(mBluetoothAdapter == null) {
            Log.i(TAG, "Initialize Bluetooth Adapter Failed");
            finish();
        }else {
            Log.i(TAG, "Initialize Bluetooth Adapter Success");
        }
    }

    protected void enableBluetooth() {
        Log.i(TAG, "enableBluetooth()");

        if(!isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_CODE_FOR_ENABLE_BLUETOOTH);
        }else {
            onBluetoothEnabled(true);
        }
    }

    protected boolean isBluetoothEnabled() {
        if(mBluetoothAdapter != null) {
            return mBluetoothAdapter.isEnabled();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_CODE_FOR_ENABLE_BLUETOOTH) {
            if(resultCode == RESULT_OK) {
                Toast.makeText(this, "已開啟藍芽", Toast.LENGTH_SHORT).show();
                onBluetoothEnabled(true);
            }else if(resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "取消啟動藍芽", Toast.LENGTH_SHORT).show();
                onBluetoothEnabled(false);
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected void startScanDevices() {
        Log.i(TAG, "startScanDevices()");

        if(isScanningBluetoothDevice) {
            Log.w(TAG, "scanning has been started");
        }


        if(PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_CODE_OF_LOCATION_PERMISSION_FOR_BT_SCANNING);
            return;

        }

        if(isBluetoothEnabled()) {
            scanBluetoothDevices();
        }else {
            showScanWhenBluetoothDisableDialog();
        }
    }

    private void scanBluetoothDevices() {
        Log.w("TAG","start to scan");
        if(mBluetoothAdapter != null) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);

            isScanningBluetoothDevice = true;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.w("TAG", "stop to scan");
                    onBluetoothDeviceScanFinished();
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    isScanningBluetoothDevice = false;
                }
            }, 5000);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord){
            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    onBluetoothDeviceScanSuccess(device);
                }
            });
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.i(TAG, "onRequestPermissionsResult()");
        if(requestCode == REQUEST_CODE_OF_LOCATION_PERMISSION_FOR_BT_SCANNING) {
            if(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.e(TAG, "get granted()");

                if(isBluetoothEnabled()) {
                    scanBluetoothDevices();
                }else {
                    showScanWhenBluetoothDisableDialog();
                }

            }else {
                showBluetoothScanWhenLocationPermissionDeniedDialog();
            }
        }
    }

    abstract protected void onBluetoothEnabled(boolean enabled);

    abstract protected void onBluetoothDeviceScanSuccess(BluetoothDevice device);

    abstract protected void onBluetoothDeviceScanFinished();

    //TODO: Override it for the real case
    protected void showScanWhenBluetoothDisableDialog() {

        Log.i(TAG, "showScanWhenBluetoothDisableDialog()");
        new AlertDialog.Builder(this)
                .setTitle("尚未開啟藍芽")
                .setMessage("請開啟藍芽，再重新掃描。")
                .create().show();
    }

    //TODO: Override it for the real case
    protected void showBluetoothScanWhenLocationPermissionDeniedDialog() {

        Log.i(TAG, "showBluetoothScanWhenLocationPermissionDeniedDialog()");
        new AlertDialog.Builder(this)
                .setTitle("無法掃描周邊藍芽裝置")
                .setMessage("請開啟允許[位置]權限，可以致[設定]頁手動開啟")
                .create().show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(isScanningBluetoothDevice) {
            if(mBluetoothAdapter != null) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                isScanningBluetoothDevice = false;
            }
        }
    }
}
