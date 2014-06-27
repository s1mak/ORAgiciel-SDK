package fr.oragiciel.sdk.camera;

import java.util.List;

import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;

public class CameraPreviewCallback implements Camera.PreviewCallback {

	private List<FrameListener> oraFrameListeners;
	private List<FrameListener> fullFrameListeners;
	private SubDivisionProportion subDivisionProportion;

	public CameraPreviewCallback(List<FrameListener> fullFrameListeners,
			List<FrameListener> oraFrameListeners) {
		this.fullFrameListeners = fullFrameListeners;
		this.oraFrameListeners = oraFrameListeners;
		subDivisionProportion = new SubDivisionProportion(0, 0, 1, 1);
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {

		Parameters p = camera.getParameters();
		Size size = p.getPreviewSize();
		for (FrameListener frameListener : fullFrameListeners) {
			frameListener.onFrame(data, size);
		}
		Point expectedXY = new Point((int) (size.width
				* subDivisionProportion.getX()), (int) (size.height
				* subDivisionProportion.getY()));
		Size expectedSize = camera.new Size((int) (size.width
				* subDivisionProportion.getWidth()), (int) (size.height
				* subDivisionProportion.getHeight()));
		
		for (FrameListener frameListener : oraFrameListeners) {
			byte[] subData = getSubDivision(data, size, expectedXY,
					expectedSize);
			frameListener.onFrame(subData, expectedSize);
		}
	}

	public byte[] getSubDivision(byte[] data, Size intial, Point expectedXY,
			Size expectedSize) {
		int dataWidth = intial.width;
		int dataHeight = intial.height;
		int width = expectedSize.width;
		int height = expectedSize.height;
		int left = expectedXY.x;
		int top = expectedXY.y;

		if (width == dataWidth && height == dataHeight) {
			return data;
		}

		int area = width * height;
		byte[] matrix = new byte[area];
		int inputOffset = top * dataWidth + left;

		// If the width matches the full width of the underlying data, perform a
		// single copy.
		if (width == dataWidth) {
			System.arraycopy(data, inputOffset, matrix, 0, area);
			return matrix;
		}

		// Otherwise copy one cropped row at a time.
		byte[] yuv = data;
		for (int y = 0; y < height; y++) {
			int outputOffset = y * width;
			System.arraycopy(yuv, inputOffset, matrix, outputOffset, width);
			inputOffset += dataWidth;
		}
		return matrix;
	}

	public void setSubDivisionProportion(
			SubDivisionProportion subDivisionProportion) {
		this.subDivisionProportion = subDivisionProportion;
	}
}
