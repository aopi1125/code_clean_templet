package cn.xlvip.ffmpeg.library.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import cn.xlvip.ffmpeg.library.GifContentHolder;


/**
 * Created by chenwenfeng on 2017/5/30.
 */

public class GlESDrawBitmap {
    private static final String NORMAL_1X1 = "1X1";//1:1
    private static final String DIFFERENT_4X3 = "4X3";//4:3
    private static final String DIFFERENT_3X4 = "3X4";//3:4

    private static final float ONE = 1;
    private static final float THREE_QUARTERS = 0.75f;

    private int mIndex = 0;
    private GifContentHolder mGifContentHolder;
    private int mTextureCount = 4;

    private GlESDrawBitmap(){
        super();
    }

    GlESDrawBitmap(GifContentHolder gif, int width, int height){
        super();
        this.mGifContentHolder = gif;
        this.mWidth = width;
        this.mHeight = height;
        init();
    }

    private void init(){
        if(mWidth == mHeight){
            mShowRatio = NORMAL_1X1;
        }else if(mWidth > mHeight){
            mShowRatio = DIFFERENT_4X3;
            VERTEX1[1] = THREE_QUARTERS;
            VERTEX1[4] = THREE_QUARTERS;
            VERTEX2[1] = THREE_QUARTERS;
            VERTEX2[4] = THREE_QUARTERS;
            VERTEX3[7] = -THREE_QUARTERS;
            VERTEX3[10] = -THREE_QUARTERS;
            VERTEX4[7] = -THREE_QUARTERS;
            VERTEX4[10] = -THREE_QUARTERS;
        }else{
            mShowRatio = DIFFERENT_3X4;
            VERTEX1[3] = -THREE_QUARTERS;
            VERTEX1[6] = -THREE_QUARTERS;
            VERTEX2[0] = THREE_QUARTERS;
            VERTEX2[9] = THREE_QUARTERS;
            VERTEX3[0] = THREE_QUARTERS;
            VERTEX3[9] = THREE_QUARTERS;
            VERTEX4[3] = -THREE_QUARTERS;
            VERTEX4[6] = -THREE_QUARTERS;
        }
        mTexNames = new int[mTextureCount];
    }

    private int mWidth;
    private int mHeight;
    private String mShowRatio = NORMAL_1X1;

//    private static final float[] VERTEX = {   // in counterclockwise order:
//            1, 1, 0,   // top right
//            -1, 1, 0,  // top left
//            -1, -1, 0, // bottom left
//            1, -1, 0,  // bottom right
//    };
    private static final float[] VERTEX1 = {   // in counterclockwise order:
            0, ONE,0,
            -ONE, ONE, 0,
            -ONE, 0, 0,
            0, 0, 0,
    };
    private static final float[] VERTEX2 = {   // in counterclockwise order:
            ONE, ONE,0,
            0, ONE, 0,
            0, 0, 0,
            ONE, 0, 0,
    };
    private static final float[] VERTEX4  = {   // in counterclockwise order:
            ONE, 0,0,
            0, 0, 0,
            0, -ONE, 0,
            ONE, -ONE, 0,
    };
    private static final float[] VERTEX3 = {   // in counterclockwise order:
            0, 0,0,
            -ONE, 0, 0,
            -ONE, -ONE, 0,
            0, -ONE, 0,
    };

    private static final short[] VERTEX_INDEX = { 0, 1, 2, 0, 2, 3 };
    private static final float[] UV_TEX_VERTEX = {   // in clockwise order:
            1, 0,  // bottom right
            0, 0,  // bottom left
            0, 1,  // top left
            1, 1,  // top right
    };
    private static final String VERTEX_SHADER = "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "attribute vec2 a_texCoord;" +
            "varying vec2 v_texCoord;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  v_texCoord = a_texCoord;" +
            "}";
    private static final String FRAGMENT_SHADER = "precision mediump float;" +
            "varying vec2 v_texCoord;" +
            "uniform sampler2D s_texture;" +
            "void main() {" +
            "  gl_FragColor = texture2D( s_texture, v_texCoord );" +
            "}";

    private final float[] mProjectionMatrix = new float[16];
    private final float[] mCameraMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];
    private int[] mTexNames;
    private FloatBuffer[] mVertexBuffers = new FloatBuffer[4];
