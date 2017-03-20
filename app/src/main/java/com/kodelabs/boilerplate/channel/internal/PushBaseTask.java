package com.kodelabs.boilerplate.channel.internal;

import android.text.TextUtils;

import com.kodelabs.boilerplate.channel.MessageProcessor;
import com.kodelabs.boilerplate.channel.PushUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by chenwf on 20170316.
 */
public abstract class PushBaseTask {

    private AtomicBoolean bConnetRunning = new AtomicBoolean(false);

    protected OnConnetListener mListener;
    protected String mUrl;

    public PushBaseTask(){
        super();
    }

    /**
     * 初始化
     */
    public abstract void init(OnConnetListener listener);

    /**
     * 连接ws通道
     */
    public abstract void connect();


    /**
     * 关闭ws通道
     */
    public abstract void closeConnect();

    /**
     * 发送消息
     */
    public abstract void realSendMsg(String msg);

    /**
     * 是否正在连接中
     */
    public abstract boolean isConnecting();

    /**
     * 是否已连接
     */
    public abstract boolean isConnected();

    public abstract boolean isClosing();

    /**
     * Returns whether the close handshake has been completed and the socket is closed.
     */
    public abstract boolean isClosed();

    /**
     * 关闭连接
     */
    public void close() {
        bConnetRunning.set(false);
        PushUtils.logcat("close()  bConnetRunning = " + bConnetRunning.get());
        PushStatus.setUnconneted();
        closeConnect();
    }

    /**
     * 执行连接
     */
    public void startConnet(String url) {
        PushUtils.logcat("PushBaseTask startConnet isConneteable = ", PushStatus.isConneteable(), " and isConnected = ", isConnected(),
                " or isConneting = ", isConnecting(), " and bConnetRunning = ", bConnetRunning.get());
        mUrl = url;
        if (PushStatus.isConneteable()) {
            if (!isConnected() && !isConnecting()) {
                if (!bConnetRunning.get()) {
                    bConnetRunning.set(true);
                    PushStatus.setConneting();
                    PushUtils.logcat("startConnet bConnetRunning === ", bConnetRunning.get());
                    OnConnetListener listener = new OnConnetListener() {

                        @Override
                        public void onSuccess() {
                            bConnetRunning.set(false);
                            PushStatus.setConneted();
                            PushUtils.logcat("conn onSuccess == bConnetRunning === " + bConnetRunning.get());
                            PushStatus.resetRetryCount();
                            MessageProcessor.get().optSendMessage();
                        }

                        @Override
                        public void onFailed(String errMsg) {
                            bConnetRunning.set(false);
                            PushStatus.setUnconneted();
                            PushUtils.logcat("conn onFailed bConnetRunning == " + bConnetRunning.get());
                            retryConnet();
                        }

                        @Override
                        public void onClose(String closeReason) {
                            bConnetRunning.set(false);
                            PushStatus.setUnconneted();
                            PushUtils.logcat("conn onClose closeReason = " + closeReason);
                        }
                    };
                    init(listener);
                    connect();
                }
            }
        } else {
            //不可连接
        }
    }

    public void retryConnet() {
        PushUtils.logcat("PushBaseTask retryConnet : PushStatus.isConneteable == " + PushStatus.isConneteable());
        if (PushStatus.isConneteable()) {
            PushStatus.addRetryOnce();
            connect();
        }
    }

    /**
     * 尝试发送指令，如不可连接时执行断开，如果isOpen则发送，断开则重连
     *
     * @param msg
     */
    public void trySendMsg(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }

        PushUtils.logcat("trySendMsg = ",  msg);

        if (!PushStatus.isConneteable()) {
            PushUtils.logcat("trySendMsg =不可连接执行断开= ");
            PushUtils.userUnConnect();
            return;
        }

        if (isConnected()) {
            realSendMsg(msg);
        } else {
            MessageProcessor.get().enqueueSendMessage(msg);
            retryConnet();
        }
    }

    public void onReceiveMessage(String message) {
        PushUtils.logcat("onReceiveMessage = " + message);
        MessageProcessor.get().receiveMessage(message);
    }

    protected void logcat(Object... logs){
        PushUtils.logcat(logs);
    }
}
