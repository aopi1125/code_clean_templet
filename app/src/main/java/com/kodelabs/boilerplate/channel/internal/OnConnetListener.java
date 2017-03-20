package com.kodelabs.boilerplate.channel.internal;

/**
 * Created by chenwf on 20170316.
 */
public interface OnConnetListener {

    public void onSuccess();

    public void onFailed(String errMsg);

    public void onClose(String closeReason);

}
