package cn.xlvip.ffmpeg.library.ext;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import cn.xlvip.ffmpeg.library.GifContentHolder;
import cn.xlvip.ffmpeg.library.GifImage;
import cn.xlvip.ffmpeg.library.XLGifToVideoManager;
import cn.xlvip.ffmpeg.library.gif.GifDecoder;


/**
 * Created by chenwenfeng on 2017/5/22.
 */

public class Utils {
    private final static int MAX_TIME = 1000;//seconds
    private final static int ONE_SECONDS = 1000;//1秒=1000毫秒

    public static final int[] TIME_GROUPS = new int[]{30, 68, 100};//100, 进度不准

    // a integer to xx:xx
    public static String secToTime(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if(time > MAX_TIME){
            time = MAX_TIME;
        }
        if (time <= 0)
            return "00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    private static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }

    public static void fileToGifImage(GifImage source, GifContentHolder gifCOntentHolder, Paint paint)
            throws FileNotFoundException, MalformedURLException, IOException{
        if (TextUtils.isEmpty(source.getFilePath())) {
            return;
        }
        String path = source.getFilePath();
        boolean orderPlay = gifCOntentHolder.getOrderType() == GifContentHolder.Order.Squence;
        int location = source.getLocation();

        for(GifImage gif : gifCOntentHolder.getGifs()){
            if(gif != null && gif.getLocation() != location && path.equals(gif.getFilePath())){
                //同个路径的图片
                Bitmap[] bitmps = gif.getFrameBitmaps();
                if(bitmps != null && bitmps.length > 0 && gif.getTextBitmap() == null && source.getTextBitmap() == null){
                    if(XLGifToVideoManager.DebuggAble) {
                        Log.d("Utils", "gifDecoder :" + location + "; same =" + gif.getLocation());
                    }
                    source.setPlayCount(gif.getPlayCount());
                    source.setFrameBitmaps(bitmps);
                    source.setFrameDelays(gif.getFrameDelays());
                    source.setDuration(gif.getDuration());
                    source.setLocation(location);
                    source.setFilePath(path);
                    source.setIsGif(gif.isGif());
                    return;//1
                }
            }
        }

//        GifDrawable gifDrawable = source.getGifDrawable();
//        if(gifDrawable != null){
//            gifDrawable.stop();
//            com.bumptech.glide.gifdecoder.GifDecoder decoder = gifDrawable.getDecoder();
//            decoder.resetFrameIndex();
//            decoder.advance();
//            int count = decoder.getFrameCount();
//            int duration = 0;
//            Bitmap[] bitmaps = new Bitmap[count];
//            long[] delays = new long[count];
//            for (int i = 0; i < count; i++) {
//                Bitmap bitmap = decoder.getNextFrame();
//                if (bitmap != null && !bitmap.isRecycled()) {
//                    boolean text = (i != 0) || (!orderPlay);
//                    bitmap = cropImage(gifCOntentHolder, source, bitmap, true, paint, text);
//                }
//                bitmaps[i] = bitmap;
//                int currentFrameIndex = decoder.getCurrentFrameIndex();
//                int delay = decoder.getDelay(currentFrameIndex);
//                delays[i] = delay;
//                duration += delays[i];
//                decoder.advance();
//            }
//            source.setPlayCount(count);
//            source.setFrameBitmaps(bitmaps);
//            source.setFrameDelays(delays);
//            source.setDuration(duration);
//            source.setLocation(location);
//            source.setFilePath(path);
//            source.setIsGif(true);
//            return;//2
//        }

//        if(XLGifToVideoManager.isNetworkUrl(path)){
//            File file = null;
//            try {
//                file = XLGifToVideoManager.getFileByUrl(path);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
//            if(file != null && file.exists()){
//                path = file.getAbsolutePath();
//            }
//        }

        int result = GifDecoder.STATUS_OPEN_ERROR;
        GifDecoder gifDecoder = new GifDecoder();
        if (path.startsWith("/")) {
            result = gifDecoder.read(new FileInputStream(new File(path)));
        } else if (XLGifToVideoManager.isNetworkUrl(path)) {
            //网络图
            URL url = new URL(path);
            URLConnection conn = url.openConnection();
            result = gifDecoder.read(conn.getInputStream());
        } else {
            ContentResolver contentResolver = XLGifToVideoManager.sContext.getContentResolver();
            result = gifDecoder.read(contentResolver.openInputStream(Uri.parse(path)));
        }
        if (result == GifDecoder.STATUS_OK) {
            //gif
            int count = gifDecoder.getFrameCount();
            int duration = 0;
            Bitmap[] bitmaps = new Bitmap[count];
            long[] delays = new long[count];
            if(XLGifToVideoManager.DebuggAble) {
                Log.d("Utils", "gifDecoder :" + location + "; count=" + count);
            }
            for (int i = 0; i < count; i++) {
                Bitmap bitmap = gifDecoder.getFrame(i);
                if (bitmap != null && !bitmap.isRecycled()) {
                    boolean text = (i != 0) || (!orderPlay);
                    bitmap = cropImage(gifCOntentHolder, source, bitmap, true, paint, text);
                }
                bitmaps[i] = bitmap;
                delays[i] = gifDecoder.getDelay(i);
                if(delays[i] <= 0){
                    delays[i] = ONE_SECONDS / XLGifToVideoManager.FRAME_RATE;
                }
                duration += delays[i];
            }

            if(orderPlay && count == 1){
                //特殊处理单帧的gif
                Bitmap bitmap = bitmaps[0];
                bitmaps = new Bitmap[2];
                bitmaps[0] = bitmap;
                Bitmap bm2 = cropImage(gifCOntentHolder, source, bitmap, false, paint, true);
                bitmaps[1] = bm2;
                source.setPlayCount(XLGifToVideoManager.FRAME_RATE);
                source.setFrameBitmaps(bitmaps);
                delays = new long[source.getPlayCount()];
                for (int i = 0; i < delays.length; i++) {
                    delays[i] = ONE_SECONDS / source.getPlayCount();
                }
                source.setFrameDelays(delays);
                source.setDuration(ONE_SECONDS);
                source.setLocation(location);
                source.setFilePath(path);
                source.setIsGif(false);//这里保持跟静态图一致，无须补帧
                return;
            }

            source.setPlayCount(count);
            source.setFrameBitmaps(bitmaps);
            source.setFrameDelays(delays);
            if(duration <= 0){
                duration = count * source.getFPS();
            }
            source.setDuration(duration);
            source.setLocation(location);
            source.setFilePath(path);
            source.setIsGif(true);
        } else if (result == GifDecoder.STATUS_FORMAT_ERROR) {
            //png or jpeg
//            source.setPlayCount(orderPlay ? XLGifToVideoManager.FRAME_RATE : 1);
            source.setPlayCount(XLGifToVideoManager.FRAME_RATE);
            long[] delays = new long[source.getPlayCount()];
            Bitmap[] bitmaps = null;
            Bitmap bitmap = decodeSampledBitmap(path, gifCOntentHolder.getWidthAndHeight().x, gifCOntentHolder.getWidthAndHeight().y);
            if (bitmap != null && !bitmap.isRecycled()) {
                if(!orderPlay){
                    Bitmap bm1 = cropImage(gifCOntentHolder, source, bitmap, true, paint, true);
                    bitmaps = new Bitmap[1];
                    bitmaps[0] = bm1;
                }else{
                    bitmaps = new Bitmap[2];
                    Bitmap bm1 = cropImage(gifCOntentHolder, source, bitmap, false, paint, false);
                    bitmaps[0] = bm1;
                    Bitmap bm2 = cropImage(gifCOntentHolder, source, bitmap, true, paint, true);
                    bitmaps[1] = bm2;
                }
            }
            for (int i = 0; i < delays.length; i++) {
                delays[i] = ONE_SECONDS / source.getPlayCount();
            }
            source.setFrameBitmaps(bitmaps);
            source.setDuration(ONE_SECONDS);
            source.setFrameDelays(delays);
            source.setLocation(location);
            source.setFilePath(path);
            source.setIsGif(false);
        }//3
    }

    /**
     * 按正方形裁切图片中间，并加上水印（若是最后的位置）
     */
    private static Bitmap cropImage(GifContentHolder gifCOntentHolder, GifImage source, Bitmap bitmap, boolean recycleSource, Paint paint, boolean drawText) {
        if (bitmap == null || gifCOntentHolder == null || source == null) {
            return null;
        }
        Bitmap waterBm = gifCOntentHolder.getWaterMarkBitmap();
        int location = source.getLocation();

        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        Bitmap bmp = null;
        if (w == h) {
            bmp = bitmap;
        } else {
            int wh = w > h ? h : w;// 裁切后所取的正方形区域边长
            int retX = w > h ? (w - h) / 2 : 0;// 基于原图，取正方形左上角x坐标
            int retY = w > h ? 0 : (h - w) / 2;

            bmp = Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null,
                    false);
        }

        if(!drawText || source.getTextBitmap() == null || source.getTextBitmap().isRecycled()) {
            //不需要文字
            if (location != GifImage.TAG_BOTTOM_RIGHT || waterBm == null) {
            }else{
                if(!bmp.isMutable()){
                    bmp = bmp.copy(Bitmap.Config.ARGB_8888, true);
                }
                Canvas canvas = new Canvas(bmp);
                addWaterMark(canvas, waterBm, paint);
            }
            //不要水印
            if (recycleSource && bitmap != null && !bitmap.equals(bmp)
                    && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            return bmp;
        }
        SoftReference<Bitmap> bitmapSoftReference = new SoftReference<Bitmap>(bmp);
        Bitmap value = Bitmap.createScaledBitmap(bitmapSoftReference.get(), gifCOntentHolder.getWidthAndHeight().x, gifCOntentHolder.getWidthAndHeight().y, false);
        if(recycleSource && bitmapSoftReference.get() != null){
            bitmapSoftReference.get().recycle();
        }
        Canvas canvas = new Canvas(value);
        if(drawText){
            if(source.getTextBitmap() != null && !source.getTextBitmap().isRecycled()){
                drawTextBitmap(canvas, source.getTextBitmap(),paint);
            }
        }

        if (location == GifImage.TAG_BOTTOM_RIGHT && waterBm != null) {
            addWaterMark(canvas, waterBm, paint);
            //保存所有元素
            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
        } else{
            //保存所有元素
            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
        }
        if (recycleSource && bitmap != null && !bitmap.equals(value)
                && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return value;
    }

    /**
     * 在右下角添加水印
     * @param markBitmap
     * @return
     */
    private static void drawTextBitmap(Canvas canvas, Bitmap markBitmap, Paint paint) {
        // 当水印文字与水印图片都没有的时候，返回原图
        if (markBitmap == null || markBitmap.isRecycled() || canvas == null ) {
            return;
        }
        int w = canvas.getWidth();
        float qrWidth = w;
        int markBitmapWidth = markBitmap.getWidth();
        int markBitmapHeight = markBitmap.getHeight();
        float qrHeight = qrWidth * markBitmapHeight / markBitmapWidth;
        float left = 0;
        float top = 0;
        // 图片开始的坐标
        RectF watermarkRect = new RectF(left, top, left + qrWidth, top + qrHeight);
        canvas.drawBitmap(markBitmap, null, watermarkRect, paint);
    }

    /**
     * 在右下角添加水印
     * @param markBitmap
     * @return
     */
    private static void addWaterMark(Canvas canvas, Bitmap markBitmap, Paint paint) {
        // 当水印文字与水印图片都没有的时候，返回原图
        if (markBitmap == null || markBitmap.isRecycled() || canvas == null) {
            return;
        }
        int w = canvas.getWidth();
        float qrWidth = w * 0.3f;
        int markBitmapWidth = markBitmap.getWidth();
        int markBitmapHeight = markBitmap.getHeight();
        float qrHeight = qrWidth * markBitmapHeight / markBitmapWidth;
        float left = w * (1 - 0.3f - 0.03F);
        float top = w - qrHeight - w * 0.03F;
        // 图片开始的坐标
        RectF watermarkRect = new RectF(left, top, left + qrWidth, top + qrHeight);
        canvas.drawBitmap(markBitmap, null, watermarkRect, paint);
    }

    /**
     * 图片等比例压缩
     *
     * @param filePath
     * @return
     */
    private static Bitmap decodeSampledBitmap(String filePath, int width, int height) throws FileNotFoundException {
        if(TextUtils.isEmpty(filePath)) return null;
        String path = getRealFilePathFromUri(XLGifToVideoManager.sContext, Uri.parse(filePath));
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, width, height);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        //竖屏拍照的照片，直接使用的话，会旋转90度，下面代码把角度旋转过来
        int rotation = getExifOrientation(path); //查询旋转角度
        if(rotation == 0){
            return bitmap;
        }
        Matrix m = new Matrix();
        m.setRotate(rotation);
        Bitmap bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        bitmap.recycle();
        return bm;
    }

    /**
     * 处理同时播放，帧率的统一。 若范围差较小，取平均值。若差别大则采取靠小补帧策略；
     * 若是顺序播放，不应进入此方法处理
     *
     * 例如：原序列帧：1|2|3|4|5|6... 补帧后为： 1|1|2|2|3|3|4|4|5|5|6|6...
     *
     * @param gifContentHolder
     * @param minFps
     * @param maxFps
     * @param criticalFps
     * @return 帧率
     */
    public static int handleForSameFps(GifContentHolder gifContentHolder, int minFps, int maxFps, int criticalFps) {
        int min = 0, max = 0;
        int numFps = 0;
        for (int i = 0; i < gifContentHolder.getGifs().length; i++) {
            int fps = gifContentHolder.getGifs()[i].getFPS();
            numFps += fps;
            min = Math.min(fps, min);
            max = Math.max(fps, max);
        }
        int averageFps = Math.round(numFps * 1.0f / gifContentHolder.getGifs().length);//平均值

        int difference1 = 0;
        int difference2 = 0;
        for(int i=0; i< gifContentHolder.getGifs().length; i++){
            int fps = gifContentHolder.getGifs()[i].getFPS();
            difference1 += Math.pow(fps-averageFps, 2);
            if(fps < criticalFps){
                difference2 += Math.pow(fps-minFps, 2);
            }else{
                difference2 += Math.pow(fps-maxFps, 2);
            }
        }

        //判断范围差
        if (difference1 <= difference2) {
            //获取到平均帧率，无需补帧
        } else {
            averageFps = max > criticalFps ? maxFps : minFps;
            for (int i = 0; i < gifContentHolder.getGifs().length; i++) {
                int fps = gifContentHolder.getGifs()[i].getFPS();
                if (fps < criticalFps && averageFps >= maxFps) {
                    //补帧
                    repairFramesDouble(gifContentHolder.getGifs()[i], averageFps);
                } else {
                    //无需处理
                }
            }
        }
        return averageFps;
    }

    private static void repairFramesDouble(GifImage gifImage, int fps) {
        if (gifImage.isGif()) {
            Bitmap[] bitmaps = new Bitmap[gifImage.getRealBitmapCount() * 2];
            for (int i = 0; i < gifImage.getRealBitmapCount(); i++) {
                Bitmap bitmap = gifImage.getBitmap(i);
                bitmaps[i*2] = bitmap;
                bitmaps[i*2 + 1] = bitmap;
            }
            gifImage.setPlayCount(bitmaps.length);
            gifImage.setFrameBitmaps(bitmaps);
        } else {
            //直接显示1秒
            gifImage.setPlayCount(fps);
        }
    }


    /**
     * 根据Uri返回文件绝对路径
     * 兼容了file:///开头的 和 content://开头的情况
     *
     * @param context
     * @param uri
     * @return the file path or null
     */
    public static String getRealFilePathFromUri(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 计算InSampleSize
     * 宽的压缩比和高的压缩比的较小值  取接近的2的次幂的值
     * 比如宽的压缩比是3 高的压缩比是5 取较小值3  而InSampleSize必须是2的次幂，取接近的2的次幂4
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            int ratio = heightRatio < widthRatio ? heightRatio : widthRatio;
            // inSampleSize只能是2的次幂  将ratio就近取2的次幂的值
            if (ratio < 3)
                inSampleSize = ratio;
            else if (ratio < 6.5)
                inSampleSize = 4;
            else if (ratio < 8)
                inSampleSize = 8;
            else
                inSampleSize = ratio;
        }

        return inSampleSize;
    }


