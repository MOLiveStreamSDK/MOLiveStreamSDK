package mo.livestream.rtmplive;

import android.hardware.Camera;

class VideoConfig {
	VideoConfig(int width, int height) {
		WIDTH = width;
		HEIGHT = height;
	}

	int WIDTH;
	int HEIGHT;
}

public class MOLiveStreamConstConfig {

	public static final String PUBLIC_RTMP_URL = "rtmp://lazbup.dnion.com/lazb/001";
	public static final String LOCAL_RTMP_URL = "rtmp://192.168.1.22/live/android";

	public static final VideoConfig FORMAT_240P = new VideoConfig(320, 240);
	public static final VideoConfig FORMAT_250P = new VideoConfig(320, 250);
	public static final VideoConfig FORMAT_CIF = new VideoConfig(352, 288);
	public static final VideoConfig FORMAT_360P = new VideoConfig(640, 360);
	public static final VideoConfig FORMAT_480P = new VideoConfig(640, 480);
	public static final VideoConfig FORMAT_720P = new VideoConfig(1280, 720);

	public static final int VIDEO_ENCODING_HEIGHT_240 = 1;
	public static final int VIDEO_ENCODING_HEIGHT_250 = 2;
	public static final int VIDEO_ENCODING_HEIGHT_CIF = 3;
	public static final int VIDEO_ENCODING_HEIGHT_360 = 4;
	public static final int VIDEO_ENCODING_HEIGHT_480 = 5;
	public static final int VIDEO_ENCODING_HEIGHT_720 = 6;
	
	public static final int VIDEO_BITRATE_92 = 92;
	public static final int VIDEO_BITRATE_192 = 192;
	public static final int VIDEO_BITRATE_384 = 384;
	public static final int VIDEO_BITRATE_512 = 512;
	public static final int VIDEO_BITRATE_768 = 768;
	public static final int VIDEO_BITRATE_1M = 1024;
	//public static final int VIDEO_BITRATE_2M = 2048;
	public static int Encode_Audio_Type   = 3001;
	public static int Encode_Video_Type   = 3002;
	public static int Contrl_Encode_Stop  = 3003;
	public static int Contrl_Encode_Start = 3004;
	
	public static boolean HW_VideoEncode_Enable = true;   //<< enable hw encode 
	public static boolean HW_VideoEncode_Disable = false; //<< disable hw encode
	
	public static final int AUDIO_SAMPLE_RATE_44100 = 44100;
	public static final int AUDIO_CHANNELS_MONO = 1;

	public static final int CAMERA_FACE_BACK  = Camera.CameraInfo.CAMERA_FACING_BACK;
	public static final int CAMERA_FACE_FRONT = Camera.CameraInfo.CAMERA_FACING_FRONT;
}

