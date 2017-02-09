package com.kodelabs.boilerplate.network.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * 功能描述：获取app的信息的类
 */

public class AppUtils {


    /**
     * 获取app的VersionCode
     *
     * @param mContext 上下文
     * @return VersionCode
     */
    public static int getVersionCode(Context mContext) {
        if (mContext != null) {
            try {
                return mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        return 0;
    }

    /**
     * 获取app的版本
     *
     * @param mContext 上下文
     * @return app的版本
     */
    public static String getVersionName(Context mContext) {
        if (mContext != null) {
            try {
                return mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }

        return "";
    }

    /**
     * 安装app
     */
    public static void installAPk(Context context, File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //如果没有设置SDCard写权限，或者没有sdcard,apk文件保存在内存中，需要授予权限才能安装
        try {
            String[] command = {"chmod", "777", apkFile.toString()};
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
        } catch (IOException ignored) {
        }
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Throwable e) {
        }
    }

    public static String initFilePath(Context context, String version) {
        return Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator + context.getPackageName() + "_" + version + ".apk";
    }

}
