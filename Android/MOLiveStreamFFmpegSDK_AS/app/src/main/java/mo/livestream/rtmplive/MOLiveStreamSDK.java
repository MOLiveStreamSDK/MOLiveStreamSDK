package mo.livestream.rtmplive;

import android.util.Log;

public class MOLiveStreamSDK {

	static {
		try {
			System.loadLibrary("MOLiveStreamSDK");
		} catch (UnsatisfiedLinkError ule) {
			System.err.println("WARNING: Could not load library!");
		}
	}

	private static String TAG = "MOLiveStreamSDK";

	private int mMediaLiveStatus = 0; //<< media live status(0:stop,1:start)
	
	private MOLiveStreamCallBack mPusherCallback = null; //<< media pusher callback

	public native int InitPublisher();

	public native int DeinitPublisher();

	// set server url
	public native void SetServerUrl(String url);

	// set video encode param
	public native int SetVideoEncode(int width, int height, int fps, int bitrate);

	// set audio encode param
	public native int SetAudioEncode(int sample_rate, int channels);

	// start live
	public native int StartLive();

	// stop live
	public native int StopLive();

	// audio data callback
	public native int OnCaptureAudioData(byte[] data, long ts);

	// video data callback
	private native int OnCaptureVideoData(byte[] data, int width, int height, long ts ,int isFront);

	// 转换YUV格式以及旋转
	private native int ConvertYUVData(byte[] srcData, byte[] dstData, int isFront);

	public int InitMediaPublisher(MOLiveStreamCallBack callback) {
		this.mPusherCallback = callback;
		return InitPublisher();
	}

	public int DeinitMediaPublisher() {
		return DeinitPublisher();
	}

	public int StartMediaLive(int enableVideo, int enableAudio) {
		int ret = StartLive();
		if (ret < 0)
			return ret;

		mMediaLiveStatus = 1;

		return 0;
	}

	public int StopMediaLive() {
		mMediaLiveStatus = 0;
		return StopLive();
	}

	public int GetMediaLiveStatus() {
		return mMediaLiveStatus;
	}

	//
	public int SetVideoEncoder(int width, int height, int fps, int bitrate, boolean enable_hw) {
		return SetVideoEncode(width, height, fps, bitrate);
	}

	public int OnCaptureVideoFrame(byte[] data, int width, int height, long ts, int isFront) 
	{
		if(mMediaLiveStatus <= 0)
			return 0;
		
		Log.d(TAG, "use sw encoder");
		OnCaptureVideoData(data, width, height, ts, isFront);
		return 0;
	}

	public void onNativeConnecting() {
		Log.d(TAG, "onNativeConnecting");
		this.mPusherCallback.onConnecting();
	}

	public void onNativeConnected() {
		Log.d(TAG, "onNativeConnected");
		this.mPusherCallback.onConnected();
	}

	public void onNativeDisconnect() {
		Log.d(TAG, "onNativeDisconnect");
		this.mPusherCallback.onDisconnect();
	}

	public void onNativeConnectError(int err) {
		Log.d(TAG, "onNativeConnectError:" + err);
		this.mPusherCallback.onConnectError( err);
	}
	
}
