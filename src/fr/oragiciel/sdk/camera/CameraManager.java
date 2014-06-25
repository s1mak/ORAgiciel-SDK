package fr.oragiciel.sdk.camera;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import fr.oragiciel.sdk.activity.OraActivity;
import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class CameraManager {

	private OraActivity oraActivity;
	private Camera camera;
	private Camera.Parameters cameraParameters;
	private CameraPreviewCallback cameraPreviewCallback;
	private SurfaceView surfaceViewSdk;
	private int camOrientation;

	private List<FrameListener> fullFrameListeners;

	public CameraManager(OraActivity activity) {
		this.oraActivity = activity;
		fullFrameListeners = new LinkedList<FrameListener>();
	}

	public synchronized void startCamera() {
		if (camera == null) {
			try {
				cameraPreviewCallback = new CameraPreviewCallback(
						fullFrameListeners);
				openCamera();
				if (camera != null) {
					if (cameraParameters != null) {
						camera.setParameters(cameraParameters);
					}
					oraActivity.resetBackground();
					camera.setPreviewDisplay(surfaceViewSdk.getHolder());
					camOrientation = getOrientationDegree();
					camera.setDisplayOrientation(camOrientation);
					camera.setPreviewCallback(cameraPreviewCallback);
					camera.startPreview();
				} else {
					Toast.makeText(oraActivity,
							"Immpossible d'utiliser la caméra",
							Toast.LENGTH_LONG);
				}
			} catch (IOException e) {
				Log.e("camera", e.getMessage(), e);
				e.printStackTrace();

				if (camera != null) {
					camera.release();
					camera = null;
				}
			}
		}
	}

	private void openCamera() {
		if (camera == null) {
			camera = Camera.open();
		}
	}

	public synchronized void stopCamera() {
		if (camera != null) {
			camera.stopPreview();
			camera.setPreviewCallback(null);
			camera.release();
			camera = null;
		}
	}

	public void setSurfaceView(SurfaceView surfaceViewSdk) {
		this.surfaceViewSdk = surfaceViewSdk;
	}

	public Camera.Parameters getCameraParameters() {
		if (cameraParameters == null) {
			openCamera();
			cameraParameters = camera.getParameters();
		}
		return cameraParameters;
	}

	public void setCameraParameters(Camera.Parameters cameraParameters) {
		this.cameraParameters = cameraParameters;
	}

	public void addFullFrameListeners(FrameListener frameListener) {
		fullFrameListeners.add(frameListener);
	}

	private int getOrientationDegree() {
		int rotation = oraActivity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		result = (90 - degrees + 360) % 360;

		return result;
	}

	public boolean camIsInverted() {
		return camOrientation == 180;
	}

}
