package com.example.zxj.bttest;

import android.content.Intent;
import android.os.Bundle;

import com.alibaba.fastjson.JSONObject;

import org.xutils.http.RequestParams;
import org.xutils.x;

import butterknife.ButterKnife;

/**
 * Created by ZXJ on 2017/5/23.
 *     //确保存在链接-->扫码-->请求初始化数据发送给设备-->将初始化结果给后台-->初始化结果展示
 */

public class DeviceInitializeActivity extends FatherActivity {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    @Override
    protected int getLayoutId() {
        return R.layout.act_device_init;
    }

    @Override
    protected void initValues() {
        initDefautHead("选择链接设备", true);
    }

    @Override
    protected void initView() {


    }

    @Override
    protected void doOperate() {
        Intent intent1 = new Intent();
        intent1.setClass(this, DeviceInitializeActivity.class);
        startActivityForResult(intent1, 999);
    }



    @Override
    protected void onPause() {
        super.onPause();

    }



    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == 999) {
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
                WWToast.showShort("设备初始化成功");
            }

            @Override
            public void onAfterFinished() {
                dismissWaitDialog();
            }
        });
    }
}
