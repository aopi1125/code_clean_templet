package com.kodelabs.boilerplate.network.okhttp;


import android.text.TextUtils;

import com.kodelabs.boilerplate.network.listener.DownloadListener;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;


/**
 * OkHttp工具类，封装下载进度监听
 * <br>
 * create on 2017/1/4
 */
public class UpdateHttpUtils {
    private volatile static UpdateHttpUtils mInstance;
    private OkHttpClient mOkHttpClient;

    private UpdateHttpUtils() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (UpdateConfig.isDebug()) {
            builder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    String outBefore = String.format("发送请求：%s %n [body]:%s %n [headers]:%s", request.url(), request.body(), request.headers());
                    Log.d("OkHttpUtils", outBefore);

                    Response response = chain.proceed(request);
                    String outAfter = String.format("接收返回： %s %n [headers]:%s %n [body]：%s", response.request().url(), response.headers(), response.body());
                    Log.d("OkHttpUtils", outAfter);
                    return response;
                }
            });
        }

        mOkHttpClient = builder.retryOnConnectionFailure(true).build();
    }

    public static UpdateHttpUtils getInstance() {
        if (mInstance == null) {
            synchronized (UpdateHttpUtils.class) {
                if (mInstance == null) {
                    mInstance = new UpdateHttpUtils();
                }
            }
        }
        return mInstance;
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public void getRequest(String url, ResponseCallback callback) {
        Request request = new Request.Builder().get().url(url).tag(url).build();
        mOkHttpClient.newCall(request).enqueue(callback);
    }

    public void downloadApp(String url, String filePath, final DownloadListener listener) {
        if (TextUtils.isEmpty(filePath) || TextUtils.isEmpty(url)) {
            if (listener != null) {
                listener.onFail(-1, "下载失败");
            }
            return;
        }
        final File file = new File(filePath);
        if (file.exists()) {
                //String fileMd5 = MD5.getFileMD5(file);
                //if (FileSizeUtil.getFileOrFilesSize(filePath, SIZETYPE_B) == updateBean.getData().getSize() &&
                //        (!TextUtils.isEmpty(fileMd5) && fileMd5.equalsIgnoreCase(updateBean.getData().getMd5()))) {
                //    if (listener != null) {
                //        listener.onSuccess(file);
                //    }
                    //已下载过
                //    return;
                //}
        }
        OkHttpClient client = mOkHttpClient.newBuilder().addInterceptor(new ResponseInterceptor(listener)).build();
        Request request = new Request.Builder().get().url(url).tag(url).build();
        client.newCall(request).enqueue(new ResponseCallback() {
            @Override
            protected void onFail(final int errorCode, final String errorMsg) {
                if (listener != null) {
                    listener.onFail(errorCode, errorMsg);
                }
            }

            @Override
            protected void onSuccess(Response response) throws IOException {
                try {
                    BufferedSink bufferedSink = Okio.buffer(Okio.sink(file));
                    bufferedSink.writeAll(response.body().source());
                    bufferedSink.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (listener != null) {
                    listener.onSuccess(file);
                }
            }
        });
    }

    public void canceRequest(String tag) {
        for (Call call : mOkHttpClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : mOkHttpClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }
}

