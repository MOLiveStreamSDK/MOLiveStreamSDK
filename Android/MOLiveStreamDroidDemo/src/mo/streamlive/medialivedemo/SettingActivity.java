package mo.streamlive.medialivedemo;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.example.medialivedemo.R;
import com.moqi.rtmplive.ConstConfig;
import com.moqi.rtmplive.MediaLiveHelper;
import com.moqi.rtmplive.MediaPusherCallBack;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SettingActivity extends Activity implements Callback{
	
	private static String TAG = "SettingActivity";
	
	private MediaLiveHelper m_MediaLiveHelper = null;
	
	private SurfaceView   m_SurfaceView     = null;   //<< camera view
	private SurfaceHolder m_SurfaceHolder    = null;  //<<
	 
	private int           m_videoWidth       = 1280;  //<<  video width
	private int           m_videoHight       = 720;  //<<  video height
    
	private Button        m_LiveCtrlBtn     = null;   //<< media live control
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        
        
        
        //camera view
        m_SurfaceView = (SurfaceView) this.findViewById(R.id.surface);
        m_SurfaceHolder = m_SurfaceView.getHolder();  
        m_SurfaceHolder.addCallback(this);
        
      //media live
        m_LiveCtrlBtn = (Button)findViewById(R.id.live_control);
        m_LiveCtrlBtn.setOnClickListener(OnClickListenBtn);
        
        //create media live object
        m_MediaLiveHelper = new MediaLiveHelper();
        //initialize object 
        m_MediaLiveHelper.InitMeidaLiveHelper(new MediaPusherCallBack() {
			
			@Override
			public void onDisconnect() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onConnecting() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onConnected() {
				// TODO Auto-generated method stub
				
			}
			public void onConnectError(int err)
			{
				Log.d(TAG,"onConnectError:"+err);
			}
		});
        //set the camera view position and resolution ratio, before start preview camera
        m_MediaLiveHelper.SetCameraView(m_SurfaceHolder, m_videoWidth, m_videoHight);
        
	}
	
	OnClickListener OnClickListenBtn = new OnClickListener(){
		@SuppressLint("NewApi")
		@Override
		public void onClick(View v) {
			if (v.getId()==R.id.live_control)
			{
				Log.d(TAG,"stop preview");
				m_MediaLiveHelper.StopPreviewCamera();
				Log.d(TAG,"deinit preview");
				m_MediaLiveHelper.DeinitMeidaLiveHelper();
				
				Log.d(TAG,"go to MainActivity");
				//跳转
				Intent intent = new Intent(SettingActivity.this, MainActivity.class);
				startActivity(intent);
	            finish();
			}
	   }
	};
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		//start preview
		if(m_MediaLiveHelper !=null)
		{
			m_MediaLiveHelper.StartPreviewCamera(ConstConfig.CAMERA_FACE_FRONT);
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
