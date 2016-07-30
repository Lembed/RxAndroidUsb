package org.lembed;

import android.content.Context;
import android.hardware.usb.UsbDevice;

import org.lembed.internal.RxAndroidUsbImpl;

import java.util.HashMap;


import rx.Observable;

public  abstract class RxAndroidUsb {


	public static RxAndroidUsb getInstance(Context context) {
		return new RxAndroidUsbImpl(context);
	}

	public abstract Observable<UsbDevice> rxOnDetached();

	public abstract Observable<UsbDevice> rxOnAttached();

	public abstract Observable<HashMap<String, UsbDevice>> rxScanUsbDevice();

	public  abstract   Observable<RxUsbDevice> rxEstablishConnection(UsbDevice usbDevice);

	public abstract Observable<Void> rxRequestPermission(UsbDevice usbDevice);
}
