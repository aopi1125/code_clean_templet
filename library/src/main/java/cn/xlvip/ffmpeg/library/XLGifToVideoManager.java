package cn.xlvip.ffmpeg.library;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.SparseIntArray;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.xlvip.ffmpeg.library.ext.Utils;
import cn.xlvip.ffmpeg.library.opengl.EncodeAndMuxToVideo;

/**
 * Created by chenwenfeng on 2017/5/26.
 */

public class XLGifToVideoManager {
    public interface OnGenVideoHandler{
        void onStart();
        /**
         * 进度返回
         * @param percent max:100
         */
        void onProgress(int percent);

        /**
         * @param success 是否成功
         * @param path 成功则返回本地路径
         * @param durtaion 成功时文件的总时长，单位毫秒
         */
        void onFinish(boolean success, String path, long durtaion);
    }

    public static final String OUTPUT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final boolean DebuggAble = BuildConfig.DEBUG;
    private static final int THRESHOLD_FRAME_OPTS1 = 150;//
    private static final int THRESHOLD_FRAME_OPTS2 = 250;//
    private static final int THRESHOLD_FRAME_OPTS3 = 500;//


    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
    private static final String FILE_SUFFIX = ".mp4";
    private static int sWidth, sHeight;

    public static final int GIF_COUNT = 4;// must be 4， 目前4个格子
    public static final int FRAME_RATE = 12;               // 12fps,Frame per Second
    public static final int FRAME_RATE_DOUBLE = 24;
    public static final int FRAME_RATE_CRITICAL = 18;
    public static Context sContext;

    public static void init(Context context){
        sContext = context.getApplicationContext();
        sWidth = sContext.getResources().getDisplayMetrics().widthPixels;
        sHeight = sContext.getResources().getDisplayMetrics().heightPixels;
        sWidth = Math.min(sHeight, sWidth) / 2;
        sHeight = sWidth;
    }

    public static String makeFilePath() {
        String videoPath = Environment.getExternalStorageDirectory() + "/video";
        File dir = new File(videoPath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        StringBuffer sb = new StringBuffer();
        sb.append(videoPath)
                .append(File.separator)
                .append("48fanhe_video_")
                .append(DATE_FORMAT.format(new Date()))
                .append(FILE_SUFFIX);
        return sb.toString();
    }

    public static String generateSaveFileName(){
        return "48fanhe_video_" + DATE_FORMAT.format(new Date()) + FILE_SUFFIX;
    }

    /**
     * 合成视频，耗时
     * @param gifCOntentHolder
     * @param file 输出视频文件目录
     * @param handler 相关回调
     */
    public static void genVideo(final GifContentHolder gifCOntentHolder, final String file, final OnGenVideoHandler handler){
        AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {

            long duration;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if(handler != null){
                    handler.onStart();
                }
            }

            @Override
            protected String doInBackground(Void... strings) {
//                String OUTPUT_DIR = GlobeContext.getDirectoryPath(DirType.video);
//                String tmp = OUTPUT_DIR + File.separator + "video_tmp" + FILE_SUFFIX;
//                File dir = new File(OUTPUT_DIR);
//                if(!dir.exists()){
//                    dir.mkdirs();
//                }
                try {
                    //gif to frames
                    Paint paint = new Paint();
                    paint.setAntiAlias(true);
                    paint.setFlags(Paint.ANTI_ALIAS_FLAG);
                    for(int i=0; i< gifCOntentHolder.getGifs().length; i++){
                        GifImage image = gifCOntentHolder.getGifs()[i];
                        Utils.fileToGifImage(image, gifCOntentHolder, paint);
                        if(handler != null) {
                            //进度不准
                            handler.onProgress((i + 1) * Utils.TIME_GROUPS[0] / gifCOntentHolder.getGifs().length);
                        }
                    }

                    int frameRate = XLGifToVideoManager.FRAME_RATE;

                    //for fps
                    if(gifCOntentHolder.getOrderType() == GifContentHolder.Order.SameTime){
                        frameRate = Utils.handleForSameFps(gifCOntentHolder, FRAME_RATE, FRAME_RATE_DOUBLE, FRAME_RATE_CRITICAL);
                    }

                    //frames to video
                    EncodeAndMuxToVideo encodeVideo = new EncodeAndMuxToVideo(frameRate, gifCOntentHolder.getWidthAndHeight().x * 2, gifCOntentHolder.getWidthAndHeight().y * 2);//两格
                    duration = encodeVideo.tryEncodeVideoToMp4(gifCOntentHolder, file, handler);//毫秒

                    gifCOntentHolder.release();

                    if(handler != null){
                        //进度不准
                        handler.onProgress(Utils.TIME_GROUPS[2]);
                    }

//                    FileUtil.notifySystemGallery(sContext, file);
                } catch (Exception e) {
                    return e.toString();
                }
                return "success";
            }

            @Override
            protected void onPostExecute(String s) {
                if(handler != null) {
                    handler.onFinish("success".equals(s), "success".equals(s) ? file : s, duration);
                }
            }
        };
        asyncTask.execute();
    }

    /**
     * 暂时处理帧过多问题
     * @param readyGifFrames
     * @return
     */
    public static Point fitFrameWidthAndHeight(SparseIntArray readyGifFrames) {
        if (readyGifFrames == null) {
            return new Point(sWidth, sHeight);
        }
        int num = 0;
        for (int i = 0; i < readyGifFrames.size(); i++) {
            num += readyGifFrames.get(i, 0);
        }
        Point point = new Point(sWidth, sHeight);
        if (num >= THRESHOLD_FRAME_OPTS1) {
            point.x = sWidth * 3 / 4;
            point.y = sHeight * 3 / 4;
        }
        if (num >= THRESHOLD_FRAME_OPTS2) {
            point.x = sWidth / 2;
            point.y = sHeight / 2;
        }
        if (num >= THRESHOLD_FRAME_OPTS3) {
            point.x = point.x * 3 / 4;
            point.y = point.y * 3 / 4;
        }
        return point;
    }

    public static boolean isNetworkUrl(String url){
        return url != null && url.startsWith("http");
    }

}
