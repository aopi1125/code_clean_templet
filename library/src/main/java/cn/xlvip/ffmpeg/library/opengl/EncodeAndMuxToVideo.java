
/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.xlvip.ffmpeg.library.opengl;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.text.TextUtils;
import android.util.Log;


import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.xlvip.ffmpeg.library.GifContentHolder;
import cn.xlvip.ffmpeg.library.XLGifToVideoManager;
import cn.xlvip.ffmpeg.library.ext.MediaExtractorUtil;
import cn.xlvip.ffmpeg.library.ext.Utils;


//20131106: removed hard-coded "/sdcard"
//20131205: added alpha to EGLConfig

/**
 * Generate an MP4 file using OpenGL ES drawing commands.  Demonstrates the use of MediaMuxer
 * and MediaCodec with Surface input.
 * <p>
 * This uses various features first available in Android "Jellybean" 4.3 (API 18).  There is
 * no equivalent functionality in previous releases.
 * <p>
 * (This was derived from bits and pieces of CTS tests, and is packaged as such, but is not
 * currently part of CTS.)
 */
public final class EncodeAndMuxToVideo {
    private static final String TAG = "EncodeAndMuxTest";
    private static final boolean VERBOSE = XLGifToVideoManager.DebuggAble;           // lots of logging

    // where to put the output file (note: /sdcard requires WRITE_EXTERNAL_STORAGE permission)

    // parameters for the encoder
    private static final String MIME_TYPE = "video/avc";//"video/mp4v-es";    // H.264 Advanced Video Coding
    private static final int IFRAME_INTERVAL = 10;          // 10 seconds between I-frames
    private int NUM_FRAMES = 0;               // 5 seconds of video

    private int mFrameRate;               // 15fps
    private int mMinFrames;//视频最短时间
    private static final int MIN_SECONDS = 4;//视频最短4秒

    // RGB color values for generated frames
    private static final int TEST_R0 = 255;
    private static final int TEST_G0 = 255;
    private static final int TEST_B0 = 255;
    private static final int TEST_R1 = 255;
    private static final int TEST_G1 = 0;
    private static final int TEST_B1 = 0;

    // size of a frame, in pixels
    private int mWidth = -1;
    private int mHeight = -1;
    // bit rate, in bits per second
    private int mBitRate = -1;

    // encoder / muxer state
    private MediaCodec mEncoder;
    private CodecInputSurface mInputSurface;
    private MediaMuxer mMuxer;
    private int mTrackIndex;
    private boolean mMuxerStarted;
    private static final long ONE_BILLION = 1000000000;//1s

    // allocate one of these up front so we don't need to do it every time
    private MediaCodec.BufferInfo mBufferInfo;

    private static final int TIMEOUT_USEC = 10000;
    private static final int OUTPUT_AUDIO_SAMPLE_RATE_HZ = 44100; // Must match the input stream.
    private static final int OUTPUT_AUDIO_CHANNEL_COUNT = 2; // Must match the input stream.
    private static final String OUTPUT_AUDIO_MIME_TYPE = "audio/mp4a-latm"; // Advanced Audio Coding

    private String mOutputPath;
    private GifContentHolder mGifContentHolder;
    private GlESDrawBitmap mGlESDrawBitmap;
    private static final int FACTOR = 1000000; //1毫秒= 1000,000纳秒
    private int mTrackAudioIndex;
    private long mVideoTime;
    private MediaExtractorUtil mAudioExtractor;

    private EncodeAndMuxToVideo(){
        super();
        mWidth = 500;
        mHeight = 500;
        mFrameRate = XLGifToVideoManager.FRAME_RATE;
        mMinFrames = MIN_SECONDS * mFrameRate;
    }

    public EncodeAndMuxToVideo(int frameRate, int width, int height){
        mWidth = width;
        mHeight = height;//暂定1:1
        mFrameRate = frameRate;
        mMinFrames = MIN_SECONDS * mFrameRate;
    }

