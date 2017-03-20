package com.kodelabs.boilerplate.channel;

import com.kodelabs.boilerplate.channel.internal.OKHttpWSPush;
import com.kodelabs.boilerplate.channel.internal.OnConnetListener;
import com.kodelabs.boilerplate.channel.internal.PushBaseTask;
import com.kodelabs.boilerplate.channel.internal.WebSocketPush;

/**
 * Created by chenwf on 2017/03/16.
 * websocket代理类
 */
public class PushProxy extends PushBaseTask {

    private static PushProxy instance = null;

    private PushBaseTask pushTask = null;

    private PushProxy() {
        super();
        if(isOkhttpSupport()){
            pushTask = new OKHttpWSPush();
        }else{
            pushTask = new WebSocketPush();
        }
    }

    public static PushProxy getInstance() {
        if(instance == null) {
            synchronized (PushProxy.class){
                instance = new PushProxy();
            }
        }
        return instance;
    }

    private boolean isOkhttpSupport(){
        boolean support = false;
        try {
            Class.forName("okhttp3.WebSocket");
            Class.forName("okio.ByteString");
            support = true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return support;
    }

    @Override
    public void close() {
        pushTask.close();
    }

    @Override
    public void startConnet(String url) {
        pushTask.startConnet(url);
    }

    @Override
    public void retryConnet() {
        pushTask.retryConnet();
    }

    @Override
    public void trySendMsg(String msg) {
        pushTask.trySendMsg(msg);
    }

    @Override
    public void onReceiveMessage(String message) {
        pushTask.onReceiveMessage(message);
    }


    @Override
    public void init(OnConnetListener listener) {
        pushTask.init(listener);
    }

    @Override
    public void connect() {
        pushTask.connect();
    }

    @Override
    public void closeConnect() {
        pushTask.closeConnect();
    }

    @Override
    public void realSendMsg(String msg) {
        pushTask.realSendMsg(msg);
    }

    @Override
    public boolean isConnecting() {
        return pushTask.isConnecting();
    }

    @Override
    public boolean isConnected() {
        return pushTask.isConnected();
    }

    @Override
    public boolean isClosing() {
        return pushTask.isClosing();
    }

    @Override
    public boolean isClosed() {
        return pushTask.isClosed();
    }

}