    /**
     * 查询图片旋转角度
     *
     * @param filepath
     * @return
     */
    public static int getExifOrientation(String filepath) {// YOUR MEDIA PATH AS STRING
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }

            }
        }
        return degree;
    }


    public static interface OnGetGif{
        public void onPreLoad();
        public void onLoaded(boolean success, GifImage image);
    }

    public static void analyzeGif(final int location, final InputStream path, final OnGetGif onGetGif) {
        AsyncTask<Void, Void, GifImage> mRunCommandAsyncTask = new AsyncTask<Void, Void, GifImage>() {
            private StringBuffer sb = new StringBuffer();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if(onGetGif != null){
                    onGetGif.onPreLoad();
                }
            }

            @Override
            protected GifImage doInBackground(Void... strings) {
                int result = GifDecoder.STATUS_OPEN_ERROR;
                GifDecoder gifDecoder = new GifDecoder();
                result = gifDecoder.read(path);
                sb.append("result");
                sb.append(":");
                sb.append(result);
                sb.append("\n");

                GifImage gifImage = null;
                if (result == GifDecoder.STATUS_OK) {
                    gifImage = new GifImage();

                    int count = gifDecoder.getFrameCount();
                    sb.append("count");
                    sb.append(":");
                    sb.append(count);
                    sb.append("\n");
                    int duration = 0;
                    Bitmap[] bitmaps = new Bitmap[count];
                    long[] delays = new long[count];
                    for (int i = 0; i < count; i++) {
                        Bitmap bitmap = gifDecoder.getFrame(i);
                        bitmaps[i] = bitmap;
                        delays[i] = gifDecoder.getDelay(i);
                        duration += delays[i];
                        if (i < 10) {
                            Utils.saveMyBitmap(bitmap, location + "", "" + 0 + i);
                        } else {
                            Utils.saveMyBitmap(bitmap, location + "", "" + i);
                        }
                    }

                    gifImage.setPlayCount(count);
                    gifImage.setFrameBitmaps(bitmaps);
                    gifImage.setFrameDelays(delays);
                    gifImage.setDuration(duration);
                    gifImage.setLocation(location);
                    sb.append("duration");
                    sb.append(":");
                    sb.append(duration);
                    sb.append("\n");
                }
                return gifImage;
            }

            @Override
            protected void onPostExecute(GifImage s) {
                super.onPostExecute(s);
                if(onGetGif != null){
                    onGetGif.onLoaded(s!=null, s);
                }
            }
        };
        mRunCommandAsyncTask.execute();
    }

    public static void analyzeGif(final int location, final String path, final OnGetGif onGetGif) {
        if(TextUtils.isEmpty(path) || !new File(path).exists()){
            if(onGetGif != null){
                onGetGif.onLoaded(false, null);
            }
            return;
        }
        AsyncTask<Void, Void, GifImage> mRunCommandAsyncTask = new AsyncTask<Void, Void, GifImage>() {
            private StringBuffer sb = new StringBuffer();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected GifImage doInBackground(Void... strings) {
                int result = GifDecoder.STATUS_OPEN_ERROR;
                GifDecoder gifDecoder = new GifDecoder();
                try {
                    result = gifDecoder.read(new FileInputStream(new File(path)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                sb.append("result");
                sb.append(":");
                sb.append(result);
                sb.append("\n");

                GifImage gifImage = null;
                if (result == GifDecoder.STATUS_OK) {
                    gifImage = new GifImage();

                    int count = gifDecoder.getFrameCount();
                    sb.append("count");
                    sb.append(":");
                    sb.append(count);
                    sb.append("\n");
                    int duration = 0;
                    Bitmap[] bitmaps = new Bitmap[count];
                    long[] delays = new long[count];
                    for (int i = 0; i < count; i++) {
                        Bitmap bitmap = gifDecoder.getFrame(i);
                        bitmaps[i] = bitmap;
                        delays[i] = gifDecoder.getDelay(i);
                        duration += delays[i];
                        if (i < 10) {
                            Utils.saveMyBitmap(bitmap, location + "", "" + 0 + i);
                        } else {
                            Utils.saveMyBitmap(bitmap, location + "", "" + i);
                        }
                    }

                    gifImage.setPlayCount(count);
                    gifImage.setFrameBitmaps(bitmaps);
                    gifImage.setFrameDelays(delays);
                    gifImage.setDuration(duration);
                    gifImage.setLocation(location);
                    sb.append("duration");
                    sb.append(":");
                    sb.append(duration);
                    sb.append("\n");
                }
                return gifImage;
            }

            @Override
            protected void onPostExecute(GifImage s) {
                super.onPostExecute(s);
                if(onGetGif != null){
                    onGetGif.onLoaded(s!=null, s);
                }
            }
        };
        mRunCommandAsyncTask.execute();
    }

    public static void saveMyBitmap(Bitmap mBitmap, String dirName, String bitName)  {
//        String dir = XLGifToVideoManager.OUTPUT_DIR + dirName + "/";
//        File f = new File(dir);
//        f.mkdirs();
//        f = new File(dir +bitName + ".png");
//        FileOutputStream fOut = null;
//        try {
//            fOut = new FileOutputStream(f);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
//        try {
//            fOut.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            fOut.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public static boolean checkSDPath() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}
