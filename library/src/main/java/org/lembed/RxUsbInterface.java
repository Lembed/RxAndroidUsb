package org.lembed;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;

import org.lembed.internal.RxUsbInterfaceImpl;

public  abstract class RxUsbInterface {


	public static RxUsbInterface getInstance(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection, UsbInterface usbInterface) {
		return new RxUsbInterfaceImpl(usbDevice, usbDeviceConnection, usbInterface);
	}


	public abstract UsbInterface getNativeInterface();

	public abstract Boolean open();

	public abstract Boolean close();

}
