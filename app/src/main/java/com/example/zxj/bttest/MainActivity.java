package com.example.zxj.bttest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;

import org.xutils.x;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends FatherActivity {

    @BindView(R.id.tv_init)
    Button tvInit;
    @BindView(R.id.tv_open)
    Button tvOpen;
    @BindView(R.id.tv_login)
    Button tv_login;
    @BindView(R.id.tv_loginout)
    Button tv_loginout;

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

    }

    @Override
    protected void doOperate() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

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

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick({R.id.tv_init, R.id.tv_open, R.id.tv_login, R.id.tv_loginout})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_login:

                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.tv_loginout:
                if (MyApplication.isLogin()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("sessionId", SharedPreferenceUtils.getInstance().getSessionId());
                    showWaitDialog();
                    x.http().post(
                            ParamsUtils.getPostJsonParams(jsonObject, Api.LogOut()),
                            new WWXCallBack("LogOut") {

                                @Override
                                public void onAfterSuccessOk(JSONObject data) {
                                    SharedPreferenceUtils.getInstance().saveSessionId("");
                                    WWToast.showShort("登出成功");
                                }

                                @Override
                                public void onAfterSuccessError(JSONObject data) {
                                    super.onAfterSuccessError(data);
                                }

                                @Override
                                public void onAfterFinished() {
                                    dismissWaitDialog();
                                }
                            });

                }else{
                    WWToast.showShort("还未登陆");
                }

                break;
            case R.id.tv_init:
                if (MyApplication.isLogin()) {
                    startActivity(new Intent(this, DeviceScanActivity.class));
                }else{
                    startActivity(new Intent(this, LoginActivity.class));
                }

                break;
            case R.id.tv_open:
                if (MyApplication.isLogin()) {
                    Intent intent = new Intent();
                    intent.setClass(this, ScanActivity.class);
                    startActivityForResult(intent, 888);
                }else{
                    startActivity(new Intent(this, LoginActivity.class));
                }



                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == 888) {
                //扫描结果 http://www.wangwangsh.cn/mobile/ysbike.html?d=00000000_0
                String s = data.getStringExtra("codedContent");

                startActivity(new Intent(this, OpenActivity.class).putExtra("Data", s));
            }
        }
    }
}
