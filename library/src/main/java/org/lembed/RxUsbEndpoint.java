package org.lembed;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;

import org.lembed.internal.RxUsbEndpointImpl;


import rx.Observable;

public  abstract class RxUsbEndpoint {


	public static RxUsbEndpoint getInstance(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection, RxUsbInterface rxUsbInterface, UsbEndpoint usbEndpoint) {
		return new RxUsbEndpointImpl(usbDevice, usbDeviceConnection, rxUsbInterface, usbEndpoint);
	}

	public abstract Observable<byte[]> rxWriteBulk(byte[] buffer,int timeout);

	public abstract Observable<byte[]> rxReadBulk(int length,int timeout);

	public abstract Observable<byte[]> rxRead();

	public abstract  Observable<byte[]> rxWrite(byte[] buffer);

	public abstract UsbDevice getNativeDevice();

	public abstract UsbDeviceConnection getNativeDeviceConnection();

	public abstract UsbEndpoint getNativeEndpoint();
}
