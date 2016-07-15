package mo.livestream.rtmplive;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class MOLiveStreamAudioHelper {

	private MOLiveStreamSDK mMediaPusher = null;
	
	private boolean isAudioRecording = false;
	private int mAudioSampleRate = 44100;
	private int mAudioChannels = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private int mAudioEncoding = AudioFormat.ENCODING_PCM_16BIT;

	private AudioRecord mAudioRecord = null;
	private int mMaxAudioReadbytes = 0;

	public void setAudioOption(int sampleRate, int maxAudioReadbytes) {
		if (sampleRate <= 0) {
			mAudioSampleRate = 44100;
			mAudioChannels = AudioFormat.CHANNEL_CONFIGURATION_MONO;
		} else {
			mAudioSampleRate = 44100;
			mAudioChannels = AudioFormat.CHANNEL_CONFIGURATION_MONO;
		}

		mMaxAudioReadbytes = maxAudioReadbytes;
		Log.d("audio record", "max audio read bytes:" + mMaxAudioReadbytes + "," + maxAudioReadbytes);	
	}

	public void setAudioDataCallBack(MOLiveStreamSDK Obj) {
		mMediaPusher = Obj;
	}

	public void OpenAudio(MOLiveStreamSDK mediaPusher, int maxAudioReadbytes) {
		if (isAudioRecording){
			return;
		}
		
		int recAudioBufSize = AudioRecord.getMinBufferSize(mAudioSampleRate, mAudioChannels, mAudioEncoding);

		Log.e("", "recBufSize:" + recAudioBufSize + "Channel: " + mAudioChannels);
		mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, mAudioSampleRate, mAudioChannels,
				mAudioEncoding, recAudioBufSize);
		isAudioRecording = true;
		
		new RecordPlayThread().start();
	}

	public void closeAudio() {
		isAudioRecording = false;
	}

	class RecordPlayThread extends Thread {
		public void run() {
			try {
				Log.d("audio record", "max audio read bytes:" + mMaxAudioReadbytes);

				byte[] pcmBuffer = new byte[mMaxAudioReadbytes];
				mAudioRecord.startRecording();

				Log.d("audio record", "start");

				while (isAudioRecording) {

					int bufferReadResult = mAudioRecord.read(pcmBuffer, 0, mMaxAudioReadbytes);
					Log.d("audio record",
							"read audio size:" + bufferReadResult + ",set buffer size: " + mMaxAudioReadbytes);
					if (bufferReadResult > 0 && mMediaPusher != null) {
						Log.d("audio record", "onCaputureAudioData");
						mMediaPusher.OnCaptureAudioData(pcmBuffer, bufferReadResult);
					}
					Thread.sleep(1);
				}
				
				Log.d("audio record", "stop");
				mAudioRecord.stop();
			
			} catch (Throwable t) {
				Log.e("", "", t);
			}
		}
	};
}
