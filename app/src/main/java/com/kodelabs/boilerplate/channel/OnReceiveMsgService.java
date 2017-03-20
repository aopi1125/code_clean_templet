package com.kodelabs.boilerplate.channel;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by chenwf on 20170316.
 */
public class OnReceiveMsgService extends IntentService {

    private static final String SERVICE_NAME = "com.kodelabs.boilerplate.channel.OnReceiveMsgService";

    public OnReceiveMsgService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        MessageProcessor.get().disposeMessages();
    }
}
