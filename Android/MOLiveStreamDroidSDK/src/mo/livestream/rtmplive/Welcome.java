package mo.livestream.rtmplive;

import mo.livestream.rtmplive.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

public class Welcome extends Activity{
	public int m_enableAudio = 0;
	public int m_enableVideo = 0;
	
	public int m_useVideo240P = 0;
	public int m_useVideo360P = 0;
	public int m_useVideo720P = 1;
	public int m_useVideo480P = 0;
	
	private Button setting_btn;
	private EditText rtmp_url_edit;
	private CheckBox enable_audio_check_box;
	private CheckBox enable_video_check_box;
	private RadioButton enable_480P;
	private RadioButton enable_240P;
	private RadioButton enable_720P;
	private RadioButton enable_360P;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        
        	Time time = new Time("GMT+8"); 
        	time.setToNow(); 
        	int year = time.year; 
        	int month = time.month; 
        	int day = time.monthDay; 
        	Log.d("welcome","time:"+year+"-"+month);
        	if(year != 2016 || month >3 )
        	{
        		System.exit(0);
        	}

        
        setting_btn = (Button)findViewById(R.id.setting_ok);
        setting_btn.setOnClickListener(OnClickChangeBtn);
        
        enable_audio_check_box = (CheckBox)findViewById(R.id.enable_audio_checkbox);
        enable_audio_check_box.setOnClickListener(OnClickChangeBtn);
        
        enable_video_check_box = (CheckBox)findViewById(R.id.enable_video_checkbox);
        enable_video_check_box.setOnClickListener(OnClickChangeBtn);
        
        enable_480P = (RadioButton)findViewById(R.id.radio480P);
        enable_480P.setOnClickListener(OnClickChangeBtn);
        
        enable_240P = (RadioButton)findViewById(R.id.radio240P);
        enable_240P.setOnClickListener(OnClickChangeBtn);
        
        enable_720P = (RadioButton)findViewById(R.id.radio720P);
        enable_720P.setOnClickListener(OnClickChangeBtn);
        
        enable_360P = (RadioButton)findViewById(R.id.radio360P);
        enable_360P.setOnClickListener(OnClickChangeBtn);
        
        
        rtmp_url_edit = (EditText)findViewById(R.id.server_url_edit);
        
    }
	
	OnClickListener OnClickChangeBtn = new OnClickListener(){
		@SuppressLint("NewApi")
		@Override
		public void onClick(View v) {
			if (v.getId()==R.id.setting_ok)
			{
				Intent intent = new Intent(Welcome.this, MediaLiveActivity.class);
				
				if(m_enableAudio == 1)
				{
					intent.putExtra("enable audio", "yes"); 
				}
				else
				{
					intent.putExtra("enable audio", "no"); 
				}
				
				if(m_enableVideo == 1)
				{
					intent.putExtra("enable video", "yes"); 
				}
				else
				{
					intent.putExtra("enable video", "no"); 
				}
				
				if(m_useVideo240P == 1)
				{
					intent.putExtra("video format", "240P"); 
				}
				else if(m_useVideo360P == 1)
				{
					intent.putExtra("video format", "360P");
				}
				else if(m_useVideo480P == 1)
				{
					intent.putExtra("video format", "480P");
				}
				else if(m_useVideo720P == 1)
				{
					intent.putExtra("video format", "720P");
				}
				
				intent.putExtra("server url", rtmp_url_edit.getText().toString());
				startActivity(intent);
				//finish();
			}
			else if(v.getId() == R.id.enable_audio_checkbox)
			{
				if(enable_audio_check_box.isChecked())
				{
					m_enableAudio = 1;
				}
				else
				{
					m_enableAudio = 0;
				}
			}
			else if(v.getId() == R.id.enable_video_checkbox)
			{
				if(enable_video_check_box.isChecked())
				{
					m_enableVideo = 1;
				}
				else
				{
					m_enableVideo = 0;
				}
				
			}
			else if(v.getId() == R.id.radio720P)
			{
				enable_720P.setChecked(true);
				enable_480P.setChecked(false);
				enable_360P.setChecked(false);
				enable_240P.setChecked(false);
				
				m_useVideo720P = 1;
				m_useVideo480P = 0;
				m_useVideo360P = 0;
				m_useVideo240P = 0;
				Log.d("radio","720P");
			}
			else if(v.getId() == R.id.radio480P)
			{
				enable_720P.setChecked(false);
				enable_480P.setChecked(true);
				enable_360P.setChecked(false);
				enable_240P.setChecked(false);
				
				m_useVideo720P = 0;
				m_useVideo480P = 1;
				m_useVideo360P = 0;
				m_useVideo240P = 0;
				Log.d("radio","480P");
			}
			else if(v.getId() == R.id.radio360P)
			{
				enable_720P.setChecked(false);
				enable_480P.setChecked(false);
				enable_360P.setChecked(true);
				enable_240P.setChecked(false);
				
				m_useVideo720P = 0;
				m_useVideo480P = 0;
				m_useVideo360P = 1;
				m_useVideo240P = 0;
				Log.d("radio","360P");
			}
			else if(v.getId() == R.id.radio240P)
			{
				enable_720P.setChecked(false);
				enable_480P.setChecked(false);
				enable_360P.setChecked(false);
				enable_240P.setChecked(true);
				
				m_useVideo720P = 0;
				m_useVideo480P = 0;
				m_useVideo360P = 0;
				m_useVideo240P = 1;
				Log.d("radio","240P");
			}
		}
	};
}

