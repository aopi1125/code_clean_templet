package com.kodelabs.boilerplate.channel;

import android.text.TextUtils;
import java.util.concurrent.ArrayBlockingQueue;
import android.content.Intent;
import java.util.LinkedList;

/**
 * Created by chenwf on 2017/3/16.
 * 消息处理器
 */

public class MessageProcessor {

    private static volatile MessageProcessor mInstance;

    public static MessageProcessor get(){
        if(mInstance == null){
            synchronized (MessageProcessor.class){
                mInstance = new MessageProcessor();
            }
        }
        return mInstance;
    }

    private final ArrayBlockingQueue<String> mMsgQueue = new ArrayBlockingQueue<String>(100);

    private LinkedList<String> mSendMsgCache = new LinkedList<>();

    public void enqueueSendMessage(String message){
        synchronized (mSendMsgCache){
            mSendMsgCache.add(message);
        }
    }

    public void optSendMessage(){
        LinkedList<String> list = null;
        synchronized (mSendMsgCache){
            if(mSendMsgCache.isEmpty()){
                return;
            }
            list = new LinkedList<>();
            list.addAll(mSendMsgCache);
            mSendMsgCache.clear();
        }
        if(list != null && !list.isEmpty()){
            for(String message : list){
                PushUtils.userSendMessage(message);
            }
        }
    }

    /*****-----received messages----------------------**/
    public void receiveMessage(String message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        mMsgQueue.offer(message);
        startMessageService();
    }

    public void disposeMessages(){
        String message = null;
        try {
            message = mMsgQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(!TextUtils.isEmpty(message)){
            doMessage(message);
        }
    }

    private void startMessageService(){
        Intent intent = new Intent(PushUtils.mAppContext, OnReceiveMsgService.class);
        PushUtils.mAppContext.startService(intent);
    }

    private void doMessage(String message){
        // TODO: 2017/3/16
    }

    /*****-----received messages End----------------------**/

}