    /**
     * Tests encoding of AVC video from a Surface.  The output is saved as an MP4 file.
     * @return 返回毫秒
     */
    public long tryEncodeVideoToMp4(GifContentHolder gifContent, String outputFile, XLGifToVideoManager.OnGenVideoHandler handler) {
        // QVGA at 2Mbps
        mBitRate = 1200000;
        mGlESDrawBitmap = new GlESDrawBitmap(gifContent, mWidth, mHeight);
        mOutputPath = outputFile;
        mGifContentHolder = gifContent;
        NUM_FRAMES = gifContent.getFrameCounts();
        NUM_FRAMES = Math.max(NUM_FRAMES, mMinFrames);
        boolean shouldOrder = (mGifContentHolder.getOrderType()== GifContentHolder.Order.Squence);
        if (VERBOSE) Log.d(TAG, "NUM_FRAMES " + NUM_FRAMES + " shouldOrder=" + shouldOrder);

        mAudioExtractor = new MediaExtractorUtil(gifContent.getAudioPath());
        try {
            prepareEncoder(gifContent.getAudioPath());

            for (int i = 0; i < NUM_FRAMES; i++) {
                // Feed any pending encoder output into the muxer.
                drainEncoder(false);

                // Generate a new frame of input.
                mGlESDrawBitmap.generateSurfaceFrame(shouldOrder, i);

                long presentTime = computePresentationTimeNsec(shouldOrder, i);
                mInputSurface.setPresentationTime(presentTime);
                mVideoTime = presentTime;
                if (VERBOSE) Log.d(TAG, "computePresentationTimeNsec() i=" + i + "; " +presentTime);


                // Submit it to the encoder.  The eglSwapBuffers call will block if the input
                // is full, which would be bad if it stayed full until we dequeued an output
                // buffer (which we can't do, since we're stuck here).  So long as we fully drain
                // the encoder before supplying additional input, the system guarantees that we
                // can supply another frame without blocking.
                if (VERBOSE) Log.d(TAG, "sending frame " + i + " to encoder");
                mInputSurface.swapBuffers();

                if(handler != null && NUM_FRAMES > 0){
                    //进度不准
                    handler.onProgress(i * Utils.TIME_GROUPS[1] / NUM_FRAMES + Utils.TIME_GROUPS[0]);
                }
            }

            try {
                if(mTrackAudioIndex != -1){
                    mAudioExtractor.combineVide(mMuxer, mTrackAudioIndex, mVideoTime);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // send end-of-stream to encoder, and drain remaining output
            drainEncoder(true);
        } finally {
            // release encoder, muxer, and input Surface
            releaseEncoder();
        }

        // To test the result, open the file with MediaExtractor, and get the format.  Pass
        // that into the MediaCodec decoder configuration, along with a SurfaceTexture surface,
        // and examine the output with glReadPixels.

        return mVideoTime / FACTOR;
    }

    /**
     * Configures encoder and muxer state, and prepares the input Surface.
     */
    private void prepareEncoder(String audioPath) {
        mBufferInfo = new MediaCodec.BufferInfo();

        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, mFrameRate);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
        if (VERBOSE) Log.d(TAG, "format: " + format);

        // Create a MediaCodec encoder, and configure it with our format.  Get a Surface
        // we can use for input and wrap it with a class that handles the EGL work.
        //
        // If you want to have two EGL contexts -- one for display, one for recording --
        // you will likely want to defer instantiation of CodecInputSurface until after the
        // "display" EGL context is created, then modify the eglCreateContext call to
        // take eglGetCurrentContext() as the share_context argument.
        try {
            mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = new CodecInputSurface(mEncoder.createInputSurface());
        mInputSurface.makeCurrent();
        mEncoder.start();

        //my
//        initMyAudio(audioPath);

        // Output filename.  Ideally this would use Context.getFilesDir() rather than a
        // hard-coded output directory.
        String outputPath = new File(mOutputPath).toString();
        Log.d(TAG, "output file is " + outputPath);


        // Create a MediaMuxer.  We can't add the video track and start() the muxer here,
        // because our MediaFormat doesn't have the Magic Goodies.  These can only be
        // obtained from the encoder after it has started processing data.
        //
        // We're not actually interested in multiplexing audio.  We just want to convert
        // the raw H.264 elementary stream we get from MediaCodec into a .mp4 file.
        try {
            mMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException ioe) {
            throw new RuntimeException("MediaMuxer creation failed", ioe);
        }

        mTrackIndex = -1;
        mMuxerStarted = false;

        mGlESDrawBitmap.initBuffer();
    }

    MediaExtractor audioExtractor;
    MediaCodec audioDecoder, audioEncoder;
    MediaCodec.BufferInfo audioDecoderOutputBufferInfo = new MediaCodec.BufferInfo();
    MediaCodec.BufferInfo audioEncoderOutputBufferInfo = new MediaCodec.BufferInfo();
    MediaFormat encoderOutputAudioFormat = null;
    MediaFormat decoderOutputAudioFormat = null;
    // We will determine these once we have the output format.
    int outputAudioTrack = -1;
    // Whether things are done on the audio side.
    boolean audioExtractorDone = false;
    boolean audioDecoderDone = false;
    boolean audioEncoderDone = false;
    // The audio decoder output buffer to process, -1 if none.
    int pendingAudioDecoderOutputBufferIndex = -1;
    boolean muxing = false;
    int audioExtractedFrameCount = 0;
    int audioDecodedFrameCount = 0;
    int audioEncodedFrameCount = 0;

    private void initMyAudio(String audioPath){
        if(TextUtils.isEmpty(audioPath)){
            return;
        }
        try {
            audioExtractor = new MediaExtractor();
            MediaCodecInfo audioCodecInfo = selectCodec(OUTPUT_AUDIO_MIME_TYPE);
            audioExtractor.setDataSource(audioPath);
            int audioInputTrack = getAndSelectAudioTrackIndex(audioExtractor);
            MediaFormat inputFormat = audioExtractor.getTrackFormat(audioInputTrack);
            MediaFormat outputAudioFormat =
                    MediaFormat.createAudioFormat(
                            OUTPUT_AUDIO_MIME_TYPE, OUTPUT_AUDIO_SAMPLE_RATE_HZ,
                            OUTPUT_AUDIO_CHANNEL_COUNT);
            audioDecoder = MediaCodec.createDecoderByType(getMimeTypeFor(inputFormat));
            audioDecoder.configure(inputFormat, null, null, 0);
            audioDecoder.start();
//            audioEncoder = MediaCodec.createByCodecName(audioCodecInfo.getName());
//            audioEncoder.configure(outputAudioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//            audioEncoder.start();
            audioEncoder = MediaCodec.createDecoderByType(getMimeTypeFor(inputFormat));
            audioEncoder.configure(inputFormat, null, null, 0);
            audioEncoder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doExtractDecodeEditEncodeMux(){
        ByteBuffer[] audioDecoderInputBuffers = audioDecoder.getInputBuffers();
        ByteBuffer[] audioDecoderOutputBuffers = audioDecoder.getOutputBuffers();;
        ByteBuffer[] audioEncoderInputBuffers = audioEncoder.getInputBuffers();;
        ByteBuffer[] audioEncoderOutputBuffers = audioEncoder.getOutputBuffers();;
        if (!audioEncoderDone) {
            if (VERBOSE) {
                Log.d(TAG, String.format(
                        "loop: "
                                + "extracted:%d(done:%b) "
                                + "decoded:%d(done:%b) "
                                + "encoded:%d(done:%b) "
                                + "pending:%d} "
                                + "muxing:%b(V:%d,A:%d)",

                        audioExtractedFrameCount, audioExtractorDone,
                        audioDecodedFrameCount, audioDecoderDone,
                        audioEncodedFrameCount, audioEncoderDone,
                        pendingAudioDecoderOutputBufferIndex,
                        muxing, 0, outputAudioTrack));
            }
            // Extract audio from file and feed to decoder.
            // Do not extract audio if we have determined the output format but we are not yet
            // ready to mux the frames.
            while ( !audioExtractorDone
                    && (encoderOutputAudioFormat == null || muxing)) {
                int decoderInputBufferIndex = audioDecoder.dequeueInputBuffer(TIMEOUT_USEC);
                if (decoderInputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (VERBOSE) Log.d(TAG, "no audio decoder input buffer");
                    break;
                }
                if (VERBOSE) {
                    Log.d(TAG, "audio decoder: returned input buffer: " + decoderInputBufferIndex);
                }
                ByteBuffer decoderInputBuffer = audioDecoderInputBuffers[decoderInputBufferIndex];
                int size = audioExtractor.readSampleData(decoderInputBuffer, 0);
                long presentationTime = audioExtractor.getSampleTime();
                if (VERBOSE) {
                    Log.d(TAG, "audio extractor: returned buffer of size " + size);
                    Log.d(TAG, "audio extractor: returned buffer for time " + presentationTime);
                }
                if (size >= 0) {
                    audioDecoder.queueInputBuffer(
                            decoderInputBufferIndex,
                            0,
                            size,
                            presentationTime,
                            audioExtractor.getSampleFlags());
                }
                audioExtractorDone = !audioExtractor.advance();
                if (audioExtractorDone) {
                    if (VERBOSE) Log.d(TAG, "audio extractor: EOS");
                    audioDecoder.queueInputBuffer(
                            decoderInputBufferIndex,
                            0,
                            0,
                            0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                }
                audioExtractedFrameCount++;
                // We extracted a frame, let's try something else next.
                break;
            }
            // Poll output frames from the audio decoder.
            // Do not poll if we already have a pending buffer to feed to the encoder.
            while (!audioDecoderDone && pendingAudioDecoderOutputBufferIndex == -1
                    && (encoderOutputAudioFormat == null || muxing)) {
                int decoderOutputBufferIndex =
                        audioDecoder.dequeueOutputBuffer(
                                audioDecoderOutputBufferInfo, TIMEOUT_USEC);
                if (decoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (VERBOSE) Log.d(TAG, "no audio decoder output buffer");
                    break;
                }
                if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    if (VERBOSE) Log.d(TAG, "audio decoder: output buffers changed");
                    audioDecoderOutputBuffers = audioDecoder.getOutputBuffers();
                    break;
                }
                if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    decoderOutputAudioFormat = audioDecoder.getOutputFormat();
                    if (VERBOSE) {
                        Log.d(TAG, "audio decoder: output format changed: "
                                + decoderOutputAudioFormat);
                    }
                    break;
                }
                if (VERBOSE) {
                    Log.d(TAG, "audio decoder: returned output buffer: "
                            + decoderOutputBufferIndex);
                }
                if (VERBOSE) {
                    Log.d(TAG, "audio decoder: returned buffer of size "
                            + audioDecoderOutputBufferInfo.size);
                }
                ByteBuffer decoderOutputBuffer =
                        audioDecoderOutputBuffers[decoderOutputBufferIndex];
                if ((audioDecoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG)
                        != 0) {
                    if (VERBOSE) Log.d(TAG, "audio decoder: codec config buffer");
                    audioDecoder.releaseOutputBuffer(decoderOutputBufferIndex, false);
                    break;
                }
                if (VERBOSE) {
                    Log.d(TAG, "audio decoder: returned buffer for time "
                            + audioDecoderOutputBufferInfo.presentationTimeUs);
                }
                if (VERBOSE) {
                    Log.d(TAG, "audio decoder: output buffer is now pending: "
                            + pendingAudioDecoderOutputBufferIndex);
                }
                pendingAudioDecoderOutputBufferIndex = decoderOutputBufferIndex;
                audioDecodedFrameCount++;
                // We extracted a pending frame, let's try something else next.
                break;
            }
            // Feed the pending decoded audio buffer to the audio encoder.
            while (pendingAudioDecoderOutputBufferIndex != -1) {
                if (VERBOSE) {
                    Log.d(TAG, "audio decoder: attempting to process pending buffer: "
                            + pendingAudioDecoderOutputBufferIndex);
                }
                int encoderInputBufferIndex = audioEncoder.dequeueInputBuffer(TIMEOUT_USEC);
                if (encoderInputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (VERBOSE) Log.d(TAG, "no audio encoder input buffer");
                    break;
                }
                if (VERBOSE) {
                    Log.d(TAG, "audio encoder: returned input buffer: " + encoderInputBufferIndex);
                }
                ByteBuffer encoderInputBuffer = audioEncoderInputBuffers[encoderInputBufferIndex];
                int size = audioDecoderOutputBufferInfo.size;
                long presentationTime = audioDecoderOutputBufferInfo.presentationTimeUs;
                if (VERBOSE) {
                    Log.d(TAG, "audio decoder: processing pending buffer: "
                            + pendingAudioDecoderOutputBufferIndex);
                }
                if (VERBOSE) {
                    Log.d(TAG, "audio decoder: pending buffer of size " + size);
                    Log.d(TAG, "audio decoder: pending buffer for time " + presentationTime);
                }
                if (size >= 0) {
                    ByteBuffer decoderOutputBuffer =
                            audioDecoderOutputBuffers[pendingAudioDecoderOutputBufferIndex]
                                    .duplicate();
                    decoderOutputBuffer.position(audioDecoderOutputBufferInfo.offset);
                    decoderOutputBuffer.limit(audioDecoderOutputBufferInfo.offset + size);
                    encoderInputBuffer.position(0);
                    encoderInputBuffer.put(decoderOutputBuffer);
                    audioEncoder.queueInputBuffer(
                            encoderInputBufferIndex,
                            0,
                            size,
                            presentationTime,
                            audioDecoderOutputBufferInfo.flags);
                }
                audioDecoder.releaseOutputBuffer(pendingAudioDecoderOutputBufferIndex, false);
                pendingAudioDecoderOutputBufferIndex = -1;
                if ((audioDecoderOutputBufferInfo.flags
                        & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (VERBOSE) Log.d(TAG, "audio decoder: EOS");
                    audioDecoderDone = true;
                }
                // We enqueued a pending frame, let's try something else next.
                break;
            }
            // Poll frames from the audio encoder and send them to the muxer.
            while (!audioEncoderDone
                    && (encoderOutputAudioFormat == null || muxing)) {
                int encoderOutputBufferIndex = audioEncoder.dequeueOutputBuffer(
                        audioEncoderOutputBufferInfo, TIMEOUT_USEC);
                if (encoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (VERBOSE) Log.d(TAG, "no audio encoder output buffer");
                    break;
                }
                if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    if (VERBOSE) Log.d(TAG, "audio encoder: output buffers changed");
                    audioEncoderOutputBuffers = audioEncoder.getOutputBuffers();
                    break;
                }
                if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    if (VERBOSE) Log.d(TAG, "audio encoder: output format changed");
                    if (outputAudioTrack >= 0) {
                        Log.d(TAG, "audio encoder changed its output format again?");
                    }
                    encoderOutputAudioFormat = audioEncoder.getOutputFormat();
                    break;
                }
                if (VERBOSE) {
                    Log.d(TAG, "audio encoder: returned output buffer: "
                            + encoderOutputBufferIndex);
                    Log.d(TAG, "audio encoder: returned buffer of size "
                            + audioEncoderOutputBufferInfo.size);
                }
                ByteBuffer encoderOutputBuffer =
                        audioEncoderOutputBuffers[encoderOutputBufferIndex];
                if ((audioEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG)
                        != 0) {
                    if (VERBOSE) Log.d(TAG, "audio encoder: codec config buffer");
                    // Simply ignore codec config buffers.
                    audioEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
                    break;
                }
                if (VERBOSE) {
                    Log.d(TAG, "audio encoder: returned buffer for time "
                            + audioEncoderOutputBufferInfo.presentationTimeUs);
                }
                if (audioEncoderOutputBufferInfo.size != 0) {
                    mMuxer.writeSampleData(
                            outputAudioTrack, encoderOutputBuffer, audioEncoderOutputBufferInfo);
                }
                if ((audioEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        != 0) {
                    if (VERBOSE) Log.d(TAG, "audio encoder: EOS");
                    audioEncoderDone = true;
                }
                audioEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
                audioEncodedFrameCount++;
                // We enqueued an encoded frame, let's try something else next.
                break;
            }
            if (!muxing && (encoderOutputAudioFormat != null)) {
                Log.d(TAG, "muxer: adding audio track.");
                outputAudioTrack = mMuxer.addTrack(encoderOutputAudioFormat);
                Log.d(TAG, "muxer: starting");
                muxing = true;
            }
        }
    }

    private int getAndSelectAudioTrackIndex(MediaExtractor extractor) {
        for (int index = 0; index < extractor.getTrackCount(); ++index) {
            if (VERBOSE) {
                Log.d(TAG, "format for track " + index + " is "
                        + getMimeTypeFor(extractor.getTrackFormat(index)));
            }
            if (isAudioFormat(extractor.getTrackFormat(index))) {
                extractor.selectTrack(index);
                return index;
            }
        }
        return -1;
    }
    private static String getMimeTypeFor(MediaFormat format) {
        return format.getString(MediaFormat.KEY_MIME);
    }
    private static boolean isAudioFormat(MediaFormat format) {
        return getMimeTypeFor(format).startsWith("audio/");
    }
    /**
     * Returns the first codec capable of encoding the specified MIME type, or null if no match was
     * found.
     */
    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    /**
     * Releases encoder resources.  May be called after partial / failed initialization.
     */
    private void releaseEncoder() {
        if (VERBOSE) Log.d(TAG, "releasing encoder objects");
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
        if (mInputSurface != null) {
            mInputSurface.release();
            mInputSurface = null;
        }
        if (mMuxer != null) {
            mMuxer.stop();
            mMuxer.release();
            mMuxer = null;
        }
        if(mGlESDrawBitmap != null){
            mGlESDrawBitmap.release();
        }
    }

    /**
     * Extracts all pending data from the encoder.
     * <p>
     * If endOfStream is not set, this returns when there is no more data to drain.  If it
     * is set, we send EOS to the encoder, and then iterate until we see EOS on the output.
     * Calling this with endOfStream set should be done once, right before stopping the muxer.
     */
    private void drainEncoder(boolean endOfStream) {
        if (VERBOSE) Log.d(TAG, "drainEncoder(" + endOfStream + ")");

        if (endOfStream) {
            if (VERBOSE) Log.d(TAG, "sending EOS to encoder");
            mEncoder.signalEndOfInputStream();
        }

        ByteBuffer[] encoderOutputBuffers = mEncoder.getOutputBuffers();

        //encode audio
//        doExtractDecodeEditEncodeMux();

        while (true) {
            int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    break;      // out of while
                } else {
                    if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = mEncoder.getOutputFormat();
                Log.d(TAG, "encoder output format changed: " + newFormat);

                // now that we have the Magic Goodies, start the muxer
                mTrackIndex = mMuxer.addTrack(newFormat);
                mTrackAudioIndex = mAudioExtractor.trackAudio(mMuxer);
                mMuxer.start();
                mMuxerStarted = true;
            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }

                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);

                    mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
                    if (VERBOSE) Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer");
                }

                mEncoder.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        if (VERBOSE) Log.d(TAG, "end of stream reached");
                    }
                    break;      // out of while
                }
            }
        }
    }


    long mNumPresentationTime = 0l;//显示到当前帧总时长
    int displayCount = 0;
    long displayDuration = 0;
    /**
     * Generates the presentation time for frame N, in nanoseconds.
     */
    private long computePresentationTimeNsec(boolean shouldOrder, int frameIndex) {
        if (shouldOrder) {
            mNumPresentationTime = 0l;//显示到当前帧总时长
            displayCount = 0;
            displayDuration = 0;
            boolean isWithin = false;
            for(int i=0; i<mGifContentHolder.getGifs().length; i++){
                if(frameIndex < displayCount + mGifContentHolder.getGifs()[i].getPlayCount()){
                    mNumPresentationTime = mGifContentHolder.getGifs()[i].getSumDelays(frameIndex - displayCount);
                    mNumPresentationTime += displayDuration;
                    isWithin = true;
                    break;
                }
                displayCount += mGifContentHolder.getGifs()[i].getPlayCount();
                displayDuration += mGifContentHolder.getGifs()[i].getDuration();
            }

//            int count1 =  mGifContentHolder.getGifs()[0].getPlayCount();
//            int count2 =  mGifContentHolder.getGifs()[1].getPlayCount();
//            int count3 =  mGifContentHolder.getGifs()[2].getPlayCount();
//            int count4 =  mGifContentHolder.getGifs()[3].getPlayCount();
//            long mNumPresentationTime = 0L;//显示到当前帧总时长
//            if (frameIndex < count1) {
//                //第1个图
//                mNumPresentationTime = mGifContentHolder.getGifs()[0].getSumDelays(frameIndex);
//            } else if (frameIndex < count1 + count2) {
//                //第2个图
//                mNumPresentationTime = mGifContentHolder.getGifs()[0].getDuration()
//                        + mGifContentHolder.getGifs()[1].getSumDelays(frameIndex-count1);
//            } else if (frameIndex < count1 + count2 + count3) {
//                //第3个图
//                mNumPresentationTime = mGifContentHolder.getGifs()[0].getDuration()
//                        + mGifContentHolder.getGifs()[1].getDuration()
//                        + mGifContentHolder.getGifs()[2].getSumDelays(frameIndex-count2-count1);
//            } else {
//                //第4个图
//                mNumPresentationTime = mGifContentHolder.getGifs()[0].getDuration()
//                        + mGifContentHolder.getGifs()[1].getDuration()
//                        + mGifContentHolder.getGifs()[2].getDuration()
//                        + mGifContentHolder.getGifs()[3].getSumDelays(frameIndex-count3-count2-count1);
//            }
            if(isWithin){
                return mNumPresentationTime * FACTOR;
            }else{
                return displayDuration * FACTOR + (frameIndex - displayCount) * ONE_BILLION / mFrameRate;
            }
        } else {
            return frameIndex * ONE_BILLION / mFrameRate;
        }
    }


