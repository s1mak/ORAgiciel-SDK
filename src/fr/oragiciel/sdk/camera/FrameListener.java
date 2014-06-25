package fr.oragiciel.sdk.camera;

import android.hardware.Camera.Size;


public interface FrameListener {

	void onFrame(byte[] data, Size size);
	
}
