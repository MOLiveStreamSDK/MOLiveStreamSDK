package mo.streamlive.medialivedemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.example.medialivedemo.R;
import com.moqi.rtmplive.ConstConfig;
import com.moqi.rtmplive.MediaLiveHelper;
import com.moqi.rtmplive.MediaPusherCallBack;

public class MainActivity extends Activity implements Callback {

	private static String TAG = "MainActivity";
	
	private MediaLiveHelper m_MediaLiveHelper = null;
	
	private Button        m_LiveCtrlBtn     = null;   //<< media live control
	private Button        m_ChangeCameraBtn = null;   //<< camera switch control
	private Button        m_BackBtn         = null;   //<< goto other activity for test
	private Button        m_FlashBtn         = null;  //<< camera flash control
	private Button		  m_CameraPreviewBtn = null;  //<< camera preview control
	
	private SurfaceView   m_SurfaceView     = null;   //<< camera view
	private SurfaceHolder m_SurfaceHolder    = null;  //<< 
	private EditText      rtmp_url_edit      = null;  //<< server url
	
	private int           m_videoWidth       = 1280;  //<<  video width
	private int           m_videoHight       = 720;   //<<  video height
	
	private int           m_isLiveStart      = 0;     //<< meida live status
	private int           m_isFlashOn        = 0;     //<< camera flash statis̬
	private int           m_enableAudio      = 1;     //<< enable audio live
	private int           m_enableVideo      = 1;     //<< enable video live
	
	private String        m_rtmp_url = null;
	
	private int           m_is_preview_camera = 0; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//camera flash
		m_FlashBtn = (Button)findViewById(R.id.flash);
		m_FlashBtn.setOnClickListener(OnClickListenBtn);
        
		//camera_preview
		m_CameraPreviewBtn = (Button)findViewById(R.id.preview);
		m_CameraPreviewBtn.setOnClickListener(OnClickListenBtn);
		        
		//media live
		m_LiveCtrlBtn = (Button)findViewById(R.id.live_control);
        m_LiveCtrlBtn.setOnClickListener(OnClickListenBtn);
        
        //camera switch
        m_ChangeCameraBtn = (Button) findViewById(R.id.camera_change);
        m_ChangeCameraBtn.setOnClickListener(OnClickListenBtn);
        
        //go to other flash
        m_BackBtn  = (Button) findViewById(R.id.back);
        m_BackBtn.setOnClickListener(OnClickListenBtn);
        
        rtmp_url_edit = (EditText)findViewById(R.id.url_tv);
        
        //camera view
        m_SurfaceView = (SurfaceView) this.findViewById(R.id.surface);  
        m_SurfaceHolder = m_SurfaceView.getHolder();  
        m_SurfaceHolder.addCallback(this);  
        
        m_rtmp_url = "rtmp://192.168.0.104/live/ios";
        rtmp_url_edit.setText(m_rtmp_url);
        rtmp_url_edit.setBackgroundColor(Color.WHITE);
        
