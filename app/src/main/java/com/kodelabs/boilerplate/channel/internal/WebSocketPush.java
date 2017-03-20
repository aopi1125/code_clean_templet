package com.xunlei.common.channel.internal;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenwf on 20170316
 * link: https://github.com/TooTallNate/Java-WebSocket.
 *
 *http://www.java2s.com/Code/Jar/j/Downloadjavawebsocket130jar.htm
 */
public class WebSocketPush extends PushBaseTask {

    WebSocketClient mWebSocketClient;

    public WebSocketPush(){
        super();
    }

    @Override
    public void init(OnConnetListener listener) {

        mListener = listener;
        URI uri = URI.create(mUrl);

        Map<String,String> headers = new HashMap<>();
        // TODO: 2017/3/16 add your headers

        mWebSocketClient = new WebSocketClient(uri, new Draft_17(), headers, 0) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                logcat("WebSocketClient Opened " + (mWebSocketClient != null ? mWebSocketClient.getURI().toString() : ""));

                if (mListener != null) {
                    mListener.onSuccess();
                }
            }

            @Override
            public void onMessage(String message) {
                onReceiveMessage(message);
            }

            @Override
            public void onClose(int code, String message, boolean remote) {
                logcat("WebSocketClient onClose: code = ", code);
                logcat("WebSocketClient onClose: message = ", message);
                logcat("WebSocketClient onClose: remote = ", remote);

                if (mListener != null) {
                    mListener.onClose("remote=" + remote + ",code=" + code + ",message= " + message);
                }
            }

            @Override
            public void onError(Exception e) {
                logcat("WebSocketClient onError: " + e.getMessage());

                if (mListener != null) {
                    mListener.onFailed(e.getMessage());
                }
            }
        };
        logcat("WebSocketClient init is : " + mWebSocketClient.toString());
    }

    @Override
    public void connect() {
        logcat("WebSocketPush connect...");
        if (mWebSocketClient != null) {
            try {
                mWebSocketClient.connect();
            } catch (Exception e) {
                logcat("WebSocketClient Connet Exception : " + e.getMessage());
            }
        }
    }

    @Override
    public void closeConnect() {
        logcat("WebSocketPush closeConnect...");
        if(mWebSocketClient != null) {
            mWebSocketClient.close();
        }
    }

    @Override
    public void realSendMsg(String msg) {
        logcat("realSendMsg == " + msg);
        if (mWebSocketClient != null) {
            mWebSocketClient.send(msg);
        }
    }

    @Override
    public boolean isConnecting() {
        if (mWebSocketClient != null && mWebSocketClient.getConnection() != null) {
            return mWebSocketClient.getConnection().isConnecting();
        }
        return false;
    }

    @Override
    public boolean isConnected() {
        if (mWebSocketClient != null && mWebSocketClient.getConnection() != null) {
            return mWebSocketClient.getConnection().isOpen();
        }
        return false;
    }

    @Override
    public boolean isClosing() {
        if (mWebSocketClient != null && mWebSocketClient.getConnection() != null) {
            return mWebSocketClient.getConnection().isClosing();
        }
        return false;
    }

    @Override
    public boolean isClosed() {
        if (mWebSocketClient != null && mWebSocketClient.getConnection() != null) {
            return mWebSocketClient.getConnection().isClosed();
        }
        return true;
    }

}
