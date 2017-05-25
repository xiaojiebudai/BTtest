package com.example.zxj.bttest;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.inuker.bluetooth.library.utils.ByteUtils;

import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;

public class MainActivity extends FatherActivity {

    @BindView(R.id.tv_init)
    Button tvInit;
    @BindView(R.id.tv_open)
    Button tvOpen;

    @BindView(R.id.tv_link_state)
    TextView tvLinkState;
    @BindView(R.id.tv_num)
    TextView tvNum;
    @BindView(R.id.tv_result)
    TextView tvResult;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initValues() {
        initDefautHead("蓝牙测试用例", false);
    }

    @Override
    protected void initView() {
        if (ClientManager.getClient().isBleSupported()) {
            if (ClientManager.getClient().isBluetoothOpened()) {
                tvLinkState.setText("设备蓝牙已打开");
            } else {
                tvLinkState.setText("设备蓝牙未打开");
            }
        } else {
            tvLinkState.setText("该设备不支持蓝牙");
        }
        ClientManager.getClient().registerBluetoothStateListener(new BluetoothStateListener() {
            @Override
            public void onBluetoothStateChanged(boolean openOrClosed) {
                if (openOrClosed) {
                    tvLinkState.setText("设备蓝牙已打开");
                } else {
                    tvLinkState.setText("设备蓝牙未打开");
                }
            }
        });
    }