//    /**
//     * Generates the presentation time for frame N, in nanoseconds.
//     */
//    private static long computePresentationTimeNsec(int frameIndex) {
//        final long ONE_BILLION = 1000000000;//1s
//        return frameIndex * ONE_BILLION / mFrameRate;
//    }

//    /**
//     * Generates a frame of data using GL commands.  We have an 8-frame animation
//     * sequence that wraps around.  It looks like this:
//     * <pre>
//     *   0 1 2 3
//     *   7 6 5 4
//     * </pre>
//     * We draw one of the eight rectangles and leave the rest set to the clear color.
//     */
//    private void generateSurfaceFrame(int frameIndex) {
//        frameIndex %= 8;
//
//        int startX, startY;
//        if (frameIndex < 4) {
//            // (0,0) is bottom-left in GL
//            startX = frameIndex * (mWidth / 4);
//            startY = mHeight / 2;
//        } else {
//            startX = (7 - frameIndex) * (mWidth / 4);
//            startY = 0;
//        }
//
//        GLES20.glClearColor(TEST_R0 / 255.0f, TEST_G0 / 255.0f, TEST_B0 / 255.0f, 1.0f);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//
//        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
//        GLES20.glScissor(startX, startY, mWidth / 4, mHeight / 2);
//        GLES20.glClearColor(TEST_R1 / 255.0f, TEST_G1 / 255.0f, TEST_B1 / 255.0f, 1.0f);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
//    }



}
