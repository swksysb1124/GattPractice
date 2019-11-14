package crop.computer.askey.gattpractice;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends BaseBluetoothActivity {

    private BluetoothDevice mBluetoothDevice;

    private TextView txtResult;

    private EditText edtSSID2G;

    private boolean isBleControlServiceStarted = false;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            String message = null;

            if("RT4430W_ACTION_STATE".equals(action)){

                String state = intent.getStringExtra("STATE");

                message = String.format("[STATE: %s]",state);

            }else if("RT4430W_ACTION_CMD_RESULT".equals(action)) {
                String cmd = intent.getStringExtra("CMD");
                String data = intent.getStringExtra("DATA");

                message = String.format("[CMD: %s, DATA: %s]",cmd, data);
            }

            if(message != null) {
                txtResult.append(message + "\n");
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtResult = findViewById(R.id.txtResult);
        txtResult.setMovementMethod(new ScrollingMovementMethod());

        edtSSID2G = findViewById(R.id.edtSSID2G);

        findViewById(R.id.btnScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanDevices();
            }
        });

        findViewById(R.id.btnReadSSID2G).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readSsid2G();
            }
        });

        findViewById(R.id.btnReadEncryption2G).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readEncryption2G();
            }
        });

        findViewById(R.id.btnReadChannel2G).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readChannel2G();
            }
        });

        findViewById(R.id.btnWriteSSID2G).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ssid2G = edtSSID2G.getText().toString();
                writeSsid2G(ssid2G);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(isBleControlServiceStarted) {
            stopService(new Intent(this, BleControlService.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("RT4430W_ACTION_CMD_RESULT");
        filter.addAction("RT4430W_ACTION_STATE");

        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onBluetoothEnabled(boolean enabled) {

    }

    @Override
    protected void onBluetoothDeviceScanSuccess(BluetoothDevice device) {

        if(device.getName()!= null && device.getName().contains("RT4430W")) {
            txtResult.append(device.getName() + "/" + device.getAddress() + "\n");
            mBluetoothDevice = device;
        }
    }

    @Override
    protected void onBluetoothDeviceScanFinished() {
        if(mBluetoothDevice != null) {
            txtResult.setText("[Scanning Finish]\n");
            txtResult.setText("[Device Found] Name: "+mBluetoothDevice.getName()+"\n");
        }
    }


    private void readSsid2G() {
        isBleControlServiceStarted = true;

        Intent intent = new Intent(this, BleControlService.class);
        intent.putExtra("BLE_MAC", mBluetoothDevice.getAddress());
        intent.putExtra("CMD_KEY", "READ_SSID_2G");
        startService(intent);

    }

    private void readEncryption2G() {
        isBleControlServiceStarted = true;

        Intent intent = new Intent(this, BleControlService.class);
        intent.putExtra("BLE_MAC", mBluetoothDevice.getAddress());
        intent.putExtra("CMD_KEY", "READ_ENCRYPTION_2G");
        startService(intent);

    }

    private void readChannel2G() {
        isBleControlServiceStarted = true;

        Intent intent = new Intent(this, BleControlService.class);
        intent.putExtra("BLE_MAC", mBluetoothDevice.getAddress());
        intent.putExtra("CMD_KEY", "READ_CHANNEL_2G");
        startService(intent);

    }

    private void writeSsid2G(String ssid2G) {
        isBleControlServiceStarted = true;

        Intent intent = new Intent(this, BleControlService.class);
        intent.putExtra("BLE_MAC", mBluetoothDevice.getAddress());
        intent.putExtra("CMD_KEY", "WRITE_SSID_2G");
        intent.putExtra("DATA", ssid2G);
        startService(intent);

    }

}
