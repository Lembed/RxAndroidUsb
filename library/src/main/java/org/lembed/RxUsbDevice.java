package org.lembed;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;

import org.lembed.internal.RxUsbDeviceImpl;

import java.util.List;
import rx.Observable;

public abstract class RxUsbDevice {

    public static RxUsbDevice getInstance(Context context, UsbDeviceConnection usbDeviceConnection, UsbDevice usbDevice) {
        return new RxUsbDeviceImpl(context, usbDeviceConnection, usbDevice);
    }

    public abstract Observable<List<UsbInterface>> rxGetUsbInterface();

    public abstract Observable<List<RxUsbEndpoint>> rxGetUsbInEndpoint();

    public abstract Observable<List<RxUsbEndpoint>> rxGetUsbOutEndpoint();

    public abstract Observable<Integer> rxWriteControl(int requestType, int request, int value, int index, byte[] buffer, int timeout);

    public abstract Observable<byte[]> rxReadControl(int requestType, int request, int value, int index,int length, int timeout);

    public abstract int getProductId();

    public abstract int getVendorId();

    public abstract UsbDevice getNativeDevice();

    public abstract void close();
}
