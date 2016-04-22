package mo.livestream.rtmplive;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

public class MOliveStreamHelper{

	private static String TAG = "MediaLiveHelper";

	private MOLiveStreamSDK mPublisherHelper = null;
	private MOLiveStreamCameraHelper 	  mCameraHelper    = null;
	private MOLiveStreamAudioHelper 	  mAudioHeplper    = null;
	private SurfaceHolder 	  mSurfaceHolder   = null;

	private int mVideoWidth   = MOLiveStreamConstConfig.FORMAT_480P.WIDTH;
	private int mVideoHeight  = MOLiveStreamConstConfig.FORMAT_480P.HEIGHT;
	private int mVideoFps     = 15;
	private int mVideoBitrate = 800; // kbps

	private int mEnableAudio = 0;
	private int mMaxAudioReadbytes = 0;
	
	public int mMeidaLiveStatus    = 0; // 0:stop 1:running

	private static int mCameraFace = Camera.CameraInfo.CAMERA_FACING_FRONT;

	public void InitMeidaLiveHelper(MOLiveStreamCallBack callback) {

		Log.d(TAG, "---> initMeidaLiveHelper");

		mPublisherHelper = new MOLiveStreamSDK();
		mPublisherHelper.InitMediaPublisher(callback);

		mCameraHelper = new MOLiveStreamCameraHelper();
		mCameraHelper.setCameraDataCallBack(mPublisherHelper);

		mAudioHeplper = new MOLiveStreamAudioHelper();
		mAudioHeplper.setAudioDataCallBack(mPublisherHelper);

		Log.d(TAG, "<--- initMeidaLiveHelper");

	}

	public void SetServerUrl(String url) {
		if (mPublisherHelper != null) {
			mPublisherHelper.SetServerUrl(url);
		}
	}

	public void SetVideoOption(int width, int height , boolean enableHWCodec){

		switch (height) {
		case 240:
			mVideoWidth = MOLiveStreamConstConfig.FORMAT_240P.WIDTH;
			mVideoHeight = MOLiveStreamConstConfig.FORMAT_240P.HEIGHT;
			mVideoBitrate = MOLiveStreamConstConfig.VIDEO_BITRATE_92;
			break;
		case 360:
			mVideoWidth = MOLiveStreamConstConfig.FORMAT_360P.WIDTH;
			mVideoHeight = MOLiveStreamConstConfig.FORMAT_360P.HEIGHT;
			mVideoBitrate = MOLiveStreamConstConfig.VIDEO_BITRATE_512;
			break;
		case 480:
			mVideoWidth = MOLiveStreamConstConfig.FORMAT_480P.WIDTH;
			mVideoHeight = MOLiveStreamConstConfig.FORMAT_480P.HEIGHT;
			mVideoBitrate = MOLiveStreamConstConfig.VIDEO_BITRATE_512;
			break;
		case 720:
			mVideoWidth = MOLiveStreamConstConfig.FORMAT_720P.WIDTH;
			mVideoHeight = MOLiveStreamConstConfig.FORMAT_720P.HEIGHT;
			mVideoBitrate = MOLiveStreamConstConfig.VIDEO_BITRATE_512;
			break;
		default:
			break;
		}

		if (mCameraHelper != null) {
			mCameraHelper.setCameraFormat(mVideoWidth, mVideoHeight);
		}
		if (mPublisherHelper != null) {
			mPublisherHelper.SetVideoEncoder(mVideoWidth, mVideoHeight, mVideoFps, mVideoBitrate,enableHWCodec);
		}
	}

	public void SetAudioOption(int sampleRate, int channel) {
		if (sampleRate <= 0 || channel <= 0) {
			//throw new ParamException("Video sampleRate or channel not supported");
		}

		if (mPublisherHelper != null) {
			mMaxAudioReadbytes = mPublisherHelper.SetAudioEncode(sampleRate, channel);
			mAudioHeplper.setAudioOption(sampleRate, mMaxAudioReadbytes);
		}
	}
	
