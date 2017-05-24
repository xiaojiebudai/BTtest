package com.example.zxj.bttest;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;

import org.xutils.http.RequestParams;
import org.xutils.x;

import butterknife.BindView;
import butterknife.OnClick;

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

    }

    @Override
    protected void initView() {


    }

    @Override
    protected void doOperate() {

    }

    @OnClick({R.id.tv_init, R.id.tv_open})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_init:
                //确保存在链接-->扫码-->请求初始化数据发送给设备-->将初始化结果给后台-->初始化结果展示
                if (true) {
                    Intent intent = new Intent();
                    intent.setClass(this, ScanActivity.class);
                    startActivityForResult(intent, 888);
                } else {
                    WWToast.showShort(this, "请先在手机设置中链接设备");
                }
                break;
            case R.id.tv_open:
                //扫码-->获取mac链接设备以及开锁指令-->开锁-->开锁结果给后台-->开锁结果展示
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == 888) {
                //扫描结果
                String s = data.getStringExtra("codedContent");
                WWToast.showShort(this, s);
                //拆分结果
                RequestParams params = new RequestParams("url");
                x.http().get(params, new WWXCallBack("") {
                    @Override
                    public void onAfterSuccessOk(JSONObject data) {

                    }

                    @Override
                    public void onAfterFinished() {

                    }
                });

            }
        }
    }
}
