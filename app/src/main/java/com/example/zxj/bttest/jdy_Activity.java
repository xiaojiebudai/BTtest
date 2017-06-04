/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.zxj.bttest;

import android.app.Activity;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class jdy_Activity extends FatherActivity  {
    private final static String TAG = jdy_Activity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private StringBuffer sbValues;

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;

    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;

    String iniStr = "ATAAOgBxQUxKc3RpcUdFamJZZHRZcUFMSnN0aXFHRWpiWWR0WTIwMTctMDUtMjYgMTE6MDMAAAAAAAAAAAAA/w==";
    String openStr = "xQ0OQel79+eOmYRP+9hhollQBECHZ1hSZvnHNZ9ksru/9uqQGTVjvCpBdgTPALVMQLQpiGD4sUwrET5mi1HRxw==";
    String testStr = "880OQel79+eOmYRP+9h";
    String initData = "AQEAJgBxQUxKc3RpcUdFamJZZHRZcUFMSnN0aXFHRWpiWWR0WQoAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA/w==";
    String openData = "tarCvqPggyrPanc5Eu5qyF8qyDbGYMCCecQYFCOfeGCS9rqKYdA5yFqQ6RlSKEuEuhxfJIFCpmZ3hqwyuzomMA==";
    String closeData = "7pUPMjdIxcWiOnLdSrv3NDYovPJHyla3v6wTFhWd52cIX2gXYIN95AMLt5BZEwmuWIHsUBdjdnC7DAVB/dMgIg==";

    boolean connect_status_bit = false;


    int tx_count = 0;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                //mConnected = true;


                connect_status_bit = true;

                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;

                updateConnectionState(R.string.disconnected);
                connect_status_bit = false;
                show_view(false);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) //接收FFE1串口透传数据通道数据
            {
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                //byte data1;
                //intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);//  .getByteExtra(BluetoothLeService.EXTRA_DATA, data1);
                displayData(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));


            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE1.equals(action)) //接收FFE2功能配置返回的数据
            {
                displayData(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));

            }
            //Log.d("", msg)
        }
    };


    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    Button send_button, tx_open;


    EditText txd_txt, rx_data_id_1;

    Button clear_button;


    TextView tx;

    void show_view(boolean p) {
        if (p) {
            send_button.setEnabled(true);
            tx_open.setEnabled(true);

        } else {
            send_button.setEnabled(false);
            tx_open.setEnabled(false);

        }
    }


    @Override
    protected int getLayoutId() {
        return R.layout.gatt_services_characteristics;
    }

    @Override
    protected void initValues() {
        initDefautHead("设备初始化", true);
    }

    @Override
    protected void initView() {
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);


        send_button = (Button) findViewById(R.id.tx_button);//send data 1002
        send_button.setOnClickListener(listener);//设置监听
        tx_open = (Button) findViewById(R.id.tx_open);//send data 1002
        tx_open.setOnClickListener(listener);//设置监听

        clear_button = (Button) findViewById(R.id.clear_button);//send data 1002
        clear_button.setOnClickListener(listener);//设置监听

        txd_txt = (EditText) findViewById(R.id.tx_text);//1002 data