//    private FloatBuffer  mVertexBuffer;
    private ShortBuffer mVertexIndexBuffer;
    private FloatBuffer mUvTexVertexBuffer;
    private int mProgram;
    private int mPositionHandle;
    private int mMatrixHandle;
    private int mTexCoordHandle;
    private int mTexSamplerHandle;

    static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    void initBuffer(){
        FloatBuffer mVertexBuffer1 = ByteBuffer.allocateDirect(VERTEX1.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX1);
        mVertexBuffer1.position(0);
        FloatBuffer mVertexBuffer2 = ByteBuffer.allocateDirect(VERTEX2.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX2);
        mVertexBuffer2.position(0);
        FloatBuffer mVertexBuffer3 = ByteBuffer.allocateDirect(VERTEX3.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX3);
        mVertexBuffer3.position(0);
        FloatBuffer mVertexBuffer4 = ByteBuffer.allocateDirect(VERTEX4.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX4);
        mVertexBuffer4.position(0);

        mVertexBuffers[0] = mVertexBuffer1;
        mVertexBuffers[1] = mVertexBuffer2;
        mVertexBuffers[2] = mVertexBuffer3;
        mVertexBuffers[3] = mVertexBuffer4;

//        mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4)
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer()
//                .put(VERTEX);
//        mVertexBuffer.position(0);

        mVertexIndexBuffer = ByteBuffer.allocateDirect(VERTEX_INDEX.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(VERTEX_INDEX);
        mVertexIndexBuffer.position(0);

        mUvTexVertexBuffer = ByteBuffer.allocateDirect(UV_TEX_VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(UV_TEX_VERTEX);
        mUvTexVertexBuffer.position(0);

        GLES20.glViewport(0, 0, mWidth, mHeight);
        mProgram = GLES20.glCreateProgram();
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
        mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mTexSamplerHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");

//        int[] mTexNames = new int[1];
//        GLES20.glGenTextures(1, mTexNames, 0);//only one texture
        IntBuffer textBuffer = IntBuffer.allocate(mTextureCount);
        GLES20.glGenTextures(mTextureCount, textBuffer);
        mTexNames = textBuffer.array();
        float ratio = (float) mHeight / mWidth;
        Matrix.frustumM(mProjectionMatrix, 0, -1, 1, -ratio, ratio, 3, 7);
        Matrix.setLookAtM(mCameraMatrix, 0, 0, 0, 3, 0, 0, 0, 0, 1, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mCameraMatrix, 0);
    }

    /**
     * 将4个gif合成一帧
     */
    void generateSurfaceFrame(boolean shouldOrder, int index){

        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        mIndex = index;

        int displayCount = 0;
        for(int i=0; i<mGifContentHolder.getGifs().length; i++){
            judgeAndDraw(shouldOrder, i, displayCount);
            displayCount += mGifContentHolder.getGifs()[i].getPlayCount();
        }
//        int count1 =  mGifContentHolder.getGifs()[0].getPlayCount();
//        int count2 =  mGifContentHolder.getGifs()[1].getPlayCount();
//        int count3 =  mGifContentHolder.getGifs()[2].getPlayCount();
//        int count4 =  mGifContentHolder.getGifs()[3].getPlayCount();
//        //gif1
//        judgeAndDraw(shouldOrder, 0, 0);
//        //gif2
//        judgeAndDraw(shouldOrder, 1, count1);
//        //gif3
//        judgeAndDraw(shouldOrder, 2, count1 + count2 );
//        //gif4
//        judgeAndDraw(shouldOrder, 3, count1 + count2 + count3);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
    }

    private void judgeAndDraw(boolean shouldOrder, int goupid, int drawedCount){
        Bitmap bitmap;
        if(shouldOrder){
            if(mIndex < drawedCount){
                bitmap = mGifContentHolder.getGifs()[goupid].getBitmap(0);
            }else {
                int index = mIndex - drawedCount;
                if (index >= mGifContentHolder.getGifs()[goupid].getRealBitmapCount()) {
                    index = mGifContentHolder.getGifs()[goupid].getRealBitmapCount() - 1;
                }
                bitmap = mGifContentHolder.getGifs()[goupid].getBitmap(index);
            }
        }else{
            //循环播放从头开始
            int index = mIndex % mGifContentHolder.getGifs()[goupid].getRealBitmapCount();
            bitmap = mGifContentHolder.getGifs()[goupid].getBitmap(index);
        }
        if(bitmap != null && !bitmap.isRecycled()){
            drawBitmap(goupid, goupid, bitmap);
        }
    }

    /**
     * 绑定纹理、设置坐标点、绘制
     * @param index
     * @param bitmap
     */
    private void drawBitmap(int textureIndex, int index, Bitmap bitmap){
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexNames[textureIndex]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glUseProgram(mProgram);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0,
                mVertexBuffers[index]);
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0,
                mUvTexVertexBuffer);
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniform1i(mTexSamplerHandle, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, VERTEX_INDEX.length,
                GLES20.GL_UNSIGNED_SHORT, mVertexIndexBuffer);
    }

    void release(){
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE);
        GLES20.glDeleteTextures(mTexNames.length, mTexNames, 0);
    }
}
