package com.example.zxj.bttest;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends FatherActivity {

    @BindView(R.id.tv_init)
    Button tvInit;
    @BindView(R.id.tv_open)
    Button tvOpen;

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

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick({R.id.tv_init, R.id.tv_open})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_init:

                startActivity(new Intent(this,DeviceScanActivity.class));
                            break;
            case R.id.tv_open:

                startActivity(new Intent(this,OpenActivity.class));


                break;
        }
    }

}
