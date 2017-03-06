package harbor.com.testndk;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    MyNdk myNdk;
    TextView mTv;
    private ByteBuffer mBf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        myNdk = new MyNdk();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Example of a call to a native method
        mTv = (TextView) findViewById(R.id.sample_text);
        mTv.setText(stringFromJNI());

        mBf = (ByteBuffer) myNdk.naMap();
        byte[] buf = new byte[mBf.capacity()];
        Log.i("buf capacity", mBf.capacity() + ";");
        mBf.get(buf);
        StringBuffer stringBuffer = new StringBuffer();
        for(int j=0; j<buf.length; j++){
            Log.i("", buf[j] + ":");
            stringBuffer.append(String.valueOf(buf[j]));
        }
        mTv.append(stringBuffer.toString() + "\n");

        startUpdateService();

//        for(int i=0; i< 3; i++){
//            mBf.rewind();
//            mBf.get(buf);
//            stringBuffer.setLength(0);
//            for(int j=0; j<buf.length; j++){
//                Log.i("", buf[j] +";");
//                stringBuffer.append(String.valueOf(buf[j]));
//            }
//            mTv.append(stringBuffer.toString() + "\n");
//            SystemClock.sleep(2000);
//        }
//        stopUpdateService();
//        myNdk.naUnmap();
    }

    private void startUpdateService() {
        Intent lIntent = new Intent(this, harbor.com.testndk.MemoryService.class);
        lIntent.putExtra(MemoryService.EXTRA_START_STOP, 0);
        startService(lIntent);
    }

    private void stopUpdateService() {
        Intent lIntent = new Intent(this, harbor.com.testndk.MemoryService.class);
        lIntent.putExtra(MemoryService.EXTRA_START_STOP, 1);
        startService(lIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public String stringFromJNI(){
        return myNdk.getString();
    }

}