    @Override
    protected void doOperate() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        ClientManager.getClient().stopSearch();
    }

    private void connectDevice() {
        showWaitDialog();
        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)
                .setConnectTimeout(20000)
                .setServiceDiscoverRetry(3)
                .setServiceDiscoverTimeout(10000)
                .build();

        ClientManager.getClient().connect(openMac, options, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile profile) {
                dismissWaitDialog();
                if (code == REQUEST_SUCCESS) {
                    ClientManager.getClient().notify(openMac, UUID.fromString(Api.SERVICE_UUID), UUID.fromString(Api.CHARACTERISTIC_UUID), new BleNotifyResponse() {
                        @Override
                        public void onNotify(UUID service, UUID character, byte[] value) {
                            //如果接受到关锁信息，发送给服务器
                            closeDevice(value);
                        }

                        @Override
                        public void onResponse(int code) {
                            if (code == REQUEST_SUCCESS) {

                            }
                        }
                    });
// 清楚所有的请求队列                   ClientManager.getClient().clearRequest(MAC, 0);
                    //链接成功
                    ClientManager.getClient().writeNoRsp(deviceSelect.getAddress(),
                            UUID.fromString(Api.SERVICE_UUID),
                            UUID.fromString(Api.CHARACTERISTIC_UUID), ByteUtils.stringToBytes(scanData), new BleWriteResponse() {
                                @Override
                                public void onResponse(int code) {
                                    if (code == REQUEST_SUCCESS) {
                                        sendOpenData(scanData, taskId);
                                    } else {
                                        WWToast.showShort("写入失败，请重新操作");
                                    }
                                }
                            });
                } else {
                    //链接失败
                    WWToast.showShort("链接失败");
                }
            }
        });
    }


    private boolean mConnected;
    private final BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            BluetoothLog.v(String.format("DeviceDetailActivity onConnectStatusChanged %d in %s",
                    status, Thread.currentThread().getName()));

            mConnected = (status == STATUS_CONNECTED);
            connectDeviceIfNeeded();
        }
    };

    private void connectDeviceIfNeeded() {
        if (!mConnected) {
            connectDevice();
        }
    }

    @Override
    protected void onDestroy() {
        if (mConnected) {
            ClientManager.getClient().disconnect(deviceSelect.getAddress());
        }

        ClientManager.getClient().unregisterConnectStatusListener(deviceSelect.getAddress(), mConnectStatusListener);
        super.onDestroy();
    }

    @OnClick({R.id.tv_init, R.id.tv_open})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_init:
                //确保存在链接-->扫码-->请求初始化数据发送给设备-->将初始化结果给后台-->初始化结果展示
                if (ClientManager.getClient().isBluetoothOpened()) {
                    Intent intent1 = new Intent();
                    intent1.setClass(this, DeviceInitializeActivity.class);
                    startActivityForResult(intent1, 999);
                } else {
                    WWToast.showShort("设备蓝牙未打开");
                }

                break;
            case R.id.tv_open:
                //扫码-->获取mac链接设备以及开锁指令-->开锁-->开锁结果给后台-->开锁结果展示
                if (ClientManager.getClient().isBluetoothOpened()) {
                    Intent intent = new Intent();
                    intent.setClass(this, ScanActivity.class);
                    startActivityForResult(intent, 888);
                } else {
                    WWToast.showShort("设备蓝牙未打开");
                }

                break;
        }
    }

    private SearchResult deviceSelect;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == 888) {
                //扫描结果 http://www.wangwangsh.cn/mobile/ysbike.html?d=00000000_0
                String s = data.getStringExtra("codedContent");
                tvResult.setText(s);
                sendOpenQr(s);

            } else if (requestCode == 999) {
                deviceSelect = data.getParcelableExtra("data");
                tvResult.setText(deviceSelect.getName() + "--" + deviceSelect.getAddress());
                Intent intent = new Intent();
                intent.setClass(this, ScanActivity.class);
                startActivityForResult(intent, 666);
            } else if (requestCode == 666) {
                //扫描结果
                String s = data.getStringExtra("codedContent");
                sendInitQr(s);
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
     * 发送开锁扫码数据
     *
     * @param scanData
     */
    private String openMac;
    private String taskId;
    private String scanData;

    private void sendOpenQr(String scanData) {
        this.scanData = scanData;
        showWaitDialog();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("scanData", scanData);
        x.http().post(getPostJsonParams(jsonObject, Api.OpenSend()), new WWXCallBack("OpenSend") {
            @Override
            public void onAfterSuccessOk(JSONObject data) {
                ZLog.showPost(data.toString());
                taskId = data.getString("taskId");
                openMac = data.getString("Data");
                tvResult.setText(openMac);
                ClientManager.getClient().registerConnectStatusListener(openMac, mConnectStatusListener);
                connectDeviceIfNeeded();
            }

            @Override
            public void onAfterFinished() {
                dismissWaitDialog();
            }
        });
    }

    /**
     * 发送开锁数据
     *
     * @param scanData
     * @param taskId
     */
    private void sendOpenData(String scanData, String taskId) {
        showWaitDialog();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("scanData", scanData);
        jsonObject.put("taskId", taskId);
        x.http().post(getPostJsonParams(jsonObject, Api.OpenReveice()), new WWXCallBack("OpenReveice") {
            @Override
            public void onAfterSuccessOk(JSONObject data) {
                tvResult.setText("开锁成功");
                WWToast.showShort("开锁成功");
            }

            @Override
            public void onAfterFinished() {
                dismissWaitDialog();
            }
        });

    }

    /**
     * 发送初始化扫码数据
     *
     * @param scanData
     */
    private void sendInitQr(final String scanData) {
        showWaitDialog();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("scanData", scanData);
        x.http().post(getPostJsonParams(jsonObject, Api.InitSend()), new WWXCallBack("InitSend") {
            @Override
            public void onAfterSuccessOk(JSONObject data) {
                Device device = JSONObject.parseObject(data.getString("Data"), Device.class);


                final String taskId = data.getString("TaskId");
                ClientManager.getClient().writeNoRsp(deviceSelect.getAddress(),
                        UUID.fromString(Api.SERVICE_UUID),
                        UUID.fromString(Api.CHARACTERISTIC_UUID), ByteUtils.stringToBytes(device.CommandText), new BleWriteResponse() {
                            @Override
                            public void onResponse(int code) {
                                if (code == REQUEST_SUCCESS) {

                                    sendInitData(scanData, taskId, deviceSelect.getAddress());
                                } else {
                                    WWToast.showShort("写入失败，请重新操作");
                                }
                            }
                        });

            }

            @Override
            public void onAfterFinished() {
                dismissWaitDialog();
            }
        });
    }

    /**
     * 发送初始化数据
     *
     * @param scanData
     * @param taskId
     * @param address
     */
    private void sendInitData(String scanData, String taskId, String address) {
        showWaitDialog();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("scanData", scanData);
        jsonObject.put("taskId", taskId);
        jsonObject.put("bluetooth", address);
        x.http().post(getPostJsonParams(jsonObject, Api.InitReveice()), new WWXCallBack("InitReveice") {
            @Override
            public void onAfterSuccessOk(JSONObject data) {

                tvResult.setText("设备初始化成功");
                WWToast.showShort("设备初始化成功");
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
     * @param value
     */
    private void closeDevice(byte[] value) {
        showWaitDialog();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("receiveData", ByteUtils.byteToString(value));

        x.http().post(getPostJsonParams(jsonObject, Api.LockReveice()), new WWXCallBack("LockReveice") {
            @Override
            public void onAfterSuccessOk(JSONObject data) {
                WWToast.showShort("关锁成功");
            }

            @Override
            public void onAfterFinished() {
                dismissWaitDialog();
            }
        });
    }
}
