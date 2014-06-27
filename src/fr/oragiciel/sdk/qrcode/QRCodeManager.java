package fr.oragiciel.sdk.qrcode;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import android.app.Activity;
import android.content.res.Resources.Theme;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;

import fr.oragiciel.sdk.camera.CameraManager;
import fr.oragiciel.sdk.camera.FrameListener;

public class QRCodeManager {

	private MultiFormatReader multiFormatReader;
	private QrCodeListener qrCodeListener;
	private CameraManager cameraManager;
	private Activity activity;

	private RawResult rawResult;
	private Boolean inProgress;
	private DecodeThread decodeThread;

	public QRCodeManager(CameraManager cameraManager) {
		this.cameraManager = cameraManager;
		cameraManager.addFullFrameListeners(new QRFrameListener());

		multiFormatReader = new MultiFormatReader();
		Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(
				DecodeHintType.class);

		hints.put(DecodeHintType.POSSIBLE_FORMATS,
				EnumSet.of(BarcodeFormat.QR_CODE));
		hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
		multiFormatReader.setHints(hints);

		inProgress = false;
		decodeThread = new DecodeThread();
	}

	public void setQrCodeListener(QrCodeListener qrCodeListener) {
		this.qrCodeListener = qrCodeListener;
	}

	public void scan() {
		// Parameters cameraParameters = cameraManager.getCameraParameters();
		// cameraParameters.setPreviewFpsRange(1000, 1000);
		// cameraManager.setCameraParameters(cameraParameters);
		decodeThread.start();
		cameraManager.startCamera();
	}

	public void stopScan() {
		decodeThread.interrupt();
		cameraManager.stopCamera();
	}

	private class QRFrameListener implements FrameListener {

		@Override
		public void onFrame(byte[] data, Size size) {
			synchronized (inProgress) {
				if (!inProgress) {
					rawResult = new RawResult();
					rawResult.data = data;
					rawResult.size = size;
					inProgress = true;
				}
			}
		}

	}

	private class RawResult {
		private byte[] data;
		private Size size;
	}

	private class DecodeThread extends Thread {

		@Override
		public void run() {
			while (!isInterrupted()) {

				try {
					byte[] data = null;
					Size size = null;
					synchronized (inProgress) {
						if (rawResult != null) {
							data = rawResult.data;
							size = rawResult.size;
							inProgress = true;
						}
					}

					if (data != null) {
						final Result result = decode(data, size);

						if (result != null) {
							if (qrCodeListener != null) {
								qrCodeListener.onQrCodeRead(result);
							}
						}

						synchronized (inProgress) {
							rawResult = null;
							inProgress = false;
						}
					}

					sleep(250);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}

		private Result decode(byte[] data, Size size) {
			Result rawResult = null;

			long start = System.currentTimeMillis();
			PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
					data, size.width, size.height, 0, 0, size.width,
					size.height, false);
			if (source != null) {
				BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(
						source));
				try {

					rawResult = multiFormatReader.decodeWithState(bitmap);
					if (cameraManager.camIsInverted()) {
						rawResult = recalculateInvertedPoints(rawResult, size);
					}
					return rawResult;
				} catch (ReaderException re) {
					// continue
				} finally {
					multiFormatReader.reset();
				}
				long stop = System.currentTimeMillis();
				Log.d("qrcode", start + " : " + (stop - start));
			}
			return null;
		}

		private Result recalculateInvertedPoints(Result rawResult, Size size) {
			int nbPoints = rawResult.getResultPoints().length;
			ResultPoint[] newResultPoints = new ResultPoint[nbPoints];

			for (int i = 0; i < nbPoints; i++) {
				ResultPoint oldPoint = rawResult.getResultPoints()[i];
				newResultPoints[i] = new ResultPoint(size.width
						- oldPoint.getX(), size.height - oldPoint.getY());
			}

			Result newResult = new Result(rawResult.getText(),
					rawResult.getRawBytes(), newResultPoints,
					rawResult.getBarcodeFormat(), rawResult.getTimestamp());
			newResult.putAllMetadata(rawResult.getResultMetadata());
			return newResult;
		}

	}
}
