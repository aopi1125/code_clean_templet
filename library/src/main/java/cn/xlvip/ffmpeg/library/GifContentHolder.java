package cn.xlvip.ffmpeg.library;

import android.graphics.Bitmap;
import android.graphics.Point;

/**
 * Created by chenwenfeng on 2017/5/25.
 */

public class GifContentHolder {

    public enum Order{
        /**
         * 同时播放
         */
        SameTime,
        /**
         * 顺序播放
         */
        Squence,
    }

    private Bitmap mWaterMarkBitmap = null;//水印
    private GifImage[] mGifs = new GifImage[XLGifToVideoManager.GIF_COUNT];//所有gif信息，必须保证集齐4个图
    private String mAudioPath;//音频路径
    private Order mOrderType = Order.Squence;//播放时序
    private Point mWidthAndHeight = new Point(0, 0);

    public Point getWidthAndHeight() {
        return mWidthAndHeight;
    }

    public void setWidthAndHeight(int width, int height) {
        mWidthAndHeight.x = width;
        mWidthAndHeight.y = height;
    }

    public GifImage[] getGifs() {
        return mGifs;
    }
    public void setGifs(GifImage[] sGifs) {
        if(sGifs != null && sGifs.length == XLGifToVideoManager.GIF_COUNT){
            this.mGifs = sGifs;
        }
    }

    public Order getOrderType() {
        return mOrderType;
    }

    public void setOrderType(Order mOrderType) {
        this.mOrderType = mOrderType;
    }

    public String getAudioPath() {
        return mAudioPath;
    }

    public void setAudioPath(String mAudioPath) {
        this.mAudioPath = mAudioPath;
    }

    /**
     * @return 同时播放：最大的帧数
     * 或者
     * 顺序播放：4个gif的总帧数
     */
    public int getFrameCounts() {
        int count = 0;
        if (mOrderType == Order.SameTime) {
            return getMaxFrames();
        } else {
            for (GifImage image : mGifs) {
                if (image != null) count += image.getPlayCount();
            }
        }
        return count;
    }

    public Bitmap getWaterMarkBitmap() {
        return mWaterMarkBitmap;
    }

    public void setWaterMarkBitmap(Bitmap mWaterMarkBitmap) {
        this.mWaterMarkBitmap = mWaterMarkBitmap;
    }

    /**
     * @return 4个gif中最多的帧数
     */
    public int getMaxFrames(){
        int count = 0;
        for(GifImage image : mGifs){
            if(image.getPlayCount() > count){
                count = image.getPlayCount();
            }
        }
        return count;
    }

    //释放内存
    public void release(){
        if(mWaterMarkBitmap != null && !mWaterMarkBitmap.isRecycled()){
            mWaterMarkBitmap.recycle();
        }
        for(GifImage image : mGifs){
            if(image != null){
                image.release();
            }
        }
        mGifs = null;
    }


}
