#include <string.h>
#include <time.h>

#include "mo_livestream_rtmplive_MOLiveStreamSDK.h"
#include "LiveStreamFFmpeg.h"

#include <android/log.h>

#define LOG_NETWORK(...) __android_log_print(ANDROID_LOG_DEBUG, "MOLiveNetwork", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "MOLiveStreamSDK", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "MOLiveStreamSDK", __VA_ARGS__)

#define GET_ARRAY_LEN(array,len){len = (sizeof(array) / sizeof(array[0]));}

LiveStreamFFmpeg *m_LiveStream = NULL;

JavaVM *g_jvm = NULL; 
jobject g_obj = NULL; 

void OnConnectCallBack(int err)
{
	 JNIEnv *env; 

     //Attach主线程 
     if( g_jvm->AttachCurrentThread(&env, NULL) != JNI_OK) 
     { 
         LOGD("%s: AttachCurrentThread() failed", __FUNCTION__); 
         return; 
     } 

	jclass cls = env->GetObjectClass(g_obj);
	jmethodID onConnetingCB = env->GetMethodID(cls,"onNativeConnecting","()V");
	jmethodID onConnetErrCB = env->GetMethodID(cls,"onNativeConnectError","(I)V");
	jmethodID onConnetedCB = env->GetMethodID(cls,"onNativeConnected","()V");
	
	if (err<0)
	{
	   LOGD("JNI_ConnectToServer err");
	   env->CallVoidMethod(g_obj, onConnetErrCB, err);
	}
    else
	{
	   LOGD("JNI_ConnectToServer err");
	   env->CallVoidMethod(g_obj, onConnetedCB);
	}

	//Detach主线程 
     if(g_jvm->DetachCurrentThread() != JNI_OK) 
     { 
         LOGD("%s: DetachCurrentThread() failed", __FUNCTION__); 
     } 
}

/*
 * Class:     mo_livestream_rtmplive_MOLiveStreamSDK
 * Method:    StreamInit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_mo_livestream_rtmplive_MOLiveStreamSDK_InitPublisher
  (JNIEnv *env, jobject obj){

	LOGD("---> StreamInit");

	m_LiveStream = new LiveStreamFFmpeg();
	m_LiveStream->Initialize();

	env->GetJavaVM(&g_jvm); 
    g_obj=env->NewGlobalRef(obj); 

	LOGD("<--- StreamInit");

	return 0;
}

/*
 * Class:     mo_livestream_rtmplive_MOLiveStreamSDK
 * Method:    StreamRelease
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_mo_livestream_rtmplive_MOLiveStreamSDK_DeinitPublisher
  (JNIEnv *env, jobject obj){

	LOGD("---> StreamRelease");

	if (m_LiveStream != NULL)
	{
		m_LiveStream->Destroy();
		delete m_LiveStream;
		m_LiveStream = NULL;
	}

	env->DeleteGlobalRef(g_obj);
	LOGD("<--- StreamRelease");

	return 0;
}

/*
 * Class:     mo_livestream_rtmplive_MOLiveStreamSDK
 * Method:    SetServerUrl
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT void JNICALL Java_mo_livestream_rtmplive_MOLiveStreamSDK_SetServerUrl
 (JNIEnv *env, jobject obj, jstring url)
{
	const char *server_url = env->GetStringUTFChars(url, 0);
	LOGD("SetServerUrl %s",server_url);

	m_LiveStream->setServerUrl((const char *)server_url);
}

/*
 * Class:     mo_livestream_rtmplive_MOLiveStreamSDK
 * Method:    SetVideoEncoder
 * Signature: (IIII)I
 */
JNIEXPORT jint JNICALL Java_mo_livestream_rtmplive_MOLiveStreamSDK_SetVideoEncode
  (JNIEnv *env, jobject obj, jint width, jint height, jint fps, jint bitrate){

	LOGD("SetVideoEncoder %dx%d, bitrate:%d fps:%d",width, height, bitrate, fps);
	return m_LiveStream->setVideoOption( width,  height,  bitrate,  fps);
}

/*
 * Class:     mo_livestream_rtmplive_MOLiveStreamSDK
 * Method:    SetAudioEncoder
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_mo_livestream_rtmplive_MOLiveStreamSDK_SetAudioEncode
  (JNIEnv *env, jobject obj, jint sample_rate, jint channels){

	LOGD("SetAudioEncoder samplerate:%d",sample_rate);
	return m_LiveStream->setAudioOption( sample_rate );
}

/*
 * Class:     mo_livestream_rtmplive_MOLiveStreamSDK
 * Method:    StartSessionLive
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_mo_livestream_rtmplive_MOLiveStreamSDK_StartLive
  (JNIEnv *env, jobject obj){
	LOGD("StartSessionLive ");
	return m_LiveStream->startSession();
}

/*
 * Class:     mo_livestream_rtmplive_MOLiveStreamSDK
 * Method:    StopSessionLive
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_mo_livestream_rtmplive_MOLiveStreamSDK_StopLive
   (JNIEnv *env, jobject obj){
	LOGD("StopSessionLive ");
	return m_LiveStream->stopSession();
}

/*
 * Class:     mo_livestream_rtmplive_MOLiveStreamSDK
 * Method:    OnPushVideo
 * Signature: ([BIIJ)V
 */
JNIEXPORT jint JNICALL Java_mo_livestream_rtmplive_MOLiveStreamSDK_OnCaptureVideoData
  (JNIEnv *env, jobject thiz, jbyteArray vdata, jint in_width, jint in_height, jlong ts, jint is_front){
	
	jint dataLength = env->GetArrayLength(vdata);
	jbyte* jBuffer = (jbyte*)malloc(dataLength * sizeof(jbyte));
	env->GetByteArrayRegion(vdata, 0, dataLength, jBuffer);
	
	int ret = m_LiveStream->OnCaputureVideo((unsigned char *) jBuffer,dataLength, in_width, in_height, ts, is_front);

	free(jBuffer);
	return ret;
}

/*
 * Class:     mo_livestream_rtmplive_MOLiveStreamSDK
 * Method:    OnPushAudio
 * Signature: ([BJ)I
 */
JNIEXPORT jint JNICALL Java_mo_livestream_rtmplive_MOLiveStreamSDK_OnCaptureAudioData
  (JNIEnv *env, jobject thiz, jbyteArray adata, jlong ts){

	jint dataLength = env->GetArrayLength( adata);
	jbyte* jBuffer = (jbyte*)malloc(dataLength * sizeof(jbyte));
	env->GetByteArrayRegion(adata, 0, dataLength, jBuffer);

	int ret = m_LiveStream->OnCaputureAudio((unsigned char *) jBuffer,dataLength, ts);
	
	free(jBuffer);
	return ret;
}
