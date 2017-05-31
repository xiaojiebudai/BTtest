package com.example.zxj.bttest;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.inuker.bluetooth.library.utils.ByteUtils;

import org.xutils.http.RequestParams;
import org.xutils.x;

/**
 * Created by ZXJ on 2017/5/23.
 * 扫码-->获取mac链接设备以及开锁指令-->开锁-->开锁结果给后台-->开锁结果展示
 */

public class OpenActivity extends FatherActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.act_open;
    }

    @Override
    protected void initValues() {

    }

    @Override
    protected void initView() {

    }

    @Override
    protected void doOperate() {


        Intent intent = new Intent();
        intent.setClass(this, ScanActivity.class);
        startActivityForResult(intent, 888);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == 888) {
                //扫描结果 http://www.wangwangsh.cn/mobile/ysbike.html?d=00000000_0
                String s = data.getStringExtra("codedContent");
                sendOpenQr(s);

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
                //
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
                WWToast.showShort("开锁成功");
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
