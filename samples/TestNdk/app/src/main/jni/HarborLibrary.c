#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <time.h>
#include <errno.h>
#include <sys/mman.h>

#include <android/log.h>

#define LOG_TAG "mmap harbor test"
#define LOG_LEVEL 10
#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGE(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}


unsigned char *maped;
int mapSize = 8;
int fd;
char *fileName="/sdcard/harbor_test.mmap";

JNIEXPORT jstring JNICALL
Java_harbor_com_testndk_MyNdk_getString(JNIEnv *env, jobject instance) {
    // TODO
    return (*env)->NewStringUTF(env, "test Harbor 测试。。");
}


int fileMap() {
    //create anaymous map, not backed by any files
    if ((fd = open(fileName, O_RDWR|O_CREAT)) == -1) {
        perror("open: ");
        LOGI(10, "error open the file: %d", errno);
        exit(1);
    }
    if (ftruncate(fd, mapSize) == -1) {
        perror("ftruncate: ");
        LOGI(10, "error truncate the file: %d", errno);
        exit(1);
    }
    maped = mmap(0, mapSize, PROT_READ|PROT_WRITE, MAP_SHARED, fd, 0);
    if (maped == MAP_FAILED || maped == NULL) {
        perror("map error: ");
        return -1;
    } else {
        LOGI(10, "*****map successful");
    }
    return 0;
}

void unmapFile() {
    munmap(maped, mapSize);
    close(fd);
}


JNIEXPORT void JNICALL
Java_harbor_com_testndk_MemoryService_naUpdate(JNIEnv *env, jclass type) {
//    int i;
//    unsigned char x;
//    LOGI(10, "...naUpdate ...");
//    srand(time(NULL));
//
//    for(i = 0; i<mapSize; i++){
//        x = rand()%128;
//        LOGI(10, "update harbor: %u", x);
//        maped[i] = x;
//    }
    int i;
    unsigned char x;
    LOGI(10, "***********************naUpdate");
    srand(time(NULL));
    //readFile();
    for (i = 0; i < mapSize; ++i) {
        x = rand()%128;
        LOGI(10, "update: %u", x);
        maped[i] = "test" + x;
    }
}

JNIEXPORT void JNICALL
Java_harbor_com_testndk_MemoryService_naMap(JNIEnv *env, jclass type) {
//    if(shareMemMap() == -1){
//        LOGI(10, "mmap failded...");
//        return;
//    }

    if (fileMap() == -1) {
        LOGI(10, "mmap failed\n");
        return;
    }
}

JNIEXPORT void JNICALL
Java_harbor_com_testndk_MemoryService_naUnmap(JNIEnv *env, jclass type) {
//    unmapShareMem();

    unmapFile();
}

int shareMemMap(){
    maped = mmap(0, mapSize, PROT_READ|PROT_WRITE, MAP_ANON|MAP_SHARED, fd, 0);
    if(maped == MAP_FAILED || maped == NULL){
        perror("map error ");
        return -1;
    } else {
        LOGI(10, "..map success");
    }
    return 0;
}

void unmapShareMem(){
    munmap(maped, mapSize);
    close(fd);
}

JNIEXPORT jobject JNICALL
Java_harbor_com_testndk_MyNdk_naMap(JNIEnv *env, jclass type) {
//    jobject byteBuf;
//    unsigned char x;
//    int i;
//    //initFile();
//    if (shareMemMap() == -1) {
//        LOGI(10, "mmap failed\n");
//        return NULL;
//    }
//    byteBuf = (*env)->NewDirectByteBuffer(env, maped, mapSize);
//    if (byteBuf == NULL) {
//        perror("cannot create direct buffer: ");
//        return NULL;
//    } else {
//        LOGI(10, "direct buffer created\n");
//        return byteBuf;
//    }

    jobject byteBuf;
    unsigned char x;
    int i;
    //initFile();
    if (fileMap() == -1) {
        LOGI(10, "mmap failed\n");
        return NULL;
    }
    byteBuf = (*env)->NewDirectByteBuffer(env, maped, mapSize);
    if (byteBuf == NULL) {
        perror("cannot create direct buffer: ");
        return NULL;
    } else {
        LOGI(10, "direct buffer created\n");
        return byteBuf;
    }

}

JNIEXPORT void JNICALL
Java_harbor_com_testndk_MyNdk_naUnmap(JNIEnv *env, jclass type) {
//    unmapShareMem();
    unmapFile();
}