package com.example.thomashuu.bluetoothapp;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private BluetoothAdapter mBtAdapter;
    private TextView TV_state;// TV_devices, TV_otherDevices;
    private ListView deviceLV, otherDeviceLV;
    private List<Map<String, Object>> deviceList, otherDeviceList;
    private Map<String, Object> deviceMap, recordMap1, recordMap2, totalMap;
    private String[] devNames;
    private String address;
    private ListAdapter lAdapter_bd, lAdapter_od;
//    private TextView test;

    // 连接到的蓝牙设备的名称
    private String mConnectedDeviceName;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        test = (TextView) findViewById(R.id.test);

        deviceLV = (ListView) findViewById(R.id.list_bondedDevices);
        deviceList = new ArrayList<>();
        lAdapter_bd = new ListAdapter(this);

        otherDeviceLV = (ListView) findViewById(R.id.list_otherDevices);
        otherDeviceList = new ArrayList<>();
        lAdapter_od = new ListAdapter(this);

        deviceMap = new HashMap<>();
        recordMap1 = new HashMap<>();
        recordMap2 = new HashMap<>();

        totalMap = new HashMap<>();
        TV_state = (TextView) findViewById(R.id.search_state);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter intentFilter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, intentFilter1);
        IntentFilter intentFilter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, intentFilter2);

    }


    public void clickToSearch(View v) {
        Button button = (Button) findViewById(R.id.btn_search);
        if(button.getText().toString().equals("search")) {
            //open bluetooth
            Log.i("hfq", button.getText().toString());
            if(!mBtAdapter.isEnabled()) {
                mBtAdapter.enable();
                Toast.makeText(MainActivity.this, "your bluetooth has been opened by app", Toast.LENGTH_LONG).show();
            }
            recordMap2.clear();
            recordMap1.clear();
            mBtAdapter.startDiscovery();
            TV_state.setText("searching...");
            Log.i("hfq","started");
            button.setText("stop");
        }
        if (button.getText().toString().equals("stop")) {
            mBtAdapter.cancelDiscovery();
            button.setText("search");
        }

    }

    private void ensureDiscoverable() {
        if (mBtAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            Log.e("hfq", action);
            if(action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(device.getBondState()==BluetoothDevice.BOND_BONDED){
                    //if the device has been paired
//                    TV_devices.append("\n" + device.getName() + "==>" + device.getAddress() + "\n");

                    if(!recordMap1.containsKey(device.getName())) {
                        recordMap1.put(device.getName(), device.getAddress());
                        deviceMap.put("name", device.getName());
                        deviceMap.put("address", device.getAddress());
                        deviceList.add(deviceMap);
                        deviceMap = new HashMap<>();
                        Toast.makeText(MainActivity.this, device.getName(), Toast.LENGTH_SHORT).show();
                    }

                } else if(device.getBondState() != BluetoothDevice.BOND_BONDED) {
//                    TV_otherDevices.append("\n" + device.getName() + "==>" + device.getAddress() + "\n");
                    if(!recordMap2.containsKey(device.getName())) {
                        recordMap2.put(device.getName(), device.getAddress());
                        deviceMap.put("name", device.getName());
                        deviceMap.put("address", device.getAddress());
                        otherDeviceList.add(deviceMap);
                        deviceMap = new HashMap<>();
                        Toast.makeText(MainActivity.this, device.getName(), Toast.LENGTH_SHORT).show();
                    }
                }
            }else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                TV_state.setText("finished");
                onDestroy();
                lAdapter_bd.setList(deviceList);
                deviceLV.setAdapter(lAdapter_bd);
                lAdapter_od.setList(otherDeviceList);
                otherDeviceLV.setAdapter(lAdapter_od);
            }
        }
    };

    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
        Log.e("hfq", "unregistered");
    }

    public void clickToStop(View v) {
        mBtAdapter.cancelDiscovery();
        TV_state.setText("stopped");
    }

    @Override
    protected synchronized void onResume() {
        super.onResume();
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity
        // returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't
            // started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    public void clickToCreate(View v) {

        final EditText et = new EditText(this);
        et.setSingleLine();
        et.setText(mBtAdapter.getName());

        new AlertDialog.Builder(this)
                .setTitle("请输入房间名：")
                .setView(et)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                String name = et.getText().toString()
                                        .trim();
                                if (name.equals("")) {
                                    Toast.makeText(MainActivity.this,
                                            "请输入房间名",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                // 设置房间名
                                mBtAdapter.setName(name);
                            }

                        })
                .setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {

                            }
                        }).create().show();

        // 创建连接，也就是设备本地蓝牙设备可被其他用户的蓝牙搜到
        ensureDiscoverable();
    }

    private void linkDevice() {
        if (mBtAdapter.isDiscovering())
            mBtAdapter.cancelDiscovery();
        int cou = recordMap1.size() + recordMap2.size();

        //add all the devices in an hashMap, for dialog list;
        devNames = new String[cou]; int i = 0;
        for (String s: recordMap1.keySet()) {totalMap.put(s, recordMap1.get(s));}
        for (String s: recordMap2.keySet()) {totalMap.put(s, recordMap2.get(s));}
        for (String s: totalMap.keySet()) {
            devNames[i] = s;
            i += 1;
        }

        new AlertDialog.Builder(this)
                .setTitle("FoundBluetoothDevices")
                .setSingleChoiceItems(devNames, 0,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // 当用户点击选中的蓝牙设备时，取出选中的蓝牙设备的MAC地址
                                address = (String) totalMap.get(devNames[which]);
                            }
                        })
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (address == null) {
                            Toast.makeText(MainActivity.this, "Search first",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Log.i("thomas", "address:" + address);
                        // Get the BLuetoothDevice object
                        BluetoothDevice device = mBtAdapter
                                .getRemoteDevice(address);
                        // Attempt to connect to the device
                        mChatService.connect(device);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();


    }

}
