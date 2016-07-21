package mo.livestream.rtmplive;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;

public class MOLiveStreamCameraHelper implements Runnable, Camera.PreviewCallback {

	private static String TAG = "CameraHelper";
	private Context mContext;
	private SurfaceHolder mSurfaceHolder = null;
	AsyncTask<Void, Void, Void> mPreviewTask = null;// thread
	private MOLiveStreamSDK mMeidaPushHelper = null;
	// CAMERA
	private Camera mCamera = null;
	private boolean mPreviewRunning = false;

	private int mVideoWidth = MOLiveStreamConstConfig.FORMAT_480P.WIDTH;
	private int mVideoHeight = MOLiveStreamConstConfig.FORMAT_480P.HEIGHT;

	private int mCameraOrientation = 0;

	private int mInputCameraFace = 0; // << 用户指定face，openCamera调用；
	private int mDeviceOrientation = 0;

	private int mCurrentCameraId = 0;
	private int mBufSize;

	private Thread mThread;

	public static ArrayList<String> sRotateModel = new ArrayList<String>();

	static {
		sRotateModel.add("Nexus 6");
	}

	// set context
	public void setContext(Context ctx) {
		mContext = ctx;
	}

	// set camera format
	public void setCameraFormat(int width, int height) {
		mVideoWidth = width;
		mVideoHeight = height;
	}

	// set camera callback
	public void setCameraDataCallBack(MOLiveStreamSDK obj) {
		mMeidaPushHelper = obj;
	}

	// set camera view
	public void setCameraViewPosition(SurfaceHolder holder) {
		mSurfaceHolder = holder;
	}

	// open camera
	public int openCamera(int cameraFace)  {
		if (mMeidaPushHelper == null) {
			//throw new ParamException("need set MediaLiveHelper callback");
		}
		if (mSurfaceHolder == null) {
			//throw new ParamException("need set SurfaceHolder callback");
		}
		mInputCameraFace = cameraFace;

		this.mThread = new Thread(this, "camera");
		this.mThread.start();
		return 0;
	}

	public void run() {
		openCamera();
	}

	public int openCamera() {
		try {
			if (mPreviewRunning) {
				Log.d(TAG, "stopPreview");
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}

			selectVideoCapture(mInputCameraFace);
			mSurfaceHolder.setKeepScreenOn(true); // 保持屏幕高亮
			initCamera(mSurfaceHolder, 0);

		} catch (Exception ex) {
			if (null != mCamera) {
				mCamera.release();
				mCamera = null;
			}
		}

		return 0;
	}

	// close camera
	public void closeCamera() {
		if (null != mCamera) {
			try {
				mCamera.stopPreview();
				mCamera.setPreviewCallbackWithBuffer(null);
				mPreviewRunning = false;
				mCamera.release();
				mCamera = null;
			} catch (Exception ex) {
				mCamera = null;
				mPreviewRunning = false;
			}
		}
	}

	// get camera face
	public int getCameraFace() {
		return mInputCameraFace;
	}

