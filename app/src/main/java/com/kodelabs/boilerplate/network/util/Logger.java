package com.kodelabs.boilerplate.network.util;


import android.util.Log;


/**
 * Created by chenwf on 2016/12/13.
 */

public class Logger {

    public static void d(String tag, String objects) {
        if (!canLog()) {
            return;
        }
        Log.d(tag, objects);
    }

    public static void d(String tag, String objects, Throwable throwable) {
        if (!canLog()) {
            return;
        }
        Log.d(tag, objects, throwable);
    }

    private static boolean canLog() {
        return false;
    }

}
