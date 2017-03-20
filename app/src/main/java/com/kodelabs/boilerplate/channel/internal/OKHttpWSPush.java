package com.kodelabs.boilerplate.channel.internal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Created by chenwf on 20170316.
 * OKhttp实现的ws通道
 *  https://github.com/square/okhttp
 */
public class OKHttpWSPush extends PushBaseTask {

    private OkHttpClient client = null;
    private WebSocket mWebSocket = null;

//    private ExecutorService mSendExecutor;

    public OKHttpWSPush(){
        super();
    }

    @Override
    public void init(OnConnetListener listener) {
        client = new OkHttpClient.Builder().build();
        mListener = listener;
    }

    @Override
    public void connect() {
        Request request = new Request.Builder().url(mUrl)
                .build();

        client.newWebSocket(request, new WebSocketListener() {
//            ExecutorService service;

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                logcat("OKHttpWSPush connect ", "onFailure ", t.getMessage());
//                if (mSendExecutor != null && !mSendExecutor.isShutdown()) {
//                    mSendExecutor.shutdown();
//                }

                if (mListener != null) {
                    mListener.onFailed(t.getMessage());
                }

            }

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                logcat("OKHttpWSPush connect ", "onOpen ");

                mWebSocket = webSocket;
//                service = Executors.newSingleThreadExecutor();
//                mSendExecutor = service;
                if (mListener != null) {
                    mListener.onSuccess();
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                onReceiveMessage(text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                onReceiveMessage(bytes.utf8());

            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                logcat("WebSocketClient onClose: code = " + code);
                logcat("WebSocketClient onClose: reason = " + reason);

//                if (mSendExecutor != null && !mSendExecutor.isShutdown()) {
//                    mSendExecutor.shutdown();
//                }
                if (mListener != null) {
                    mListener.onClose("code==" + code + "  reason== " + reason);
                }
            }
        });
    }

    @Override
    public void closeConnect() {
        if (mWebSocket != null) {
            try {
                mWebSocket.close(0, "byXLSDK");
            } catch (Exception e) {
                logcat("WebSocketClient closeConnect: Exception = " + e.getMessage());
            }
        }
    }

    @Override
    public void realSendMsg(final String msg) {
//        try {
//            if (mSendExecutor != null && !mSendExecutor.isShutdown()) {
//                mSendExecutor.execute(new Runnable() {
//                    @Override
//                    public void run() {
                        try {
                            if (isConnected()) {
                                mWebSocket.send(msg);
                            }
                        } catch (Exception e) {
                            logcat("WebSocketClient realSendMsg: Exception = " + e.getMessage());
                        }
//                    }
//                });
//            }
//        } catch (Exception e) {
//        }
    }

    @Override
    public boolean isConnecting() {
        return PushStatus.getConnetStatus() == PushStatus.PUSH_CONNET_STATUS_CONNETING;
    }

    @Override
    public boolean isConnected() {
        return PushStatus.getConnetStatus() == PushStatus.PUSH_CONNET_STATUS_CONNETED;
    }

    @Override
    public boolean isClosing() {
        return PushStatus.getConnetStatus() == PushStatus.PUSH_CONNET_STATUS_UNCONNETED;
    }

    @Override
    public boolean isClosed() {
        return PushStatus.getConnetStatus() == PushStatus.PUSH_CONNET_STATUS_UNCONNETED;
    }
}
