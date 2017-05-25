package com.example.zxj.bttest;

/**
 * Created by ZXJ on 2017/5/25.
 */

public class Api {
    /**
     * 主机地址
     */
    public static final String ONLINE = "http://api.jsh88.net/Block/Json/";
    public static final String SERVICE_UUID = "00001111-0000-1000-8000-00805f9b34fb";
    public static final String CHARACTERISTIC_UUID = "00002222-0000-1000-8000-00805f9b34fb";

    /**
     * 取初始化发送指令
     * String scanData
     *
     * @return
     */
    public static final String InitSend() {
        return ONLINE + "InitSend";
    }

    /**
     * 初始化数据反馈
     * taskId
     * receiveData
     * bluetooth
     *
     * @return
     */
    public static final String InitReveice() {
        return ONLINE + "InitReveice";
    }

    /**
     * 取开锁指令
     * scanData
     *
     * @return
     */
    public static final String OpenSend() {
        return ONLINE + "OpenSend";
    }

    /**
     * 开锁反馈
     * scanData
     * taskId
     *
     * @return
     */
    public static final String OpenReveice() {
        return ONLINE + "OpenReveice";
    }

    /**
     * 关锁接受到的信息
     * receiveData
     *
     * @return
     */
    public static final String LockReveice() {
        return ONLINE + "LockReveice";
    }
}
