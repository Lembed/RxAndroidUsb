package org.lembed.internal;


import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.util.Log;

import org.lembed.RxUsbDevice;
import org.lembed.RxUsbEndpoint;
import org.lembed.RxUsbInterface;
import org.lembed.exeception.RxUsbReadExeception;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;


public  class RxUsbDeviceImpl extends RxUsbDevice{

    private final Context context;
    private final UsbDeviceConnection usbDeviceConnection;
    private final UsbDevice usbDevice;


    public RxUsbDeviceImpl(Context context, UsbDeviceConnection usbDeviceConnection, UsbDevice usbDevice) {
        this.context = context;
        this.usbDeviceConnection = usbDeviceConnection;
        this.usbDevice = usbDevice;
    }

    @Override
    public Observable<List<UsbInterface>> rxGetUsbInterface() {
        Observable observable = Observable.create(subscriber-> {
            ArrayList<UsbInterface> list = new ArrayList<>();
            for (int i = 0; i < usbDevice.getInterfaceCount(); i++) {
                list.add(usbDevice.getInterface(i));
            }
            subscriber.onNext(list);
            subscriber.onCompleted();
        });

        return observable;
    }

    private List<UsbEndpoint> getUsbEndpoint(UsbInterface usbInterface) {
        ArrayList<UsbEndpoint> list = new ArrayList<>();
        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            list.add(usbInterface.getEndpoint(i));
        }
        return list;
    }




    private List<UsbEndpoint> getUsbInEndpoint(UsbInterface usbInterface) {
        ArrayList<UsbEndpoint> list = new ArrayList<>();
        for (UsbEndpoint usbEndpoint : getUsbEndpoint(usbInterface)) {
            if (usbEndpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                list.add(usbEndpoint);
            }
        }
        return list;
    }
    @Override
    public Observable<List<RxUsbEndpoint>> rxGetUsbInEndpoint() {
        return rxGetUsbInterface().map(usbInterfaces-> {
            ArrayList<RxUsbEndpoint> list = new ArrayList<>();
            for (UsbInterface uif : usbInterfaces) {
                List<UsbEndpoint> o = getUsbInEndpoint(uif);
                for (UsbEndpoint u : o) {
                    RxUsbInterface rxUsbInterface = RxUsbInterface.getInstance(usbDevice,usbDeviceConnection,uif);
                    RxUsbEndpoint rxUsbEndpoint = RxUsbEndpoint.getInstance(usbDevice, usbDeviceConnection, rxUsbInterface, u);
                    list.add(rxUsbEndpoint);
                }
            }
            return list;
        });
    }

    public List<UsbEndpoint> getUsbOutEndpoint(UsbInterface usbInterface) {
        ArrayList<UsbEndpoint> list = new ArrayList<>();
        for (UsbEndpoint usbEndpoint : getUsbEndpoint(usbInterface)) {
            if (usbEndpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                list.add(usbEndpoint);
            }
        }
        return list;
    }
    @Override
    public Observable<List<RxUsbEndpoint>> rxGetUsbOutEndpoint() {
        return rxGetUsbInterface().map(usbInterfaces-> {

            ArrayList<RxUsbEndpoint> list = new ArrayList<>();
            for (UsbInterface uif : usbInterfaces) {
                List<UsbEndpoint> o = getUsbOutEndpoint(uif);
                for (UsbEndpoint u : o) {
                    RxUsbInterface rxUsbInterface = RxUsbInterface.getInstance(usbDevice,usbDeviceConnection,uif);
                    RxUsbEndpoint rxUsbEndpoint = RxUsbEndpoint.getInstance(usbDevice, usbDeviceConnection, rxUsbInterface, u);
                    list.add(rxUsbEndpoint);
                }
            }

            return list;
        });
    }

    @Override
    public Observable<Integer> rxWriteControl(int requestType, int request, int value, int index, byte[] buffer, int timeout) {
        return Observable.create(subscriber -> {

            showMessage("rxWriteControl");
            int res = usbDeviceConnection.controlTransfer(requestType, request, value, index, buffer, buffer.length, timeout);
            if(res<=0){
                subscriber.onError(new RxUsbReadExeception());
            }
            subscriber.onNext(res);
            subscriber.onCompleted();

        });
    }

    @Override
    public Observable< byte[]> rxReadControl(int requestType, int request, int value, int index, int length, int timeout) {
        Observable observable = Observable.create(subscriber -> {
            byte[] buffer = new byte[length];
            int res = usbDeviceConnection.controlTransfer(requestType, request, value, index, buffer, buffer.length, timeout);
            if(res<=0){
                subscriber.onError(new RxUsbReadExeception());
            }
            subscriber.onNext(buffer);
            subscriber.onCompleted();
        });

        return observable;
    }
    @Override
    public void close(){
        usbDeviceConnection.close();
    }

    @Override
    public int getProductId() {
        return usbDevice.getProductId();
    }

    @Override
    public int getVendorId() {
        return usbDevice.getVendorId();
    }

    @Override
    public UsbDevice getNativeDevice(){
        return usbDevice;
    }

    private void showMessage(String msg){
        Log.e(RxUsbDeviceImpl.class.getSimpleName(),msg);
    }
}
