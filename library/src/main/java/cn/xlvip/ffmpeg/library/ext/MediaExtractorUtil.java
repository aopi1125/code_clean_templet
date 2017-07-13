package cn.xlvip.ffmpeg.library.ext;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by chenwenfeng on 2017/5/26.
 * 音视频合成工具
 */

public class MediaExtractorUtil {

    private MediaExtractor mAudioExtractor;
    private int mAudioTrackIndex;
    private String mAudioPath;
	private boolean bWithADTS = false;


    public MediaExtractorUtil(String audio){
        super();
        mAudioPath = audio;
    }

    public int trackAudio(MediaMuxer mediaMuxer){
        if(TextUtils.isEmpty(mAudioPath)) return -1;

        try {
            mAudioExtractor = new MediaExtractor();
            mAudioExtractor.setDataSource(mAudioPath);
            MediaFormat audioFormat = null;
            mAudioTrackIndex = -1;
            int audioTrackCount = mAudioExtractor.getTrackCount();
            for (int i = 0; i < audioTrackCount; i++) {
                audioFormat = mAudioExtractor.getTrackFormat(i);
                String mimeType = audioFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("audio/")) {
                    mAudioTrackIndex = i;
                    break;
                }
            }
            mAudioExtractor.selectTrack(mAudioTrackIndex);
			if(audioFormat.containsKey(MediaFormat.KEY_IS_ADTS)){
                int is_adts = audioFormat.getInteger(MediaFormat.KEY_IS_ADTS);
                bWithADTS = (is_adts == 1);
            }
            int writeAudioTrackIndex = mediaMuxer.addTrack(audioFormat);
            return writeAudioTrackIndex;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     *
     * @param mediaMuxer 视频复用器
     * @param writeAudioTrackIndex mediaMuxer加入的音轨{@link #trackAudio(MediaMuxer)}
     * @param videoTime 无声视频总时长，单位纳秒。1微妙 = 1000纳秒
     * @throws Exception
     */
    public boolean combineVide(MediaMuxer mediaMuxer, int writeAudioTrackIndex, long videoTime) throws Exception {
        if(TextUtils.isEmpty(mAudioPath)) return false;

        try {
            videoTime = videoTime / 1000;
            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
            long sampleTime;//微秒
            {
                //音频
                mAudioExtractor.readSampleData(byteBuffer, 0);
                if (mAudioExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                    mAudioExtractor.advance();
                }
                mAudioExtractor.readSampleData(byteBuffer, 0);
                long secondTime = mAudioExtractor.getSampleTime();
                mAudioExtractor.advance();
                mAudioExtractor.readSampleData(byteBuffer, 0);
                long thirdTime = mAudioExtractor.getSampleTime();
                sampleTime = Math.abs(thirdTime - secondTime);
            }

            mAudioExtractor.unselectTrack(mAudioTrackIndex);
            mAudioExtractor.selectTrack(mAudioTrackIndex);

            int time = 0;
            while (true) {
                int readAudioSampleSize = mAudioExtractor.readSampleData(byteBuffer, 0);
                if (time >= videoTime) {
                    //视频太短结束，若太长则截取时间跟视频长度一致
                    break;
                }else{
                }
                if(readAudioSampleSize < 0){
                    mAudioExtractor.unselectTrack(mAudioTrackIndex);
                    mAudioExtractor.selectTrack(mAudioTrackIndex);
                    mAudioExtractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
                    continue;
                }
                
				audioBufferInfo.size = readAudioSampleSize - (bWithADTS ? 7 : 0);
                if(audioBufferInfo.size < 0){
                    audioBufferInfo.size = 0;
                }
                audioBufferInfo.presentationTimeUs += sampleTime;
                audioBufferInfo.offset = (bWithADTS ? 7 : 0);
                audioBufferInfo.flags = mAudioExtractor.getSampleFlags();
                mediaMuxer.writeSampleData(writeAudioTrackIndex, byteBuffer, audioBufferInfo);
                mAudioExtractor.advance();
                time += sampleTime;
            }
        } finally {
            mAudioExtractor.release();
        }
        return true;
    }

    public static boolean combineVideo(String video, String audio, String out) throws Exception {
        if (TextUtils.isEmpty(video) || !new File(video).exists()) {
            return false;
        }
        MediaExtractor videoExtractor = new MediaExtractor();
        videoExtractor.setDataSource(video);
        MediaFormat videoFormat = null;
        int videoTrackIndex = -1;
        int videoTrackCount = videoExtractor.getTrackCount();
        for (int i = 0; i < videoTrackCount; i++) {
            videoFormat = videoExtractor.getTrackFormat(i);
            String mimeType = videoFormat.getString(MediaFormat.KEY_MIME);
            if (mimeType.startsWith("video/")) {
                videoTrackIndex = i;
                break;
            }
        }

        MediaExtractor audioExtractor = new MediaExtractor();
        audioExtractor.setDataSource(audio);
        MediaFormat audioFormat = null;
        int audioTrackIndex = -1;
        int audioTrackCount = audioExtractor.getTrackCount();
        for (int i = 0; i < audioTrackCount; i++) {
            audioFormat = audioExtractor.getTrackFormat(i);
            String mimeType = audioFormat.getString(MediaFormat.KEY_MIME);
            if (mimeType.startsWith("audio/")) {
                audioTrackIndex = i;
                break;
            }
        }

        videoExtractor.selectTrack(videoTrackIndex);
        audioExtractor.selectTrack(audioTrackIndex);

        MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
        MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();

        MediaMuxer mediaMuxer = new MediaMuxer(out, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        int writeVideoTrackIndex = mediaMuxer.addTrack(videoFormat);
        int writeAudioTrackIndex = mediaMuxer.addTrack(audioFormat);
        mediaMuxer.start();
        ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
        long sampleTime = 0;
        {
            //视频
            videoExtractor.readSampleData(byteBuffer, 0);
            if (videoExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                videoExtractor.advance();
            }
            videoExtractor.readSampleData(byteBuffer, 0);
            long secondTime = videoExtractor.getSampleTime();
            videoExtractor.advance();
            videoExtractor.readSampleData(byteBuffer, 0);
            long thirdTime = videoExtractor.getSampleTime();
            sampleTime = Math.abs(thirdTime - secondTime);
        }
        videoExtractor.unselectTrack(videoTrackIndex);
        videoExtractor.selectTrack(videoTrackIndex);
        int longTime = 0;//统计视频总长度

        while (true) {
            int readVideoSampleSize = videoExtractor.readSampleData(byteBuffer, 0);
            if (readVideoSampleSize < 0) {
                break;
            }
            videoBufferInfo.size = readVideoSampleSize;
            videoBufferInfo.presentationTimeUs += sampleTime;
            videoBufferInfo.offset = 0;
            videoBufferInfo.flags = videoExtractor.getSampleFlags();
            mediaMuxer.writeSampleData(writeVideoTrackIndex, byteBuffer, videoBufferInfo);
            videoExtractor.advance();
            longTime += sampleTime;
        }

        {
            //音频
            audioExtractor.readSampleData(byteBuffer, 0);
            if (videoExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                videoExtractor.advance();
            }
            audioExtractor.readSampleData(byteBuffer, 0);
            long secondTime = audioExtractor.getSampleTime();
            audioExtractor.advance();
            audioExtractor.readSampleData(byteBuffer, 0);
            long thirdTime = audioExtractor.getSampleTime();
            sampleTime = Math.abs(thirdTime - secondTime);
        }

        audioExtractor.unselectTrack(audioTrackIndex);
        audioExtractor.selectTrack(audioTrackIndex);

        int time = 0;
        while (true) {
            int readAudioSampleSize = audioExtractor.readSampleData(byteBuffer, 0);
            if (time >= longTime) {
                //视频太短结束，若太长则截取时间跟视频长度一致
                break;
            }else{
            }
            if(readAudioSampleSize < 0){
                audioExtractor.unselectTrack(audioTrackIndex);
                audioExtractor.selectTrack(audioTrackIndex);
                audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
                continue;
            }
            audioBufferInfo.size = readAudioSampleSize;
            audioBufferInfo.presentationTimeUs += sampleTime;
            audioBufferInfo.offset = 0;
            audioBufferInfo.flags = videoExtractor.getSampleFlags();
            mediaMuxer.writeSampleData(writeAudioTrackIndex, byteBuffer, audioBufferInfo);
            audioExtractor.advance();
            time += sampleTime;
        }

        mediaMuxer.stop();
        mediaMuxer.release();
        videoExtractor.release();
        audioExtractor.release();
        return true;
    }
}