//        txd_txt.setText("0102030405060708090A0102030405060708090A0102030405060708090A0102030405060708090A");


        txd_txt.clearFocus();

        rx_data_id_1 = (EditText) findViewById(R.id.rx_data_id_1);//1002 data
        rx_data_id_1.setText("");


        tx = (TextView) findViewById(R.id.tx);

        sbValues = new StringBuffer();


        Message message = new Message();
        message.what = 1;
        handler.sendMessage(message);


        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {

            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }


        boolean sg;
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        sg = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        //getActionBar().setTitle( "="+BluetoothLeService );
        //mDataField.setText("="+sg );
        updateConnectionState(R.string.connecting);

        show_view(false);
    }

    @Override
    protected void doOperate() {

    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                //tvShow.setText(Integer.toString(i++));
                //scanLeDevice(true);
                if (mBluetoothLeService != null) {
                    if (mConnected == false) {
                        updateConnectionState(R.string.connecting);
                        final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                        Log.d(TAG, "Connect request result=" + result);
                    }
                }
            }
            if (msg.what == 2) {
                try {
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBluetoothLeService.enable_JDY_ble(0);
                try {
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBluetoothLeService.enable_JDY_ble(0);
                try {
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBluetoothLeService.enable_JDY_ble(1);
                try {
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                byte[] WriteBytes = new byte[2];
                WriteBytes[0] = (byte) 0xE7;
                WriteBytes[1] = (byte) 0xf6;
                mBluetoothLeService.function_data(WriteBytes);// 发送读取所有IO状态
            }
            super.handleMessage(msg);
        }

        ;
    };


    Button.OnClickListener listener = new Button.OnClickListener() {//创建监听对象
        public void onClick(View v) {
            //String strTmp="点击Button02";
            //Ev1.setText(strTmp);
            switch (v.getId()) {
                case R.id.tx_button://uuid1002 初始化

                    if (connect_status_bit) {
                        if (mConnected) {

                            tx_count += mBluetoothLeService.txxx(iniStr, true);//发送字符串数据
                            tx.setText("发送数据：" + tx_count);
                            //mBluetoothLeService.txxx( tx_string,false );//发送HEX数据
                        }
                    } else {
                        //Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_LONG).show();
                        Toast toast = Toast.makeText(jdy_Activity.this, "设备没有连接！", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    break;
                case R.id.tx_open://uuid1002 开锁

                    if (connect_status_bit) {
                        if (mConnected) {


                            tx_count += mBluetoothLeService.txxx(openStr, true);//发送字符串数据
                            tx.setText("发送数据：" + tx_count);
                            //mBluetoothLeService.txxx( tx_string,false );//发送HEX数据
                        }
                    } else {
                        //Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_LONG).show();
                        Toast toast = Toast.makeText(jdy_Activity.this, "设备没有连接！", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    break;
                case R.id.clear_button: {
                    sbValues.delete(0, sbValues.length());
                    len_g = 0;
                    da = "";
                    rx_data_id_1.setText(da);
                    mDataField.setText("" + len_g);
                    tx_count = 0;
                    tx.setText("发送数据：" + tx_count);
                }
                break;
                default:
                    break;
            }
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;

    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    String da = "";
    int len_g = 0;

    private void displayData(byte[] data1) //接收FFE1串口透传数据通道数据
    {
        //String head1,data_0;
        /*
		head1=data1.substring(0,2);
		data_0=data1.substring(2);
		*/
        //da = da+data1+"\n";
        if (data1 != null && data1.length > 0) {
            //sbValues.insert(0, data1);
            //sbValues.indexOf( data1 );
            String res = new String(data1);


            Log.d("dataData", res.toString());

            sbValues.append(res);
            //mDataField.setText( data1 );
            len_g += data1.length;
            //da = data1+da;

            rx_data_id_1.setText(sbValues.toString());

            rx_data_id_1.setSelection(sbValues.length());
            if (sbValues.length() >= 5000) sbValues.delete(0, sbValues.length());
            mDataField.setText("" + len_g);

            //rx_data_id_1.setGravity(Gravity.BOTTOM);
            //rx_data_id_1.setSelection(rx_data_id_1.getText().length());
        }

    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {


        if (gattServices == null) return;

        if (gattServices.size() > 0 && mBluetoothLeService.get_connected_status(gattServices) == 2)//表示为JDY-06、JDY-08系列蓝牙模块
        {
            if (connect_status_bit) {
                mConnected = true;
                show_view(true);
                mBluetoothLeService.Delay_ms(100);
                mBluetoothLeService.enable_JDY_ble(0);
                mBluetoothLeService.Delay_ms(100);
                mBluetoothLeService.enable_JDY_ble(1);
                mBluetoothLeService.Delay_ms(100);

                byte[] WriteBytes = new byte[2];
                WriteBytes[0] = (byte) 0xE7;
                WriteBytes[1] = (byte) 0xf6;
                mBluetoothLeService.function_data(WriteBytes);// 发送读取所有IO状态


                updateConnectionState(R.string.connected);
            } else {
                //Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_LONG).show();
                Toast toast = Toast.makeText(jdy_Activity.this, "设备没有连接！", Toast.LENGTH_SHORT);
                toast.show();
            }
        } else if (gattServices.size() > 0 && mBluetoothLeService.get_connected_status(gattServices) == 1)//表示为JDY-09、JDY-10系列蓝牙模块
        {
            if (connect_status_bit) {
                mConnected = true;
                show_view(true);

                mBluetoothLeService.Delay_ms(100);
                mBluetoothLeService.enable_JDY_ble(0);

                updateConnectionState(R.string.connected);
            } else {
                //Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_LONG).show();
                Toast toast = Toast.makeText(jdy_Activity.this, "设备没有连接！", Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            Toast toast = Toast.makeText(jdy_Activity.this, "提示！此设备不为JDY系列BLE模块", Toast.LENGTH_SHORT);
            toast.show();
        }


    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE1);
        return intentFilter;
    }

}
