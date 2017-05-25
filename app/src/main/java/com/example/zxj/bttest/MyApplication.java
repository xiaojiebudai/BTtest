package com.example.zxj.bttest;

import android.app.Application;

import com.inuker.bluetooth.library.BluetoothContext;

import org.xutils.x;

/**
 * Created by dingjikerbo on 2016/8/27.
 */
public class MyApplication extends Application {

    private static MyApplication instance;

    public static Application getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        x.Ext.init(this);
        x.Ext.setDebug(true);
        BluetoothContext.set(this);

    }
}
