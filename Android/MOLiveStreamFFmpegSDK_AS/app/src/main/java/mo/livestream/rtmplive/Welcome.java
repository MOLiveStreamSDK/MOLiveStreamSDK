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

	private Button setting_btn;
	private EditText rtmp_url_edit;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        setting_btn = (Button)findViewById(R.id.setting_ok);
        setting_btn.setOnClickListener(OnClickChangeBtn);      
        
        rtmp_url_edit = (EditText)findViewById(R.id.server_url_edit);
        
    }
	
	OnClickListener OnClickChangeBtn = new OnClickListener(){
		@SuppressLint("NewApi")
		@Override
		public void onClick(View v) {
			if (v.getId()==R.id.setting_ok)
			{
				Intent intent = new Intent(Welcome.this, MediaLiveActivity.class);
				intent.putExtra("enable audio", "yes"); 
				intent.putExtra("enable video", "yes"); 
				intent.putExtra("video format", "480P");
				intent.putExtra("server url", rtmp_url_edit.getText().toString());
				startActivity(intent);
				//finish();
			}
		}
	};
}

