package com.example.zxj.bttest;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;

import org.xutils.x;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ZXJ on 2017/6/14.
 */

public class LoginActivity extends FatherActivity {
    @BindView(R.id.ed_username)
    EditText edUsername;
    @BindView(R.id.ed_password)
    EditText edPassword;
    @BindView(R.id.tv_login)
    TextView tvLogin;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initValues() {
        initDefautHead("登陆", true);
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void doOperate() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @OnClick(R.id.tv_login)
    public void onViewClicked() {
        final String username = edUsername.getText().toString();
        final String psw = edPassword.getText().toString();
        if (TextUtils.isEmpty(username)) {
            WWToast.showShort("请输入您的账号");
            return;
        }
        if (TextUtils.isEmpty(psw)) {
            WWToast.showShort("请输入您的密码");
            return;
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userName", username);
        jsonObject.put("password", psw);
        showWaitDialog();
        x.http().post(
                ParamsUtils.getPostJsonParams(jsonObject, Api.Login()),
                new WWXCallBack("Login") {

                    @Override
                    public void onAfterSuccessOk(JSONObject data) {
                        SharedPreferenceUtils.getInstance().saveSessionId(data.getString("Data"));
                        WWToast.showShort("登陆成功");
                        finish();

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

    }
}
