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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.List;
import java.util.Timer;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class jdy_Activity extends Activity implements SeekBar.OnSeekBarChangeListener{
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



    boolean connect_status_bit=false;


    ToggleButton key1,key2,key3,key4;
    Switch switch1;
    SeekBar seekBar1;

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


                connect_status_bit=true;

                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;

                updateConnectionState(R.string.disconnected);
                connect_status_bit=false;
                show_view(false);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) //接收FFE1串口透传数据通道数据
            {
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                //byte data1;
                //intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);//  .getByteExtra(BluetoothLeService.EXTRA_DATA, data1);
                displayData( intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA) );
            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE1.equals(action)) //接收FFE2功能配置返回的数据
            {
                displayData1( intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA1) );
            }
            //Log.d("", msg)
        }
    };



    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    Button send_button;


    EditText txd_txt,rx_data_id_1;

    Button clear_button;


    Timer timer = new Timer();

    TextView textView5;
    CheckBox checkBox5,checkBox1;

    TextView tx;

    boolean send_hex = true;//HEX格式发送数据  透传
    boolean rx_hex = false;//HEX格式接收数据  透传

    void show_view( boolean p )
    {
        if(p){
            send_button.setEnabled(true);
            key1.setEnabled(true);
            key2.setEnabled(true);
            key3.setEnabled(true);
            key4.setEnabled(true);
            switch1.setEnabled(true);
            seekBar1.setEnabled(true);
        }else{
            send_button.setEnabled(false);
            key1.setEnabled(false);
            key2.setEnabled(false);
            key3.setEnabled(false);
            key4.setEnabled(false);
            switch1.setEnabled(false);
            seekBar1.setEnabled(false);
        }
    }

    public void delay(int ms){
        try {
            Thread.currentThread();
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        setTitle( mDeviceName );


        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        //mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        // mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);



        send_button=(Button)findViewById(R.id.tx_button);//send data 1002
        send_button.setOnClickListener(listener);//设置监听

        clear_button=(Button)findViewById(R.id.clear_button);//send data 1002
        clear_button.setOnClickListener(listener);//设置监听

        txd_txt=(EditText)findViewById(R.id.tx_text);//1002 data
        txd_txt.setText("0102030405060708090A0102030405060708090A0102030405060708090A0102030405060708090A");
        txd_txt.clearFocus();

        rx_data_id_1=(EditText)findViewById(R.id.rx_data_id_1);//1002 data
        rx_data_id_1.setText("");

        key1 = (ToggleButton)findViewById(R.id.toggleButton1);
        key2 = (ToggleButton)findViewById(R.id.toggleButton2);
        key3 = (ToggleButton)findViewById(R.id.toggleButton3);
        key4 = (ToggleButton)findViewById(R.id.toggleButton4);

        key1.setOnClickListener( OnClickListener_listener );//设置监听
        key2.setOnClickListener( OnClickListener_listener );//设置监听
        key3.setOnClickListener( OnClickListener_listener );//设置监听
        key4.setOnClickListener( OnClickListener_listener );//设置监听

        textView5 = (TextView)findViewById(R.id.textView5);
        tx = (TextView)findViewById(R.id.tx);

        sbValues = new StringBuffer();

        switch1 = (Switch)findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked)
                {
                    if( mConnected )
                    {
                        mBluetoothLeService.set_PWM_frequency( 250 );//设置PWM频率
                        mBluetoothLeService.Delay_ms(20);//延时20MS
                        mBluetoothLeService.set_PWM_OPEN( 1 );//打开PWM
                    }
                } else {
                    if( mConnected )
                        mBluetoothLeService.set_PWM_OPEN( 0 );//关闭PWM
                }
            }
        });
        seekBar1 = (SeekBar)findViewById(R.id.seekBar1);
        seekBar1.setOnSeekBarChangeListener(this);
        seekBar1.setMax(255);

        checkBox5 = (CheckBox)findViewById(R.id.checkBox5);
        checkBox1 = (CheckBox)findViewById(R.id.checkBox1);
        checkBox5.setOnCheckedChangeListener(new OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                if(isChecked){
                    rx_hex = true;
                    rx_data_id_1.setText( mBluetoothLeService.String_to_HexString(sbValues.toString()) );
                    //Toast.makeText(jdy_Activity.this, "接收十六进制格式", Toast.LENGTH_SHORT).show();
                }else{
                    rx_hex = false;
                    rx_data_id_1.setText( sbValues );
                    //Toast.makeText(jdy_Activity.this, "接收字符串格式", Toast.LENGTH_SHORT).show();
                }
            }
        });
        checkBox1.setOnCheckedChangeListener(new OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                if(isChecked){
                    send_hex = false;
                }else{
                    send_hex = true;
                }
            }
        });


        Message message = new Message();
        message.what = 1;
        handler.sendMessage(message);


        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {

            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }


        boolean sg;
        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        sg = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        //getActionBar().setTitle( "="+BluetoothLeService );
        //mDataField.setText("="+sg );
        updateConnectionState(R.string.connecting);

        show_view(false);
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1)
            {
                //tvShow.setText(Integer.toString(i++));
                //scanLeDevice(true);
                if (mBluetoothLeService != null) {
                    if( mConnected==false )
                    {
                        updateConnectionState(R.string.connecting);
                        final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                        Log.d(TAG, "Connect request result=" + result);
                    }
                }
            }
            if (msg.what == 2)
            {
                try {
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBluetoothLeService.enable_JDY_ble( 0 );
                try {
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBluetoothLeService.enable_JDY_ble( 0 );
                try {
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBluetoothLeService.enable_JDY_ble( 1 );
                try {
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                byte[] WriteBytes = new byte[2];
                WriteBytes[0] = (byte) 0xE7;
                WriteBytes[1] = (byte) 0xf6;
                mBluetoothLeService.function_data( WriteBytes );// 发送读取所有IO状态
            }
            super.handleMessage(msg);
        };
    };


    ToggleButton.OnClickListener OnClickListener_listener = new ToggleButton.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if( mConnected )
            {
                // TODO 自动生成的方法存根
                byte bit=(byte) 0x00;
                if( v.getId()==R.id.toggleButton1 )
                {
                    bit=(byte) 0xf1;
                }
                else if( v.getId()==R.id.toggleButton2 )
                {
                    bit=(byte) 0xf2;
                }
                else if( v.getId()==R.id.toggleButton3 )
                {
                    bit=(byte) 0xf3;
                }
                else if( v.getId()==R.id.toggleButton4 )
                {
                    bit=(byte) 0xf4;

                    //				 byte[] WriteBytes = new byte[2];
                    //				 WriteBytes[0] = (byte) 0xE7;
                    //				 WriteBytes[1] = (byte) 0xf6;
                    //				 //WriteBytes[2] = (byte)0x01;
                    //				 mBluetoothLeService.function_data( WriteBytes );

                }
                if( bit!=(byte) 0x00 )
                {
                    boolean on = ((ToggleButton) v).isChecked();
                    if (on)
                    {
                        // Enable here
                        //Toast.makeText(jdy_Activity.this, "Enable here", Toast.LENGTH_SHORT).show();
                        // E7F101
                        byte[] WriteBytes = new byte[3];
                        WriteBytes[0] = (byte) 0xE7;
                        WriteBytes[1] = bit;
                        WriteBytes[2] = (byte)0x01;
                        mBluetoothLeService.function_data( WriteBytes );
                    }
                    else
                    {
                        // Disable here
                        //Toast.makeText(jdy_Activity.this, "Disable here", Toast.LENGTH_SHORT).show();
                        byte[] WriteBytes = new byte[3];
                        WriteBytes[0] = (byte) 0xE7;
                        WriteBytes[1] = bit;
                        WriteBytes[2] = (byte)0x00;
                        mBluetoothLeService.function_data( WriteBytes );
                    }
                }
            }
        }

    };

    Button.OnClickListener listener = new Button.OnClickListener(){//创建监听对象
        public void onClick(View v){
            //String strTmp="点击Button02";
            //Ev1.setText(strTmp);
            switch( v.getId())
            {
                case R.id.tx_button ://uuid1002 数传通道发送数据
                    if( connect_status_bit )
                    {
                        if( mConnected )
                        {
                            String tx_string=txd_txt.getText().toString().trim();
                            tx_count+=mBluetoothLeService.txxx( tx_string,send_hex );//发送字符串数据
                            tx.setText("发送数据："+tx_count);
                            //mBluetoothLeService.txxx( tx_string,false );//发送HEX数据
                        }
                    }else{
                        //Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_LONG).show();
                        Toast toast = Toast.makeText(jdy_Activity.this, "设备没有连接！", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    break;
                case R.id.clear_button:
                {
                    sbValues.delete(0,sbValues.length());
                    len_g =0;
                    da = "";
                    rx_data_id_1.setText( da );
                    mDataField.setText( ""+len_g );
                    tx_count = 0;
                    tx.setText("发送数据："+tx_count);
                }break;
                default :
                    break;
            }
        }

    };
    @Override
    protected void onResume() {
        super.onResume();

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
        timer.cancel();
        timer=null;
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }
    String da="";
    int len_g = 0;
    private void displayData( byte[] data1 ) //接收FFE1串口透传数据通道数据
    {
        //String head1,data_0;
		/*
		head1=data1.substring(0,2);
		data_0=data1.substring(2);
		*/
        //da = da+data1+"\n";
        if (data1 != null && data1.length > 0)
        {
            //sbValues.insert(0, data1);
            //sbValues.indexOf( data1 );
            String res = new String( data1 );

            sbValues.append( res ) ;
            //mDataField.setText( data1 );
            len_g += data1.length;
            //da = data1+da;

            if( rx_hex )rx_data_id_1.setText( mBluetoothLeService.String_to_HexString(sbValues.toString()) );
            else rx_data_id_1.setText( sbValues.toString() );
            // data1 );
            rx_data_id_1.setSelection(sbValues.length());
            if( sbValues.length()>=5000 )sbValues.delete(0,sbValues.length());
            mDataField.setText( ""+len_g );

            //rx_data_id_1.setGravity(Gravity.BOTTOM);
            //rx_data_id_1.setSelection(rx_data_id_1.getText().length());
        }

    }
    private void displayData1( byte[] data1 ) //接收FFE2功能配置返回的数据
    {
        //String str = mBluetoothLeService.bytesToHexString1( data1 );//将接收的十六进制数据转换成十六进制字符串


        if( data1.length==5&&data1[0]==(byte) 0xf6 )//判断是否是读取IO状态位
        {
            if( data1[1]==(byte) 0x01 )
            {
                key1.setChecked( true );
            }else
            {
                key1.setChecked( false );
            }
            if( data1[2]==(byte) 0x01 )
            {
                key2.setChecked( true );
            }else
            {
                key2.setChecked( false );
            }
            if( data1[3]==(byte) 0x01 )
            {
                key3.setChecked( true );
            }else
            {
                key3.setChecked( false );
            }
            if( data1[4]==(byte) 0x01 )
            {
                key4.setChecked( true );
            }else
            {
                key4.setChecked( false );
            }
        }


    }
    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {


        if (gattServices == null) return;

        if( gattServices.size()>0&&mBluetoothLeService.get_connected_status( gattServices )==2 )//表示为JDY-06、JDY-08系列蓝牙模块
        {
            if( connect_status_bit )
            {
                mConnected = true;
                show_view( true );
                mBluetoothLeService.Delay_ms( 100 );
                mBluetoothLeService.enable_JDY_ble( 0 );
                mBluetoothLeService.Delay_ms( 100 );
                mBluetoothLeService.enable_JDY_ble( 1 );
                mBluetoothLeService.Delay_ms( 100 );

                byte[] WriteBytes = new byte[2];
                WriteBytes[0] = (byte) 0xE7;
                WriteBytes[1] = (byte) 0xf6;
                mBluetoothLeService.function_data( WriteBytes );// 发送读取所有IO状态


                updateConnectionState(R.string.connected);
            }else{
                //Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_LONG).show();
                Toast toast = Toast.makeText(jdy_Activity.this, "设备没有连接！", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        else if( gattServices.size()>0&&mBluetoothLeService.get_connected_status( gattServices )==1 )//表示为JDY-09、JDY-10系列蓝牙模块
        {
            if( connect_status_bit )
            {
                mConnected = true;
                show_view( true );

                mBluetoothLeService.Delay_ms( 100 );
                mBluetoothLeService.enable_JDY_ble( 0 );

                updateConnectionState(R.string.connected);
            }else{
                //Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_LONG).show();
                Toast toast = Toast.makeText(jdy_Activity.this, "设备没有连接！", Toast.LENGTH_SHORT);
                toast.show();
            }
        }else
        {
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        // TODO 自动生成的方法存根
        if( mConnected )
        {
            mBluetoothLeService.set_PWM_ALL_pulse( seekBar.getProgress(), seekBar.getProgress(), seekBar.getProgress(), seekBar.getProgress() );
            textView5.setText("暂空比："+seekBar.getProgress() );
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO 自动生成的方法存根

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO 自动生成的方法存根
        //mBluetoothLeService.set_PWM_ALL_pulse( seekBar.getProgress(), seekBar.getProgress(), seekBar.getProgress(), seekBar.getProgress() );
        //Toast.makeText(jdy_Activity.this, "pulse"+seekBar.getProgress(), Toast.LENGTH_SHORT).show();
    }
}
