package com.kodelabs.boilerplate.network.;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;

import com.kodelabs.boilerplate.network.listener.DownloadListener;
import com.kodelabs.boilerplate.network.okhttp.ResponseCallback;
import com.kodelabs.boilerplate.network.okhttp.UpdateHttpUtils;
import com.kodelabs.boilerplate.network.util.AppUtils;
import com.kodelabs.boilerplate.network.util.Logger;
import com.kodelabs.boilerplate.network.util.MD5;
import com.kodelabs.boilerplate.network.util.NetworkUtils;

import java.io.File;
import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Response;

/**
 * @author Harbor
 * @date 2016/8/9
 * 功能描述：升级的功能类   对外的接口基本在此类
 */

public class XLUpdateAgent {

    private static final String TAG = "XLUpdateAgent";
    private static Handler mHandler = new Handler(Looper.getMainLooper());

    private XLUpdateAgent() {
    }

    /**
     * 检测升级
     *
     * @param context 上下文
     * @param channel 渠道号
     */
    public static void update(final Context context, String channel, final XLUpdateListener listener) {

        if (!NetworkUtils.isNetworkAvailable(context)) {
            //无网络
            return;
        } else if (UpdateConfig.isUpdateOnlyWifi()) {
            if (!NetworkUtils.isWifi(context) && !UpdateConfig.isUpdateForce()) {
                //若非wifi 不更新
                if (listener != null) {
                    listener.onUpdateReturned(UpdateStatus.NoneWifi, null);
                }
                return;
            }
        }
        String version = AppUtils.getVersionName(context);
        String appId = context.getPackageName();
        if (TextUtils.isEmpty(version) || TextUtils.isEmpty(appId)) {
            return;
        }
        String url = "download_url;
        Logger.d(TAG, "update url=" + url);
        Logger.d(TAG, "update version=" + version);
        Logger.d(TAG, "update appId=" + appId);
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        builder.addQueryParameter("channel", channel);//渠道id
        builder.addQueryParameter("appid", appId);//后台约定的id
        builder.addQueryParameter("version", version);//app的版本
        builder.addQueryParameter("type", "2");//请求协议规定  android的type为2，1、pc；3、iOS

        UpdateHttpUtils.getInstance().getRequest(builder.build().toString(), new ResponseCallback() {
            @Override
            protected void onFail(int errorCode, String errorMsg) {
                exeRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onUpdateReturned(UpdateStatus.No, null);
                        }
                    }
                });
            }

            @Override
            protected void onSuccess(Response response) throws IOException {
                final String data = response.body().string();
                exeRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(data)) {
                            if (listener != null) {
                                listener.onUpdateReturned(UpdateStatus.No, null);
                            }
                            return;
                        }
                        
                    }
                });
            }
        });
    }


}
