package com.kodelabs.boilerplate.channel;

import android.util.Log;

import com.kodelabs.boilerplate.channel.internal.PushStatus;

/**
 * Created by chenwf on 20170316.
 * 统一对外接口
 */
public class PushUtils {

    private static final boolean DEBUGGABLE = true;
    private static final String TAG_LOG = "Harbor_Push";
    private static final String WEBSOCKET_URI = "ws://your_uri";

    static Context mAppContext;

    public static void logcat(Object... logs) {
        if(logs != null) {
            if(DEBUGGABLE) {
                StringBuffer sb = new StringBuffer();
                for(Object content : logs){
                    sb.append(String.valueOf(content));
                }
                Log.d(TAG_LOG, sb.toString());
            }
        }
    }

    public static void init(Context context){
        mAppContext = context.getApplicationContext();
    }

    /**
     * 开始执行长连接操作，主要包含本地连接状态的控制
     */
    public static void userConnect(String userId) {
        logcat("userConnect");
        if(!PushStatus.isConneteable()) {
            logcat("userConnect 不可连接");
            return;
        }
        if(!PushStatus.isConnecting() && !PushStatus.isConnected()) {
            PushProxy.getInstance().startConnet(WEBSOCKET_URI + userId);
        } else {
            logcat("userConnect inProgressing or conneted...");
        }
    }

    /**
     * 开始登出长连接操作，主要包含本地连接状态的控制
     */
    public static void userUnConnect() {
        logcat("userUnConnect");

        PushProxy.getInstance().close();
        PushStatus.resetRetryCount();
    }

    public static void userSendMessage(String message){
        PushProxy.getInstance().trySendMsg(message);
    }
}
