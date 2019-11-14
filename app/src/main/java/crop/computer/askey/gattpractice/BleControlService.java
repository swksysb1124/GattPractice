package crop.computer.askey.gattpractice;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class BleControlService extends Service {

    private static final String TAG = "LOG_TAG_" + BleControlService.class.getSimpleName();

    private BluetoothDevice mBtDevice;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mGatt;
    private List<BluetoothGattService> mGattServices;

    private String mCurrentCmd;
    private String mCurrentDataForWrite;


    /**
     * This flag is for checking GATT Server have been connected.
     *
     * Connection to GATT Server is the first step for BLE communication.
     */
    private boolean mIsGattConnected = false;

    /**
     * This flag is for checking GATT Services have been discovered.
     *
     * All BLE operations(Read/Write/Notifiable) cannot work until all services have been discovered
     */
    private boolean mIsGattServicesDiscovered = false;

    /**
     *  This flag is for checking if pending execution is needed.
     *
     *  When GATT Server is not connected or GATT services are not discovered, this flag has to set {false}
     */
    private boolean mInPendingExecution = false;

    public BleControlService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.wtf(TAG, "BLE Control Service created");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.wtf(TAG, "BLE Control Service destroyed");
        if (mIsGattConnected) {
            if (mGatt != null) {
                mGatt.disconnect();
            }
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        this.stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(TAG, "onStartCommand()");

        mInPendingExecution = false;
        String remoteBleDeviceMac = intent.getStringExtra("BLE_MAC");

        // 嘗試取得 BT/BLE Device
        if (mBtDevice == null && remoteBleDeviceMac != null) {
            mBtDevice = mBluetoothAdapter.getRemoteDevice(remoteBleDeviceMac);
            Log.w(TAG, "First connections");
            Log.w(TAG, "Remote BLE Device MAC: " + mBtDevice.getAddress());
            Log.w(TAG, "Remote BLE Device Name: " + mBtDevice.getName());
        }

        if (mBtDevice == null) {
            Log.e(TAG, "No Valid Bluetooth Le Device");
            throw new RuntimeException("No Valid Bluetooth Le Device");
        }

        if (!mIsGattConnected) {
            connectGattServer();
        }

        mCurrentCmd = intent.getStringExtra("CMD_KEY");
        mCurrentDataForWrite = intent.getStringExtra("DATA");

        Log.w(TAG, "Received command = " + mCurrentCmd);
        Log.w(TAG, "Received data = " + mCurrentDataForWrite);

        if (mIsGattConnected && mIsGattServicesDiscovered) {
            executeCmd();
        } else {
            Log.e(TAG, "Not ready to communicate yet, execute later...");
            mInPendingExecution = true;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void connectGattServer() {
        Log.e(TAG, "connecting GATT Server...");
        broadcastState("[GATT Server Connecting...]");

        // 連線 GATT Server
        mGatt = mBtDevice.connectGatt(this, false, new BluetoothGattCallback() {

            // 回調 GATT連線 狀態
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "GATT Server Connected");

                    mIsGattConnected = true;

                    mGatt.discoverServices(); // 一但連線成功就開始搜尋 GATT 服務

                    broadcastState("[GATT Server Connected]");

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.e(TAG, "GATT Server Disconnected");

                    mIsGattConnected = false;
                    mIsGattServicesDiscovered = false;

                    broadcastState("[GATT Server Disconnected]");
                }
            }

            // 回調 GATT服務搜尋 狀態
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "GATT Service Services Discovered");

                    broadcastState("[GATT Service Services Discovered]");

                    mIsGattServicesDiscovered = true;

                    mGattServices = mGatt.getServices(); // 暫存GATT服務

//                    printGattServicesAndCharacteristics();

                    if (mInPendingExecution) {
                        executeCmd();
                    }

                } else {
                    Log.e(TAG, "onServicesDiscovered received: " + status);
                    mIsGattServicesDiscovered = false;
                }
            }


            // 回調 GATT特徵值改變 (必須先設定要接收特徵值的通知)
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
            }

            // 回調 完成GATT特徵值讀取
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.w(TAG, "onCharacteristicRead()");
                final byte[] data = characteristic.getValue();

                if (data != null && data.length > 0) {
                    // 我們自訂的 Value 採用 UTF-8 編碼
                    String value = new String(data, StandardCharsets.UTF_8);
                    Log.w(TAG, "value = " + value);

                    broadcastResult(mCurrentCmd, value);
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.w(TAG, "onCharacteristicWrite()");
                broadcastResult(mCurrentCmd, mCurrentDataForWrite + " sent");
            }

        });
    }


    private void executeCmd() {
        Log.e(TAG, "execute command: " + mCurrentCmd);

        UUID service;
        UUID characteristic;

        if ("READ_SSID_2G".equals(mCurrentCmd)) {
            service = RT4436WProfile.UUID_SERVICE_WIFI_SETTING;
            characteristic = RT4436WProfile.UUID_CHARACTERISTIC_SSID_2G;

            read(service, characteristic);
        }

        if ("READ_ENCRYPTION_2G".equals(mCurrentCmd)) {
            service = RT4436WProfile.UUID_SERVICE_WIFI_SETTING;
            characteristic = RT4436WProfile.UUID_CHARACTERISTIC_ENCRYPTION_2G;

            read(service, characteristic);
        }

        if ("READ_CHANNEL_2G".equals(mCurrentCmd)) {
            service = RT4436WProfile.UUID_SERVICE_WIFI_SETTING;
            characteristic = RT4436WProfile.UUID_CHARACTERISTIC_CHANNEL_2G;

            read(service, characteristic);
        }

        if ("WRITE_SSID_2G".equals(mCurrentCmd)) {
            service = RT4436WProfile.UUID_SERVICE_WRITE_COMMAND;
            characteristic = RT4436WProfile.UUID_CHARACTERISTIC_WRITE_VALUED;

            write(service, characteristic, mCurrentDataForWrite);
        }
    }

    private void read(UUID service, UUID characteristic) {
        if (service != null && characteristic != null) {
            BluetoothGattCharacteristic bluetoothGattCharacteristic
                    = mGatt.getService(service).getCharacteristic(characteristic);

            mGatt.readCharacteristic(bluetoothGattCharacteristic);
        }
    }

    private void write(UUID service, UUID characteristic, String data) {
        if (service != null && characteristic != null) {
            BluetoothGattCharacteristic bluetoothGattCharacteristic
                    = mGatt.getService(service).getCharacteristic(characteristic);

            bluetoothGattCharacteristic.setValue(data.getBytes());

            mGatt.writeCharacteristic(bluetoothGattCharacteristic);
        }
    }

    private void printGattServicesAndCharacteristics() {
        Log.w(TAG, "==== GATT profile ====");

        for (BluetoothGattService service : mGattServices) {
            Log.w(TAG, "*****************************************");
            Log.w(TAG, "Service UUID: " + service.getUuid().toString());
            Log.w(TAG, "-----------------------------------------");
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                Log.w(TAG, "Characteristic UUID: " + characteristic.getUuid().toString());
            }
        }
    }

    public String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private void broadcastResult(String cmd, String data) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("RT4430W_ACTION_CMD_RESULT");
        broadcastIntent.putExtra("CMD", cmd);
        broadcastIntent.putExtra("DATA", data);
        sendBroadcast(broadcastIntent);
    }

    private void broadcastState(String state) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("RT4430W_ACTION_STATE");
        broadcastIntent.putExtra("STATE", state);
        sendBroadcast(broadcastIntent);
    }

}
