package com.kodelabs.boilerplate.network;


import com.kodelabs.boilerplate.network.bean.UpdateBean;

/**
 * @author harbor
 * @date 2016/8/18
 * 功能描述：检测升级的监听事件
 */

public interface XLUpdateListener {
    public void onUpdateReturned(int updateStatus, UpdateBean appData);
    public void killApp();
}
