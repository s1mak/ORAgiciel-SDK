package fr.oragiciel.sdk.camera;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.widget.Toast;
import fr.oragiciel.sdk.activity.OraActivity;

public class CameraManager {

	private OraActivity oraActivity;
	private Camera camera;
	private Camera.Parameters cameraParameters;
	private CameraPreviewCallback cameraPreviewCallback;
	private SurfaceView sdkSurfaceView;
	private SurfaceView userSurfaceView;
	private int camOrientation;

	private List<FrameListener> fullFrameListeners;
	private List<FrameListener> oraFrameListeners;
	private SubDivisionProportion subDivisionProportion;

	public CameraManager(OraActivity activity) {
		this.oraActivity = activity;
		fullFrameListeners = new LinkedList<FrameListener>();
		oraFrameListeners = new LinkedList<FrameListener>();
	}

	public synchronized void startCamera() {
		if (camera == null) {
			try {
				cameraPreviewCallback = new CameraPreviewCallback(
						fullFrameListeners, oraFrameListeners);
				if (subDivisionProportion != null) {
					cameraPreviewCallback
							.setSubDivisionProportion(subDivisionProportion);
				}
				openCamera();
				if (camera != null) {
					if (cameraParameters != null) {
						camera.setParameters(cameraParameters);
					}
					if (userSurfaceView != null) {
						camera.setPreviewDisplay(userSurfaceView.getHolder());
					} else {
						camera.setPreviewDisplay(sdkSurfaceView.getHolder());
					}
					camOrientation = getOrientationDegree();
					camera.setDisplayOrientation(camOrientation);
					camera.setPreviewCallback(cameraPreviewCallback);
					camera.startPreview();
					oraActivity.cameraStarted();
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
			oraActivity.cameraStopped();
		}
	}

	public void setSdkSurfaceView(SurfaceView sdkSurfaceView) {
		this.sdkSurfaceView = sdkSurfaceView;
	}

	public void setUserSurfaceView(SurfaceView userSurfaceView) {
		this.userSurfaceView = userSurfaceView;
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

	public void addOraFrameListeners(FrameListener frameListener) {
		oraFrameListeners.add(frameListener);
	}

	public void setSubDivisionProportion(
			SubDivisionProportion subDivisionProportion) {
		this.subDivisionProportion = subDivisionProportion;
		if (cameraPreviewCallback != null) {
			cameraPreviewCallback
					.setSubDivisionProportion(subDivisionProportion);
		}
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
