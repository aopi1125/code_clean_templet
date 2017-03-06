package harbor.com.testndk;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

public class MemoryService extends Service {

    public static final String EXTRA_START_STOP = "s_or_s";

    private Context mContext;
    private Thread myTask;
    private boolean bRunning = false;

    public MemoryService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        myTask = new Thread(new MyTask());
        naMap();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        int startStop = intent.getIntExtra(EXTRA_START_STOP, 0);
        if(startStop <= 0){
            bRunning = true;
            myTask.start();
        }else{
            bRunning = false;
//            naUnmap();
            stopSelf();
        }
    }

    private class MyTask implements Runnable{
        @Override
        public void run() {
            while (bRunning){
                naUpdate();
                SystemClock.sleep(300);
            }
        }
    }

    private static native void naUpdate();
    private static native void naMap();
    private static native void naUnmap();

    static {
        System.loadLibrary("HarborLibrary");
    }

}
