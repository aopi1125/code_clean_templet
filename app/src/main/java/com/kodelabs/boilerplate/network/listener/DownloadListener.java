package com.kodelabs.boilerplate.network.listener;

import java.io.File;

/**
 * Created by chenwf on 2017/1/4.
 */

public interface DownloadListener {
    void onUpdateInProgress(int process);

    void onFail(int errorCode, String errorMsg);

    void onSuccess(File file);
}
