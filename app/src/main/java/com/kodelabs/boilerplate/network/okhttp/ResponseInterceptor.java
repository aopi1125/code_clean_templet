package com.kodelabs.boilerplate.network.okhttp;

import com.kodelabs.boilerplate.network.listener.DownloadListener;
import com.kodelabs.boilerplate.network.listener.ProgressListener;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by chenwf on 2017/1/4.
 *
 * 下载流拦截器，主要做进度展示
 *
 */

public class ResponseInterceptor implements Interceptor {

    private DownloadListener mDlListener;

    protected ResponseInterceptor(DownloadListener listener) {
        this.mDlListener = listener;
    }

    ProgressListener listener = new ProgressListener() {
        @Override
        public void onResponseProgress(long byteRead, long totalLength, boolean finish) {
            final int percentage = (int) (byteRead * 100 / totalLength);
            if (mDlListener != null) {
                mDlListener.onUpdateInProgress(percentage);
            }
        }
    };

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        return response.newBuilder().body(new ProgressResponseBody(response.body(), listener)).build();
    }
}
