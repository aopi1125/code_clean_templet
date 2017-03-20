package com.kodelabs.boilerplate.channel.internal;

import com.kodelabs.boilerplate.channel.PushProxy;

/**
 * Created by chenwf on 20170316.
 */
public class PushStatus {

    public static final int PUSH_CONNET_STATUS_CONNETED = 0;
    public static final int PUSH_CONNET_STATUS_CONNETING = 1;
    public static final int PUSH_CONNET_STATUS_UNCONNETED = 2;

    private static int mConnetStatus = PUSH_CONNET_STATUS_UNCONNETED;
    private static int mConnetRetryCount = 0;

    public static boolean isUnconneted() {
        return !PushProxy.getInstance().isConnected();
    }

    public static boolean isConnected() {
        return PushProxy.getInstance().isConnected();
    }

    public static boolean isConnecting() {
        return PushProxy.getInstance().isConnecting();
    }

    protected static int getConnetStatus() {
        return mConnetStatus;
    }

    protected static void setUnconneted() {
        mConnetStatus = PUSH_CONNET_STATUS_UNCONNETED;
    }

    protected static void setConneted() {
        mConnetStatus = PUSH_CONNET_STATUS_CONNETED;
    }

    protected static void setConneting() {
        mConnetStatus = PUSH_CONNET_STATUS_CONNETING;
    }

    /**
     * 是否不可连接
     * 用户登出时不可重连或者网络无连接时
     * @return
     */
    public static boolean isConneteable() {
        // TODO: 2017/3/16
//        return isNetworkAvailable() && isIs_login();
        return true;
    }

    protected static void addRetryOnce() {
        mConnetRetryCount ++;
    }

    protected static int getRetryCount() {
        return mConnetRetryCount;
    }

    public static void resetRetryCount() {
        mConnetRetryCount = 0;
    }

}
