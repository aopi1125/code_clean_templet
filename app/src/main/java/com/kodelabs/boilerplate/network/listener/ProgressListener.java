package com.kodelabs.boilerplate.network.listener;

/**
 * Created by chenwf on 2017/1/4.
 */

public interface ProgressListener {
    void onResponseProgress(long byteRead, long totalLength, boolean finish);
}