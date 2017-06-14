package com.example.zxj.bttest;

/**
 * Created by ZXJ on 2017/5/25.
 */

public class Api {
    /**
     * 主机地址
     */
    public static final String ONLINE = "http://api.jsh88.net/Block/Json/";
    public static final String ADMIN = "http://api.jsh88.net/Admin/Json/";


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
    /**
     *管理员登陆
     * @return
     */
    public static final String Login() {
        return ADMIN + "Login";
    }
    /**
     *管理员登出
     * @return
     */
    public static final String LogOut() {
        return ADMIN + "LogOut";
    }
}
