package fr.oragiciel.sdk.camera;

import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;

public class CameraPreviewCallback implements Camera.PreviewCallback {

	private List<FrameListener> fullFrameListeners;
	
	public CameraPreviewCallback(List<FrameListener> fullFrameListeners) {
		this.fullFrameListeners = fullFrameListeners;	
	}
	
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		
		Parameters p = camera.getParameters();
		Size size = p.getPreviewSize();
		for (FrameListener frameListener : fullFrameListeners) {
			frameListener.onFrame(data, size);
		}
//		Log.d("preview", "data="+data.length + " w="+ p.getPreviewSize().width + " h=" + p.getPreviewSize().height+ "bits= " + ImageFormat.getBitsPerPixel(p.getPreviewFormat()));
		
	}

}
