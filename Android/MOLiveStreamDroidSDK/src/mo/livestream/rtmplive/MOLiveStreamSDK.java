package mo.livestream.rtmplive;

import android.util.Log;

public class MOLiveStreamSDK {

	static {
		try {
			System.loadLibrary("yuvhelper");
			System.loadLibrary("openh264");
			System.loadLibrary("MOLiveStreamSDK");
		} catch (UnsatisfiedLinkError ule) {
			System.err.println("WARNING: Could not load library!");
		}
	}

	private static String TAG = "MOLiveStreamSDK";

	private int IsSupportAVC = 0; //<< if support hw encode
	private byte[] mYuvOutData = null; //<< convert output data
	private byte[] mVideoEncData = null; //<< hw encode output data

	private int mMediaLiveStatus = 0; //<< media live status(0:stop,1:start)
	private MOLiveStreamCallBack mPusherCallback = null; //<< media pusher callback

	public native int InitPublisher();

	public native int DeinitPublisher();

	// set server url
	public native void SetServerUrl(String url);

	// set video encode param
	public native int SetVideoEncode(int width, int height, int fps, int bitrate);

	// set audio encode param
	public native int SetAudioEncode(int sample_rate, int channel);

	// connect to server
	public native int ConnectToServer();

	// disconnect
	public native int Disconnect();

	// start live
	public native int StartLive(int enableVideo, int enableAudio);

	// stop live
	public native int StopLive();

	// return live status
	public native int GetLiveStatus();

	// audio data callback
	public native int OnCaptureAudioData(byte[] data, int len);

	// video data callback
	private native int OnCaptureVideoData(byte[] data, int len, int isFront,
			int isHWEncode/* hw encode */);

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
		int ret = StartLive(enableVideo, enableAudio);
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

	public int OnCaptureVideoFrame(byte[] data, int len, int isFront) 
	{
		if(mMediaLiveStatus <= 0)
			return 0;
		
		Log.d(TAG, "use sw encoder");
		OnCaptureVideoData(data, len, isFront, 0);
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
