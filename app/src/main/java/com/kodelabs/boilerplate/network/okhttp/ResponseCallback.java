package com.kodelabs.boilerplate.network.okhttp;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by chenwf on 2017/1/4.
 */

public abstract class ResponseCallback implements okhttp3.Callback {
    @Override
    public void onFailure(Call call, final IOException e) {
        onFail(-103, e.toString());
    }

    @Override
    public void onResponse(Call call, final Response response) throws IOException {
        if (call.isCanceled()) {//请求被取消
            onFail(-100, "request is Canceled!");
            return;
        }

        if (!response.isSuccessful()) {//请求失败
            onFail(-101, "request failed , reponse's code is : " + response.code());
            return;
        }
        try {
            onSuccess(response);
        } catch (Exception e) {
            onFail(-102, e.toString());
        }
    }

    protected abstract void onFail(int errorCode, String errorMsg);

    protected abstract void onSuccess(Response response) throws IOException;
}
