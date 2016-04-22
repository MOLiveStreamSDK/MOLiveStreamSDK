package mo.livestream.rtmplive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

@SuppressLint("InlinedApi")
public class MOLiveStreamAvcEncoder {	
	private static String TAG = "AvcEncoder";
	
	private final static String MINE_TYPE = "video/avc";
	private MediaCodec mediaCodec;
	private int m_width = 0;
	private int m_height = 0;
	private int m_bitrate = 0;
	byte[] m_info = null;
	public int m_sps_len = 0;
	public int m_pps_len = 0;
    private int    m_color_formats = 0;
	private String m_sEncoderName = null;

	public static final int[] SUPPORTED_COLOR_FORMATS = { 
		    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar,
			MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar,
			MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar,
			MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar,
			MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar };

	private static Codec[] sEncoders = null;

	static class Codec {
		public Codec(String type, String name, Integer[] formats) {
			this.type = type;
			this.name = name;
			this.formats = formats;
		}

		public String type;
		public String name;
		public Integer[] formats;
	}

	@SuppressLint("NewApi")
	public MOLiveStreamAvcEncoder() {

	}

	@SuppressLint("NewApi")
	public int initialize() {
		// check sdk api support
		if (isSupport() == false)
			return -1;

		return 0;
	}

	protected boolean isSupport() {
		if (Build.VERSION.SDK_INT >= 18 /* Build.VERSION_CODES.JELLY_BEAN_MR2 */) {
			Codec[] encoders = findEncodersForMimeType(MINE_TYPE);

			// support encoder flag
			int support_qcom_encoder = 0;
			int support_google_encoder = 0;
			
			// Find available encoders
			for (int i = 0; i < encoders.length; i++) {
				for (int j = 0; j < encoders[i].formats.length; j++) {
					
					Log.d(TAG, encoders[i].formats[j] + "," + encoders[i].name + ",type:" + encoders[i].type);
					
					if (encoders[i].name.equalsIgnoreCase("OMX.google.h264.encoder"))
					{
						m_color_formats = encoders[i].formats[j];
						support_google_encoder = 1;
					}
					else if(encoders[i].name.equalsIgnoreCase("OMX.qcom.video.encoder.avc"))
					{
						m_color_formats = encoders[i].formats[j];
						support_qcom_encoder = 1;
					}
				}
			}
			if(support_qcom_encoder == 1)
			{
				m_sEncoderName = "OMX.qcom.video.encoder.avc";
				return true;
			}
			else if(support_google_encoder == 1)
			{
				m_sEncoderName = "OMX.google.h264.encoder";
				return true;
			}
		}
		return false;
	}

	@SuppressLint("NewApi")
	public synchronized static Codec[] findEncodersForMimeType(String mimeType) {
		if (sEncoders != null)
			return sEncoders;

		ArrayList<Codec> encoders = new ArrayList<Codec>();

		// We loop through the encoders, apparently this can take up to a sec
		for (int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--) {
			MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);
			if (!codecInfo.isEncoder())
				continue;

			String[] types = codecInfo.getSupportedTypes();

			for (int i = 0; i < types.length; i++) {

				// find all type, name, code
				if (types[i].equalsIgnoreCase(mimeType)) {
					Log.d(TAG, "support type:" + types[i]);
					try {
						MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
						Set<Integer> formats = new HashSet<Integer>();

						// And through the color formats supported
						for (int k = 0; k < capabilities.colorFormats.length; k++) {
							int format = capabilities.colorFormats[k];

							if (format == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar||
									format == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) 
							{
								formats.add(format);
							}
						}

						Codec codec = new Codec(types[i], codecInfo.getName(),
								(Integer[]) formats.toArray(new Integer[formats.size()]));
						encoders.add(codec);
					} catch (Exception e) {
						Log.wtf(TAG, e);
					}
				}
			}
		}

		sEncoders = (Codec[]) encoders.toArray(new Codec[encoders.size()]);
		if (sEncoders.length == 0) {
			sEncoders = new Codec[] { new Codec(null, null, new Integer[] { 0 }) };
		}
		return sEncoders;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public int setVideoOptions(int width, int height, int fps, int bitrate) {
		m_width = width;
		m_height = height;
		m_bitrate = bitrate*1000;
		if (Build.VERSION.SDK_INT >= 18 /* Build.VERSION_CODES.JELLY_BEAN_MR2 */) {
			try {
				Log.d(TAG,"[use param] encoder name:"+m_sEncoderName+", colro format:"+m_color_formats);
				
				mediaCodec = MediaCodec.createByCodecName(m_sEncoderName);
				MediaFormat mediaFormat = MediaFormat.createVideoFormat(MINE_TYPE, m_width, m_height);

				mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, m_bitrate);
				mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
				mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1); 
				mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, m_color_formats);
				mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
				mediaCodec.start();
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		} else
			return -1;
		return 0;
	}

	@SuppressLint("NewApi")
	public void close() {
		try {
			mediaCodec.stop();
			mediaCodec.release();
			mediaCodec = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("NewApi")
	public int EncodeData(byte[] input, byte[] output) {
		Log.d(TAG, "---> EncodeData:" + output.length);
		int pos = 0;

		try {
			ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
			ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();

			Log.d(TAG, "---> dequeueInputBuffer");
			int inputBufferIndex = mediaCodec.dequeueInputBuffer(5000000);
			Log.d(TAG, "<--- dequeueInputBuffer:" + inputBufferIndex);

			long time_stamp = System.nanoTime() / 1000;

			if (inputBufferIndex >= 0) {
				ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
				inputBuffer.clear();
				inputBuffer.put(input);
				Log.d(TAG, "---> queueInputBuffer");
				mediaCodec.queueInputBuffer(inputBufferIndex, 0, inputBuffer.position(), time_stamp, 0);
				Log.d(TAG, "<--- queueInputBuffer:" + inputBufferIndex + ",ts:" + time_stamp);
			}

			MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

			Log.d(TAG, "---> dequeueOutputBuffer");
			int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
			Log.d(TAG, "<--- dequeueOutputBuffer:" + outputBufferIndex + ",ts:" + time_stamp);

			while (outputBufferIndex >= 0) {
				ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
				byte[] outData = new byte[bufferInfo.size];
				outputBuffer.get(outData);

				{
					Log.d(TAG, "---> get output data:" + outData.length);

					if (output.length >= outData.length) {
						Log.d(TAG, "---> System.arraycopy:" + outData.length);
						System.arraycopy(outData, 0, output, pos, outData.length);
						pos += outData.length;
						Log.d(TAG, "<--- System.arraycopy:" + outData.length);

						Log.d(TAG, "output pos:" + pos);
					}
					Log.d(TAG, "<--- get output data");
				}

				mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
				outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}

		Log.d(TAG, "<--- EncodeData:");
		return pos;
	} 
}
