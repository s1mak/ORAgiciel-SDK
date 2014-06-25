package fr.oragiciel.sdk.qrcode;

import com.google.zxing.Result;

public interface QrCodeListener {

	void onQrCodeRead(Result result);
	
}
