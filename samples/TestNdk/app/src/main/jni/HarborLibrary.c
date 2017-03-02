#include <jni.h>

JNIEXPORT jstring JNICALL
Java_harbor_com_testndk_MyNdk_getString(JNIEnv *env, jobject instance) {

    // TODO


    return (*env)->NewStringUTF(env, "test Harbor 测试。。");
}