	public void SetCameraView(SurfaceHolder sufaceHolder,int width, int height) {
		
		switch (height) {
		case 240:
			mVideoWidth = MOLiveStreamConstConfig.FORMAT_240P.WIDTH;
			mVideoHeight = MOLiveStreamConstConfig.FORMAT_240P.HEIGHT;
			mVideoBitrate = MOLiveStreamConstConfig.VIDEO_BITRATE_92;
			break;
		case 360:
			mVideoWidth = MOLiveStreamConstConfig.FORMAT_360P.WIDTH;
			mVideoHeight = MOLiveStreamConstConfig.FORMAT_360P.HEIGHT;
			mVideoBitrate = MOLiveStreamConstConfig.VIDEO_BITRATE_512;
			break;
		case 480:
			mVideoWidth = MOLiveStreamConstConfig.FORMAT_480P.WIDTH;
			mVideoHeight = MOLiveStreamConstConfig.FORMAT_480P.HEIGHT;
			mVideoBitrate = MOLiveStreamConstConfig.VIDEO_BITRATE_512;
			break;
		case 720:
			mVideoWidth = MOLiveStreamConstConfig.FORMAT_720P.WIDTH;
			mVideoHeight = MOLiveStreamConstConfig.FORMAT_720P.HEIGHT;
			mVideoBitrate = MOLiveStreamConstConfig.VIDEO_BITRATE_512;
			break;
		default:
			break;
		}
		
		if (mCameraHelper != null) {
			this.mSurfaceHolder = sufaceHolder;
			mCameraHelper.setCameraViewPosition(sufaceHolder);
			mCameraHelper.setCameraFormat(mVideoWidth, mVideoHeight);
		}
	}

	public int ConnectToServer() {
		if (mPublisherHelper != null) {
			return mPublisherHelper.ConnectToServer();
		}
		return -2;
	}

	public int Disconnect() {
		if (mPublisherHelper != null) {
			return mPublisherHelper.Disconnect();
		}
		return -2;
	}

	public int GetLiveStatus() {
		return mMeidaLiveStatus;
	}

	public void StartPreviewCamera(int current_face) {
		
		mCameraFace = current_face;
		
		Log.d(TAG, "OpenCamera:" + mCameraFace);
		if (mCameraHelper != null) {
			mCameraHelper.openCamera(mCameraFace);
		}
	}

	public void StopPreviewCamera() {
		if (mCameraHelper != null) {
			mCameraHelper.closeCamera();
		}
	}

	public int StartLive(int enableVideo, int enableAudio) {
		int ret = -1;
	
		mEnableAudio = enableAudio;

		if (mPublisherHelper != null) {
			ret = mPublisherHelper.StartMediaLive(enableVideo, enableAudio);
			Log.d(TAG, "StartPublish : " + ret);
			if (ret < 0) {
				return ret;
			}
			mMeidaLiveStatus = 1;
		}

		Log.d(TAG, "OpenAudio");
		
		if (mEnableAudio == 1) {
			mAudioHeplper.OpenAudio(mPublisherHelper, mMaxAudioReadbytes);
		}

		return ret;
	}

	public void Stop() {
		if (mPublisherHelper != null){
			mPublisherHelper.StopMediaLive();
		}
		if (mEnableAudio == 1){
			mAudioHeplper.closeAudio();
		}
		mMeidaLiveStatus = 0;
	}

	public void SwitchFlash(int mode/* 1:open, 0:close */) {
		if (mCameraHelper != null){
			mCameraHelper.switchFlash(mode);
		}
	}

	public void SwitchCamera() {
		if (mCameraHelper != null) {
			mCameraHelper.closeCamera();
			if(mCameraFace == Camera.CameraInfo.CAMERA_FACING_BACK)
			{
				mCameraHelper.openCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
				mCameraFace = Camera.CameraInfo.CAMERA_FACING_FRONT;
			}
			else
			{
				mCameraHelper.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
				mCameraFace = Camera.CameraInfo.CAMERA_FACING_BACK;
			}
		}
	}

	public void DeinitMeidaLiveHelper() {
		mAudioHeplper.closeAudio();
		mCameraHelper.closeCamera();
		mPublisherHelper.StopLive();
		mPublisherHelper.Disconnect();
		mPublisherHelper.DeinitMediaPublisher();
		mCameraFace = 1;
	}
}
