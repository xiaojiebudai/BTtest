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


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

//import com.example.jdy_type.Get_type;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 * <p>
 * <p>
 * 扫描并筛选支持设备
 */
public class DeviceScanActivity extends FatherActivity implements OnClickListener {
    // private LeDeviceListAdapter mLeDeviceListAdapter;
//	Get_type mGet_type;
    private BluetoothAdapter mBluetoothAdapter;


    private DeviceListAdapter mDevListAdapter;

    ListView lv_bleList;


    byte dev_bid;


    @Override
    protected int getLayoutId() {
        return R.layout.jdy_activity_main;
    }

    @Override
    protected void initValues() {
        initDefautHead("扫描设备", true);
    }

    @Override
    protected void initView() {

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // 如果本地蓝牙没有开启，则开启
        if (!mBluetoothAdapter.isEnabled()) {
            // 我们通过startActivityForResult()方法发起的Intent将会在onActivityResult()回调方法中获取用户的选择，比如用户单击了Yes开启，
            // 那么将会收到RESULT_OK的结果，
            // 如果RESULT_CANCELED则代表用户不愿意开启蓝牙
            Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(mIntent, 1);
            // 用enable()方法来开启，无需询问用户(实惠无声息的开启蓝牙设备),这时就需要用到android.permission.BLUETOOTH_ADMIN权限。
            // mBluetoothAdapter.enable();
            // mBluetoothAdapter.disable();//关闭蓝牙
        }


        lv_bleList = (ListView) findViewById(R.id.lv_bleList);


        mDevListAdapter = new DeviceListAdapter(mBluetoothAdapter, DeviceScanActivity.this);
        dev_bid = (byte) 0x88;//88 是JDY厂家VID码
        mDevListAdapter.set_vid(dev_bid);//用于识别自家的VID相同的设备，只有模块的VID与APP的VID相同才会被搜索得到
        lv_bleList.setAdapter(mDevListAdapter.init_adapter());


        lv_bleList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (mDevListAdapter.get_count() > 0) {


                    Byte vid_byte = mDevListAdapter.get_vid(position);//返回136表示是JDY厂家模块
                    //String vid_str =String.format("%02x", vid_byte );
                    //Toast.makeText( DeviceScanActivity.this,"设备VID:"+vid_str, Toast.LENGTH_SHORT).show();
//				    Toast.makeText( DeviceScanActivity.this, "type:"+mDevListAdapter.get_item_type(position), Toast.LENGTH_SHORT).show();

                    if (vid_byte == dev_bid)//JDY厂家VID为0X88， 用户的APP不想搜索到其它厂家的JDY-08模块的话，可以设备一下 APP的VID，此时模块也需要设置，
                        //模块的VID与厂家APP的VID要一样，APP才可以搜索得到模块VID与APP一样的设备
                        switch (mDevListAdapter.get_item_type(position)) {
                            case JDY:////为标准透传模块
                            {
                                BluetoothDevice device1 = mDevListAdapter.get_item_dev(position);
                                if (device1 == null) return;
                                Intent intent1 = new Intent(DeviceScanActivity.this, DeviceInitializeActivity.class);
                                ;
                                intent1.putExtra(jdy_Activity.EXTRAS_DEVICE_NAME, device1.getName());
                                intent1.putExtra(jdy_Activity.EXTRAS_DEVICE_ADDRESS, device1.getAddress());
                                mDevListAdapter.scan_jdy_ble(false);
                                startActivity(intent1);
                                break;
                            }

                            default:
                                break;
                        }


                }
            }
        });
        initTextHeadRigth("扫描", new OnClickListener() {
            @Override
            public void onClick(View view) {
                mDevListAdapter.scan_jdy_ble(true);
            }
        });
    }

    @Override
    protected void doOperate() {

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case 0:
                break;

        }
    }


    @Override
    protected void onResume() {//打开APP时扫描设备
        super.onResume();
        mDevListAdapter.scan_jdy_ble(true);
    }

    @Override
    protected void onPause() {//停止扫描
        super.onPause();
        mDevListAdapter.scan_jdy_ble(false);
    }

}