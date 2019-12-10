package com.bell_labs.drs.gearvr;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Camera2Helper {

	private Activity mActivity;
	private Size[] mAvailableSizes;
	private Size mPreferredSize;
	private CameraManager mCameraManager;
	private String mCameraId;
	private CameraDevice mCameraDevice;
	private SurfaceTexture mSurfaceTexture;
	private CaptureRequest.Builder mPreviewBuilder;
	private CameraCaptureSession mPreviewSession;
	private MediaRecorder mMediaRecorder;
    private String mNextVideoAbsolutePath;

	public Camera2Helper(Activity activity, int cameraIndex) throws CameraAccessException
	{
		mActivity = activity;
		
		mCameraManager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
		mCameraId = mCameraManager.getCameraIdList()[cameraIndex];
		CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
		StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
		mAvailableSizes = map.getOutputSizes(SurfaceTexture.class);
        mMediaRecorder = new MediaRecorder();

        for(CameraCharacteristics.Key<?> K : characteristics.getKeys()) {
            Log.d("Cam2Help", "Camera Key: " +  K.getName() + ", Value: " + characteristics.get(K).toString());
        }
        Range<Integer>[] ranges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        for(int i=0; i < ranges.length; i++) {
            Log.d("Cam2Help", "AvailableFPSRanges: " + ranges[i].toString());
        }
        for(int i=0; i < mAvailableSizes.length; i++) {
            Log.d("Cam2Help", "AvailableSizes: " + mAvailableSizes[i]);
        }
	}
	
	public Size[] getOutputSizes()
	{
		return mAvailableSizes;
	}
	
	public Size setPreferredSize(int width, int height)
	{
		int pixels = width * height;
		int candidateIndex = 0;
		int candidatePixels = 0;
		for(int i=0; i<mAvailableSizes.length; i++)
		{
			Size s = mAvailableSizes[i];
			int product = s.getWidth() * s.getHeight();
			if( pixels == product )
			{
				mPreferredSize = s;
				return mPreferredSize;
			}
			else if( pixels > product && product > candidatePixels )
			{
				candidateIndex = i;
				candidatePixels = product;
			}
		}
		mPreferredSize = mAvailableSizes[candidateIndex];
		return mPreferredSize;
	}
	
	public Size getPreferredSize()
	{
		return mPreferredSize;
	}
	
	public void startCapture(SurfaceTexture surfaceTexture) throws CameraAccessException
	{
        mSurfaceTexture = surfaceTexture;
        mSurfaceTexture.setDefaultBufferSize(mPreferredSize.getWidth(), mPreferredSize.getHeight());
		
		HandlerThread thread = new HandlerThread("CameraOpen");
		thread.start();
		Handler openHandler = new Handler(thread.getLooper());

        if(mActivity.checkCallingOrSelfPermission(Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
		    mCameraManager.openCamera(mCameraId, mStateCallback, openHandler);
	}

	
	private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

		@Override
		public void onOpened(CameraDevice camera) {
			mCameraDevice = camera;
			startPreview();
		}

		@Override
		public void onDisconnected(CameraDevice camera) {
		}

		@Override
		public void onError(CameraDevice camera, int error) {
		}

	};
	
	private void startPreview()
	{	
		try {
			mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE,	CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
			//mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT);

            // Disable video stabilization. It creates a nasty disparity effect between camera and background.
            mPreviewBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_OFF);
            mPreviewBuilder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_OFF);

            List<Surface> surfaces = new ArrayList<>();
            Surface previewSurface = new Surface(mSurfaceTexture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

			mCameraDevice.createCaptureSession(surfaces, mCaptureCallback, null);


		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}
	
	private CameraCaptureSession.StateCallback mCaptureCallback = new CameraCaptureSession.StateCallback()
	{
		@Override
		public void onConfigured(CameraCaptureSession session)
		{	
			mPreviewSession = session;
			updatePreview();
		}

		@Override
		public void onConfigureFailed(CameraCaptureSession session)
		{

		}
	};
	
	private void updatePreview() {
		mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
		
		HandlerThread thread = new HandlerThread("CameraPreview");
		thread.start();
		Handler backgroundHandler = new Handler(thread.getLooper());

		try 
		{
            CaptureRequest request;
            request = mPreviewBuilder.build();
			mPreviewSession.setRepeatingRequest(request, null, backgroundHandler);
		} catch (CameraAccessException e)
		{
			e.printStackTrace();
		}
	}


	private void setUpMediaRecorder(String tag) throws IOException {
		//mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
			mNextVideoAbsolutePath = getVideoFilePath(mActivity, tag);
		}
		mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
		mMediaRecorder.setVideoEncodingBitRate(10000000);
		mMediaRecorder.setVideoFrameRate(30);
		mMediaRecorder.setVideoSize(mPreferredSize.getWidth(), mPreferredSize.getHeight());
		mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		//mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		mMediaRecorder.prepare();
        
        Log.i("Cam2Help", "Setting up recording to: " + mNextVideoAbsolutePath);
	}

	private String getVideoFilePath(Context context, String tag) {
		final File DCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        final File dir = new File(DCIM.getAbsolutePath() + "/EgoRecorder");
        if(! dir.exists())
            dir.mkdir();
        String fname = tag;
        if (fname.length() == 0)
            fname = "recording" + System.currentTimeMillis();
        return dir.getAbsolutePath() + "/" + fname + ".mp4";
	}

	void startRecordingVideo(String tag) {
		if (null == mCameraDevice || null == mSurfaceTexture || null == mPreferredSize) {
			return;
		}
		try {
			closePreviewSession();
			setUpMediaRecorder(tag);
			mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mPreviewBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_OFF);
            mPreviewBuilder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_OFF);


            List<Surface> surfaces = new ArrayList<>();

			// Set up Surface for the camera preview
			Surface previewSurface = new Surface(mSurfaceTexture);
			surfaces.add(previewSurface);
			mPreviewBuilder.addTarget(previewSurface);

			// Set up Surface for the MediaRecorder
			Surface recorderSurface = mMediaRecorder.getSurface();
			surfaces.add(recorderSurface);
			mPreviewBuilder.addTarget(recorderSurface);

			// Start a capture session
			// Once the session starts, we can update the UI and start recording
			mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

				@Override
				public void onConfigured(CameraCaptureSession cameraCaptureSession) {
					mPreviewSession = cameraCaptureSession;
					updatePreview();
					mMediaRecorder.start();
				}

				@Override
				public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

				}
			}, null);
		} catch (CameraAccessException | IOException e) {
			e.printStackTrace();
		}

	}

	private void closePreviewSession() {
		if (mPreviewSession != null) {
			mPreviewSession.close();
			mPreviewSession = null;
		}
	}

	void stopRecordingVideo() {

		// Stop recording
		mMediaRecorder.stop();
		mMediaRecorder.reset();
		mNextVideoAbsolutePath = null;
		startPreview();
	}

	public void closeCamera() 
	{
		try 
		{
			closePreviewSession();
			if (null != mCameraDevice) 
			{
				mCameraDevice.close();
				mCameraDevice = null;
			}
			if (null != mMediaRecorder) {
				mMediaRecorder.release();
				mMediaRecorder = null;
			}
		} 
		catch (IllegalStateException ie)
		{
			ie.printStackTrace();
		}

	}
}
