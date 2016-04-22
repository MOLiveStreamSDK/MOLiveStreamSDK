package mo.livestream.rtmplive;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class MediaLiveActivity extends Activity implements Callback {
	private static String TAG = "Live Activity";

	private MOliveStreamHelper mMediaLiveHelper = null;

	private Button mPreviewBtn = null;
	private Button mFlashCtrlBtn = null;
	private Button mLiveCtrlBtn = null;
	private Button mChangeCameraBtn = null;
	private Button mBackBtn = null;
	private SurfaceView mSurfaceView = null;
	private SurfaceHolder mSurfaceHolder = null;

	private int mVideoWidth  = 640;
	private int mVideoHeight = 480;
	
	private int isPreviewCamera = 0;
	private int isOpenFlash = 0;

	private int m_enableAudio = 0;
	private int m_enableVideo = 0;

	private String mRtmpUrl = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mPreviewBtn = (Button) findViewById(R.id.preview_camera);
		mPreviewBtn.setOnClickListener(OnClickChangeBtn);

		mFlashCtrlBtn = (Button) findViewById(R.id.flash_ctrl);
		mFlashCtrlBtn.setOnClickListener(OnClickChangeBtn);

		mLiveCtrlBtn = (Button) findViewById(R.id.live_control);
		mLiveCtrlBtn.setOnClickListener(OnClickChangeBtn);

		mChangeCameraBtn = (Button) findViewById(R.id.camera_change);
		mChangeCameraBtn.setOnClickListener(OnClickChangeBtn);

		mBackBtn = (Button) findViewById(R.id.back);
		mBackBtn.setOnClickListener(OnClickChangeBtn);

		mSurfaceView = (SurfaceView) this.findViewById(R.id.surface);
		
		
		
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		Intent intent = getIntent();
		String stringEnableAudio = intent.getStringExtra("enable audio");
		String stringEnableVideo = intent.getStringExtra("enable video");
		String stringUrl = intent.getStringExtra("server url");

		String stringVideoFormat = intent.getStringExtra("video format");
		Log.d("Video Format", "Video Fromat:" + stringVideoFormat);

		if (stringEnableAudio.contentEquals("yes")) {
			Log.d("value", "enable audio:" + stringEnableAudio);
			m_enableAudio = 1;
		}
		if (stringEnableVideo.contentEquals("yes")) {
			Log.d("value", "enable video:" + stringEnableVideo);
			m_enableVideo = 1;
		}

		if (stringUrl.length() > 7) {
			Log.d("url", stringUrl);
			mRtmpUrl = stringUrl;
		}

		mMediaLiveHelper = new MOliveStreamHelper();

		mMediaLiveHelper.InitMeidaLiveHelper(new MOLiveStreamCallBack() {

			@Override
			public void onDisconnect() {
				// TODO Auto-generated method stub
				Log.d(TAG, "onDisconnect");
			}

			@Override
			public void onConnecting() {
				// TODO Auto-generated method stub
				Log.d(TAG, "onConnecting");
			}

			@Override
			public void onConnected() {
				// TODO Auto-generated method stub
				Log.d(TAG, "onConnected");
				
			}
			public void onConnectError(int err){
				Log.d(TAG, "onConnectError:"+err);
			}
		});
			mMediaLiveHelper.SetVideoOption(mVideoWidth, mVideoHeight, MOLiveStreamConstConfig.HW_VideoEncode_Disable);
			mMediaLiveHelper.SetCameraView(mSurfaceHolder, mVideoWidth, mVideoHeight);
			mMediaLiveHelper.SetAudioOption(MOLiveStreamConstConfig.AUDIO_SAMPLE_RATE_44100, 
											MOLiveStreamConstConfig.AUDIO_CHANNELS_MONO);
			mMediaLiveHelper.SetServerUrl(mRtmpUrl);
	}

	OnClickListener OnClickChangeBtn = new OnClickListener() {
		@SuppressLint("NewApi")
		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.camera_change) {
				mMediaLiveHelper.SwitchCamera();
			} else if (v.getId() == R.id.live_control) {

				RunPublisherHelper();
			} else if (v.getId() == R.id.back) {
				Intent intent = new Intent(MediaLiveActivity.this, SettingActivity.class);
				startActivity(intent);
				// finish();
			} else if (v.getId() == R.id.preview_camera) {
				if (isPreviewCamera == 0) {
					mMediaLiveHelper.StartPreviewCamera(MOLiveStreamConstConfig.CAMERA_FACE_FRONT);
					isPreviewCamera = 1;
					mPreviewBtn.setText("Stop preview");
				} else {
					mMediaLiveHelper.StopPreviewCamera();
					isPreviewCamera = 0;
					mPreviewBtn.setText("Start preview");
				}
			} else if (v.getId() == R.id.flash_ctrl) {
				if (isOpenFlash == 0) {
					mMediaLiveHelper.SwitchFlash(1);
					isOpenFlash = 1;
					mFlashCtrlBtn.setText("Stop flash");
				} else {
					mMediaLiveHelper.SwitchFlash(0);
					isOpenFlash = 0;
					mFlashCtrlBtn.setText("Start flash");
				}
			}
		}
	};

	private void RunPublisherHelper() {
		int ret = -1;

		if (mMediaLiveHelper.GetLiveStatus() == 0) {
			mMediaLiveHelper.ConnectToServer();

			ret = mMediaLiveHelper.StartLive(m_enableVideo, m_enableAudio);

			if (ret < 0) {
				mLiveCtrlBtn.setText("start");
				Log.d(TAG, "Start live Error:" + ret);
			} else {
				mLiveCtrlBtn.setText("Stop");
				Log.d(TAG, "Start live Sucesse");
			}
		} else {
			mLiveCtrlBtn.setText("Start");
			mMediaLiveHelper.Stop();
			mMediaLiveHelper.Disconnect();
			Log.d(TAG, "Stop live");
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onDestroy");
		mMediaLiveHelper.DeinitMeidaLiveHelper();

		super.onDestroy();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.d(TAG, "surfaceCreated:" + mSurfaceHolder);
		mMediaLiveHelper.StartPreviewCamera(MOLiveStreamConstConfig.CAMERA_FACE_FRONT);
		isPreviewCamera = 1;
		mPreviewBtn.setText("Stop preview");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d(TAG, "surfaceChanged:" + mSurfaceHolder);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.d(TAG, "surfaceDestroyed:" + mSurfaceHolder);
	}
}