	@SuppressLint("NewApi")
	private void initCamera(SurfaceHolder holder, int flash_mode) {
		try {
			Log.d(TAG, "camera id:" + mCurrentCameraId);
			int numberOfCameras = Camera.getNumberOfCameras();
			if (numberOfCameras > 0) {
				Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
				for (int i = 0; i < numberOfCameras; i++) {
					Camera.getCameraInfo(i, cameraInfo);
					if (cameraInfo.facing == mInputCameraFace) {
						mCamera = Camera.open(i);
						mCurrentCameraId = i;
					}
				}
			} else {
				mCamera = Camera.open();
			}
			
			cameraAutoFocus();
			
			mDeviceOrientation = getDisplayOrientation(0, mCurrentCameraId);
			mCamera.setDisplayOrientation(mDeviceOrientation);

			Log.d(TAG, "allocate: device orientation=" + mDeviceOrientation + ", camera orientation="
					+ mCameraOrientation + ", facing=" + mInputCameraFace);

			/* Camera Service settings */
			Camera.Parameters parameters = mCamera.getParameters();
			
			//set focus mode
		    List<String> focusModesList = parameters.getSupportedFocusModes();  
		    for(int i=0;i<focusModesList.size();i++)
		    {
		    	  Log.d(TAG, "Camera Support FOCUS MODE:"+focusModesList.get(i));
		    }		      
		    if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {  		            
		    	  parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		          Log.d(TAG, "Current Focus Mode is FOCUS_MODE_CONTINUOUS_PICTURE");       
		    }else if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {  		            
		    	  parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		          Log.d(TAG, "Current Focus Mode is AUTO FOCUS MODE");    
		    }else if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {  
		    	  parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		          Log.d(TAG, "Current Focus Mode is FOCUS_MODE_CONTINUOUS_VIDEO");  
		    }
		      
		     
		    //set preview size
			List previewSizes = this.mCamera.getParameters().getSupportedPreviewSizes();
			for (int i = 0; i < previewSizes.size(); i++) {
		        Camera.Size s = (Camera.Size)previewSizes.get(i);

		        Log.d(TAG, "preview width:" + s.width + ", height:" + s.height);

		        if ((s.width == this.mVideoWidth) && (s.height == this.mVideoHeight)) {
		          this.mVideoWidth = s.width;
		          this.mVideoHeight = s.height;
		          parameters.setPreviewSize(s.width, s.height);
		          break;
		        }
		    }
			
			//set flash mode
			if (flash_mode == 1) {
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			} else {
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			}

			List<int[]> fpsRange = parameters.getSupportedPreviewFpsRange();
			for (int i = 0; i < fpsRange.size(); i++) {
				int[] r = fpsRange.get(i);
				if (r[0] >= 25000 && r[1] >= 25000) {
					parameters.setPreviewFpsRange(r[0], r[1]);
					break;
				}
			}

			parameters.setPreviewFormat(ImageFormat.NV21);

			try {
				mCamera.setParameters(parameters);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage() + "");
			}

			Camera.Size captureSize = mCamera.getParameters().getPreviewSize();

			mBufSize = captureSize.width * captureSize.height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8;
			for (int i = 0; i < 3; i++) {
				byte[] buffer = new byte[mBufSize];
				mCamera.addCallbackBuffer(buffer);
			}

			Log.d(TAG, "setPreviewCallbackWithBuffer:" + this);
			mCamera.setPreviewCallbackWithBuffer(this);
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (Exception ex) {
			}

			Log.d(TAG, "startPreview:");
			mCamera.startPreview();
			mPreviewRunning = true;

			if (sRotateModel.contains(Build.MODEL)) {
				mInputCameraFace = Camera.CameraInfo.CAMERA_FACING_BACK;
			}

		} catch (Exception e) {
			Log.e(TAG, e.getMessage() + "");
		}
	}

	// camera auto focus
	public void cameraAutoFocus() {
		if (mCamera == null || !mPreviewRunning)
			return;
		try {
			mCamera.autoFocus(null);
		} catch (Exception ex) {
		}
	}

	// switch flash
	public void switchFlash(int mode/* 1:open, 0:close */) {
		if (mPreviewRunning == false) {
			Log.d(TAG, "camera doesn't open");
			return;
		}

		try {
			if (mCurrentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT || mSurfaceHolder == null)
				return;

			if (null != mCamera) {
				mCamera.stopPreview();
				mCamera.setPreviewCallbackWithBuffer(null);
				mPreviewRunning = false;
				mCamera.release();
				mCamera = null;
			}
			initCamera(mSurfaceHolder, mode);
		} catch (Exception ex) {
			if (null != mCamera) {
				mCamera.release();
				mCamera = null;
			}
		}

	}

	// switch camera
	public void switchCamera() {
		if (mPreviewRunning == false) {
			Log.d(TAG, "camera doesn't open");
			return;
		}
		try {
			if (Camera.getNumberOfCameras() == 1 || mSurfaceHolder == null)
				return;
			mCurrentCameraId = (mCurrentCameraId == 0) ? 1 : 0;
			if (null != mCamera) {
				mCamera.stopPreview();
				mCamera.setPreviewCallbackWithBuffer(null);
				mPreviewRunning = false;
				mCamera.release();
				mCamera = null;
			}

			initCamera(mSurfaceHolder, 0);
		} catch (Exception ex) {
			if (null != mCamera) {
				mCamera.release();
				mCamera = null;
			}
		}
	}

	// select video capture device
	private void selectVideoCapture(int facing) {
		for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == facing) {
				mCurrentCameraId = i;
				break;
			}
		}
	}

	public void onPreviewFrame(final byte[] data, final Camera camera) {
		// forward image data to JNI
		// TODO Auto-generated method stub
		if (data == null) {
			// It appears that there is a bug in the camera driver that is
			// asking for a buffer size bigger than it should
			mBufSize += mBufSize / 20;
			camera.addCallbackBuffer(new byte[mBufSize]);
		} else {
			camera.addCallbackBuffer(data);
			//Log.e(TAG, mMeidaPushHelper.GetMediaLiveStatus()+"");
			if (mMeidaPushHelper.GetMediaLiveStatus() == 1 && mMeidaPushHelper != null) {
				Log.d(TAG, "OnCaptureVideo:" + data.length);
				
				mMeidaPushHelper.OnCaptureVideoFrame(data, this.mVideoWidth, this.mVideoHeight, 0L, getCameraFace());
				Log.d(TAG, "OnCaptureVideo");
			}
		}
	}

	public static int getDisplayOrientation(int degrees, int cameraId) {
		
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		return result;
	}
}