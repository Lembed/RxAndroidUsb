package org.lembed.internal;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.util.Log;

import org.lembed.RxUsbInterface;

public  class RxUsbInterfaceImpl extends RxUsbInterface {

    private final UsbDevice usbDevice;
    private final UsbDeviceConnection usbDeviceConnection;
    private final UsbInterface usbInterface;

    public RxUsbInterfaceImpl(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection, UsbInterface usbInterface) {
        this.usbDevice = usbDevice;
        this.usbDeviceConnection = usbDeviceConnection;
        this.usbInterface = usbInterface;

    }

    public Boolean open(){
        return usbDeviceConnection.claimInterface(usbInterface,true);
    }

    public Boolean close(){
        return usbDeviceConnection.releaseInterface(usbInterface);
    }

    public UsbInterface getNativeInterface() {
        return usbInterface;
    }

    private void showMessage(String msg) {
        Log.e(RxUsbInterfaceImpl.class.getSimpleName(), msg);
    }
}