        //create media live object
        m_MediaLiveHelper = new MediaLiveHelper();
        //initialize object 
        m_MediaLiveHelper.InitMeidaLiveHelper(new MediaPusherCallBack() {
			
			@Override
			public void onDisconnect() {
				// TODO Auto-generated method stub
				Log.d(TAG,"onDisconnect");
			}
			
			@Override
			public void onConnecting() {
				// TODO Auto-generated method stub
				Log.d(TAG,"onConnecting");
			}
			
			@Override
			public void onConnected() {
				// TODO Auto-generated method stub
				Log.d(TAG,"onConnecting");
			}
			public void onConnectError(int err)
			{
				Log.d(TAG,"onConnectError:"+err);
			}
		});
        //set the camera view position and resolution ratio, before start preview camera
        m_MediaLiveHelper.SetCameraView(m_SurfaceHolder, m_videoWidth, m_videoHight);
        //set the video resolution ratio as same as the camera setting
        m_MediaLiveHelper.SetVideoOption(m_videoWidth, m_videoHight , ConstConfig.HW_VideoEncode_Disable);
        //set the audio option(use default), before start live
        m_MediaLiveHelper.SetAudioOption(44100, 1);
        //set the server url before connect to server
        m_MediaLiveHelper.SetServerUrl(m_rtmp_url);
	}
	
	OnClickListener OnClickListenBtn = new OnClickListener(){
		@SuppressLint("NewApi")
		@Override
		public void onClick(View v) {
			if (v.getId()==R.id.camera_change)
			{
				//switch the camera
				m_MediaLiveHelper.SwitchCamera();
			}
			else if(v.getId()==R.id.live_control)
			{
				//media live control
				RunPublisherHelper();
			}
			else if(v.getId() == R.id.back)
			{
				//跳转测试
//				m_MediaLiveHelper.Stop();
//				m_MediaLiveHelper.StartPreviewCamera();
//				m_MediaLiveHelper.Disconnect();
//				m_MediaLiveHelper.DeinitMeidaLiveHelper();
				Log.d(TAG,"go to setting");
//				Intent intent = new Intent(MainActivity.this, SettingActivity.class);
//				startActivity(intent);
	            finish();
			}
			else if(v.getId() == R.id.flash)
			{
				if(m_isFlashOn == 0)
					m_isFlashOn=1;
				else
					m_isFlashOn=0;
				
				//camera flash control
				m_MediaLiveHelper.SwitchFlash(m_isFlashOn); // 1:������0:�ر�		
			}
			else if(v.getId() == R.id.preview)
			{
				if(m_is_preview_camera==0)
		           {
		        	   //start preview
					   m_MediaLiveHelper.StartPreviewCamera(ConstConfig.CAMERA_FACE_FRONT);
		        	   m_is_preview_camera = 1;
		        	   m_CameraPreviewBtn.setText("stop");
		           }
		           else
		           {
		        	   //stop preview
		        	   m_MediaLiveHelper.StopPreviewCamera();
		        	   m_is_preview_camera = 0;
		        	   m_CameraPreviewBtn.setText("cam");
		           }	
			}
	   }
	};

	private void RunPublisherHelper()
	{
		int ret = -1;
		
		if(m_isLiveStart == 0)
		{	        										
	        if(m_MediaLiveHelper.GetLiveStatus() == 0)
			{
	        	m_rtmp_url    = rtmp_url_edit.getText().toString();
	        	Log.d(TAG,m_rtmp_url);
	        	m_MediaLiveHelper.SetServerUrl(m_rtmp_url);
	        	
	        	//connect to server 
	        	ret = m_MediaLiveHelper.ConnectToServer();
	        	Log.d(TAG, "Connect to server:"+ret);
	        	//start live
				ret = m_MediaLiveHelper.StartLive(m_enableVideo, m_enableAudio);
				
				Log.d(TAG, "live:"+ret);
			
				if(ret < 0)
				{
					Log.d(TAG, "Start live Error:" + ret);
				}
				else
				{
					m_isLiveStart = 1;
					m_LiveCtrlBtn.setText("stop");
				}
			}
		}
		else
		{		
			m_LiveCtrlBtn.setText("live");
			
			//ֹͣstop live
			m_MediaLiveHelper.Stop();
    		Log.d(TAG, "Stop");
    		
    		// disconnect
    		m_MediaLiveHelper.Disconnect();
    		Log.d(TAG, "Disconnect");	
    		
    		m_isLiveStart = 0;
		}
	}
   
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		m_MediaLiveHelper.Stop();
		m_MediaLiveHelper.StopPreviewCamera();
		m_MediaLiveHelper.Disconnect();
		m_MediaLiveHelper.DeinitMeidaLiveHelper();
		
		super.onDestroy();
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		//start preview
		if(m_MediaLiveHelper !=null)
		{
			m_MediaLiveHelper.StartPreviewCamera(ConstConfig.CAMERA_FACE_FRONT);
			m_CameraPreviewBtn.setText("stop"); 
			m_is_preview_camera = 1;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub		
	}

}
