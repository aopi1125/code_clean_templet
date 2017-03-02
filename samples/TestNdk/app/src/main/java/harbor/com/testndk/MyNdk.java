package harbor.com.testndk;

/**
 * Created by fish on 2017/3/2.
 */

public class MyNdk {

    static {
        System.loadLibrary("HarborLibrary");
    }

    public native String getString();
}
