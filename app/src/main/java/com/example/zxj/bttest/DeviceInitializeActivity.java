package com.example.zxj.bttest;

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
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;

import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by ZXJ on 2017/5/23.
 *     //确保存在链接-->扫码-->请求初始化数据发送给设备-->将初始化结果给后台-->初始化结果展示
 */

public class DeviceInitializeActivity extends FatherActivity {
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

        }
    };


    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    Button send_button;


    EditText rx_data_id_1;
    TextView txd_txt;
    Button clear_button;


    TextView tx;

    void show_view(boolean p) {
        if (p) {
            send_button.setEnabled(true);


        } else {
            send_button.setEnabled(false);

        }
    }


    @Override
    protected int getLayoutId() {
        return R.layout.act_device_init;
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
        ZLog.showPost(mDeviceAddress);
        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);


        send_button = (Button) findViewById(R.id.tx_button);//send data 1002
        send_button.setOnClickListener(listener);//设置监听

        clear_button = (Button) findViewById(R.id.clear_button);//send data 1002
        clear_button.setOnClickListener(listener);//设置监听

        txd_txt = (TextView) findViewById(R.id.tx_text);//1002 data



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

                    Intent intent = new Intent();
                    intent.setClass(DeviceInitializeActivity.this, ScanActivity.class);
                    startActivityForResult(intent, 888);

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
//        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        unregisterReceiver(mGattUpdateReceiver);
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

        if (data1 != null && data1.length > 0) {

            String res = new String(data1);

            Log.d("dataData", res.toString());

           if(sbValues.length()>=88)return;

            sbValues.append(res);

            len_g += data1.length;
            rx_data_id_1.setText(sbValues.toString());
            rx_data_id_1.setSelection(sbValues.length());
            if (sbValues.length() >= 5000) sbValues.delete(0, sbValues.length());
            mDataField.setText("" + len_g);

            if(sbValues.length()==88){
                infoReceive(sbValues.toString(), mDeviceAddress);
            }


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
                Toast toast = Toast.makeText(DeviceInitializeActivity.this, "设备没有连接！", Toast.LENGTH_SHORT);
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
                Toast toast = Toast.makeText(DeviceInitializeActivity.this, "设备没有连接！", Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            Toast toast = Toast.makeText(DeviceInitializeActivity.this, "提示！此设备不为JDY系列BLE模块", Toast.LENGTH_SHORT);
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
private String scanData;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
           if (requestCode == 888) {
                //扫描结果
                 scanData = data.getStringExtra("codedContent");
                sendInitQr(scanData);
            }
        }
    }
    public static RequestParams getPostJsonParams(JSONObject jsonObject,
                                                  String url) {
        RequestParams params = new RequestParams(url);
        params.setAsJsonContent(true);
        params.setBodyContent(jsonObject.toString());
        return params;
    }

    /**
     * 发送初始化扫码数据
     *
     * @param scanData
     */
    private String taskId;
    private void sendInitQr(final String scanData) {
        showWaitDialog();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("bluetooth", mDeviceAddress);
        jsonObject.put("scanData", scanData);
        jsonObject.put("sessionId", SharedPreferenceUtils.getInstance().getSessionId());
        x.http().post(getPostJsonParams(jsonObject, Api.InitSend()), new WWXCallBack("InitSend") {
            @Override
            public void onAfterSuccessOk(JSONObject data) {
                Device device = JSONObject.parseObject(data.getString("Data"), Device.class);
                taskId = data.getString("TaskId");

                if (connect_status_bit) {
                    if (mConnected) {
                        if(sbValues!=null&&sbValues.length()>0){
                            sbValues.delete(0,sbValues.length());
                        }
                        iniStr=device.CommandText;
                        tx_count += mBluetoothLeService.txxx(iniStr, true);//发送字符串数据
                        txd_txt.setText(iniStr);
                        tx.setText("发送数据：" + tx_count);
                        //mBluetoothLeService.txxx( tx_string,false );//发送HEX数据
                    }
                } else {
                    //Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_LONG).show();
                    Toast toast = Toast.makeText(DeviceInitializeActivity.this, "设备没有连接！", Toast.LENGTH_SHORT);
                    toast.show();
                }

            }

            @Override
            public void onAfterFinished() {
                dismissWaitDialog();
            }
        });
    }


    /**
     * 关锁指令
     *

     */
    private void infoReceive(String receiveData,String address) {
        showWaitDialog();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("receiveData", receiveData);
        jsonObject.put("bluetooth", address);
        jsonObject.put("sessionId", SharedPreferenceUtils.getInstance().getSessionId());
        ZLog.showPost(receiveData);
        //发送完立马清除
        if (sbValues != null && sbValues.length() > 0) {
            sbValues.delete(0, sbValues.length());
        }
        x.http().post(getPostJsonParams(jsonObject, Api.LockReveice()), new WWXCallBack("LockReveice") {
            @Override
            public void onAfterSuccessOk(JSONObject data) {
                Device device = JSONObject.parseObject(data.getString("Data"), Device.class);
                switch (device.CommandName) {
                    case Device.INIT:
                        mBluetoothLeService.txxx(device.CommandText, true);//发送字符串数据
                        WWToast.showShort("发送初始化数据给后台成功");
                        break;
                    case Device.OPEN:

                        initDefautHead("骑行中", false);
                        WWToast.showShort("开始用车");

                        break;
                    case Device.CLOSE:
                        mBluetoothLeService.txxx(device.CommandText, true);//发送字符串数据
                        WWToast.showShort("关锁成功");

                        break;
                    case Device.UNKNOW:
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onAfterFinished() {
                dismissWaitDialog();
            }
        });
    }
}
