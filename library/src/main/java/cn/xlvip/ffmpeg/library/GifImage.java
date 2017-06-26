package cn.xlvip.ffmpeg.library;

import android.graphics.Bitmap;

/**
 * Created by chenwenfeng on 2017/5/23.
 */

public class GifImage {
    public static final int TAG_TOP_LEFT = 1;//左上
    public static final int TAG_TOP_RIGHT = 2;//右上
    public static final int TAG_BOTTOM_LEFT = 3;//左下
    public static final int TAG_BOTTOM_RIGHT = 4;//右下

    private int mLocation;//gif所在的位置 1(left_top)、2(right_top)、3(left_bottom)、4(right_bottom)
    private int mFrameCount = 1;//所有的帧数，理论上至少有一帧以上
    private long mDuration;//gif时长，单位毫秒
    private Bitmap[] mFrameBitmap;//每一帧的bitmap
    private long[] mFrameDelays;//每一帧的间隔，单位毫秒；1毫秒= 1000,000纳秒
    private String mFilePath;//gif文件所在路径
    private Bitmap mTextBitmap;//文字信息
    private boolean bIsGif = true;//是否gif图片
//    private GifDrawable mGifDrawable;
//
//    public GifDrawable getGifDrawable() {
//        return mGifDrawable;
//    }
//
//    public void setGifDrawable(GifDrawable mGifDrawable) {
//        this.mGifDrawable = mGifDrawable;
//    }

    public boolean isGif() {
        return bIsGif;
    }

    public void setIsGif(boolean bIsGif) {
        this.bIsGif = bIsGif;
    }

    public Bitmap getTextBitmap() {
        return mTextBitmap;
    }

    public void setTextBitmap(Bitmap mTextBitmap) {
        this.mTextBitmap = mTextBitmap;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String mFilePath) {
        this.mFilePath = mFilePath;
    }

    /**
     * @return 返回模拟，需要绘制的帧数
     */
    public int getPlayCount() {
        return mFrameCount;
    }

    public long getDuration(){
        return mDuration;
    }

    public void setDuration(long mDuration) {
        this.mDuration = mDuration;
    }

    public void setPlayCount(int count){
        mFrameCount = count;
    }

    public void setLocation(int location) {
        this.mLocation = location;
    }

    public int getLocation() {
        return mLocation;
    }

    public void setFrameBitmaps(Bitmap[] bms) {
        mFrameBitmap = bms;
    }

    public Bitmap[] getFrameBitmaps() {
        return mFrameBitmap;
    }

    public void setFrameDelays(long[] frameDelays) {
        this.mFrameDelays = frameDelays;
    }

    public long[] getFrameDelays() {
        return this.mFrameDelays;
    }

    public Bitmap getBitmap(int index) {
        if (mFrameBitmap == null) {
            return null;
        }
        if (index >= mFrameBitmap.length) {
            throw new IllegalArgumentException("index is invalid..");
        }
        return mFrameBitmap[index];
    }

    /**
     * @return 返回位图的具体实数
     */
    public int getRealBitmapCount(){
        if(mFrameBitmap != null){
            return mFrameBitmap.length;
        }
        return 0;
    }

    public long getDelay(int index) {
        if (mFrameDelays == null) {
            return 0;
        }
        if (index >= mFrameDelays.length) {
            throw new IllegalArgumentException("index is invalid..");
        }
        return mFrameDelays[index];
    }

    /**
     *
     * @param index 例如2， 则是0+1+2即第一帧到第三帧的总时长
     * @return 获取前面帧的所有时间
     */
    public long getSumDelays(int index){
        if (mFrameDelays == null) {
            return 0;
        }
        if (index >= mFrameDelays.length) {
            return getDuration();
        }
        long sum = 0;
        for(int i=0; i<=index; i++){
            sum += getDelay(i);
        }
        return sum;
    }

    /**
     * @return 获取gif的平均帧率，单位：帧/秒
     */
    public int getFPS(){
        if(mFrameCount <= 0 || mDuration <= 0){
            return XLGifToVideoManager.FRAME_RATE;
        }
        return Math.round(mFrameCount * 1000.0f / mDuration);
    }

    //释放位图
    public void release() {
        if(mTextBitmap != null){
            if (!mTextBitmap.isRecycled()) {
                mTextBitmap.recycle();
            }
        }
        if (mFrameBitmap != null) {
            for (Bitmap bm : mFrameBitmap) {
                if (bm != null && !bm.isRecycled()) {
                    bm.recycle();
                }
            }
        }
    }
}
