package com.example.zxj.bttest;

import android.content.Intent;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

public class ScanActivity extends FatherActivity implements QRCodeView.Delegate {
    private static final String TAG = ScanActivity.class.getSimpleName();
    private QRCodeView mQRCodeView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_test_scan;
    }

    @Override
    protected void initValues() {
        initDefautHead("扫码开锁", true);
    }

    @Override
    protected void initView() {
        mQRCodeView = (ZXingView) findViewById(R.id.zxingview);
        mQRCodeView.setDelegate(this);
    }

    @Override
    protected void doOperate() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mQRCodeView.startCamera();
        mQRCodeView.showScanRect();
    }

    @Override
    protected void onStop() {
        mQRCodeView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mQRCodeView.onDestroy();
        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        if (!TextUtils.isEmpty(result)) {
            Intent intent = getIntent();
            intent.putExtra("codedContent", result);
            setResult(RESULT_OK, intent);
            finish();
        }
        vibrate();
        mQRCodeView.startSpot();
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(TAG, "打开相机出错");
    }
private boolean  isShowLight=false;
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open_flashlight:
                if(isShowLight){
                    mQRCodeView.closeFlashlight();
                }else{
                    mQRCodeView.openFlashlight();
                }
                isShowLight=!isShowLight;
                break;

        }
    }
}