package org.lembed.internal;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbRequest;
import android.util.Log;

import org.lembed.RxUsbEndpoint;
import org.lembed.RxUsbInterface;
import org.lembed.exeception.RxUsbReadExeception;
import org.lembed.exeception.RxUsbWriteExeception;

import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

import rx.Observable;
import rx.subscriptions.Subscriptions;

public  class RxUsbEndpointImpl extends RxUsbEndpoint {

    private final UsbDevice usbDevice;
    private final UsbDeviceConnection usbDeviceConnection;
    private final RxUsbInterface rxUsbInterface;
    private final UsbEndpoint usbEndpoint;

    Semaphore semaphore = new Semaphore(1);

    public RxUsbEndpointImpl(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection, RxUsbInterface rxUsbInterface, UsbEndpoint usbEndpoint) {
        this.usbDevice = usbDevice;
        this.usbDeviceConnection = usbDeviceConnection;
        this.rxUsbInterface = rxUsbInterface;
        this.usbEndpoint = usbEndpoint;
    }

    @Override
    public Observable<byte[]> rxWriteBulk(byte[] buffer,int timeout) {
        Observable observable = Observable.create(subscriber -> {

            try {
                semaphore.acquire();
            }catch (InterruptedException e){
                e.printStackTrace();
            }

            if(getDirection() != UsbConstants.USB_DIR_OUT){
                subscriber.onNext(new RxUsbWriteExeception());
            }

            int ret = usbDeviceConnection.bulkTransfer(usbEndpoint,buffer,buffer.length,timeout);
            if(ret<0){
                subscriber.onError(new RxUsbReadExeception());
            }
            subscriber.onNext(buffer);
            subscriber.onCompleted();

            subscriber.add(Subscriptions.create(()->{
                semaphore.release();
            }));
        });
        return observable;
    }

    private int getType() {
        return usbEndpoint.getType();
    }

    private int getDirection() {
        return usbEndpoint.getDirection();
    }

    @Override
    public Observable<byte[]> rxReadBulk(int length,int timeout) {
        Observable observable = Observable.create(subscriber -> {

            try {
                semaphore.acquire();
            }catch (InterruptedException e){
                e.printStackTrace();
            }

            if(getDirection() != UsbConstants.USB_DIR_IN){
                subscriber.onNext(new RxUsbReadExeception());
            }

            byte[] buffer = new byte[length];
            int ret = usbDeviceConnection.bulkTransfer(usbEndpoint,buffer,buffer.length,timeout);
            if(ret<0){
                subscriber.onError(new RxUsbReadExeception());
            }

            subscriber.onNext(buffer);
            subscriber.onCompleted();

            subscriber.add(Subscriptions.create(()->{
                semaphore.release();
            }));

        });
        return observable;
    }

    @Override
    public Observable<byte[]> rxRead() {

        Observable observable = Observable.create(subscriber-> {

            try {
                semaphore.acquire();
            }catch (InterruptedException e){
                e.printStackTrace();
            }

            showMessage("doRead");
            if (usbEndpoint.getDirection() != UsbConstants.USB_DIR_IN) {
                subscriber.onError(new RuntimeException());
            }

            if (usbEndpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_CONTROL) {
                subscriber.onError(new RuntimeException());
            }

            if (!rxUsbInterface.open()) {
                subscriber.onError(new RuntimeException());
            }

            int inMax = usbEndpoint.getMaxPacketSize();
            ByteBuffer byteBuffer = ByteBuffer.allocate(inMax);

            UsbRequest usbRequest = new UsbRequest();
            usbRequest.initialize(usbDeviceConnection, usbEndpoint);
            usbRequest.queue(byteBuffer, inMax);
            if (usbDeviceConnection.requestWait() == usbRequest) {
                byte[] retData = byteBuffer.array();
                subscriber.onNext(retData);
                subscriber.onCompleted();
                showMessage("doRead completed");
            }
            subscriber.add(Subscriptions.create(()->{
                semaphore.release();
                usbRequest.close();
                rxUsbInterface.close();
            }));
        });

        return observable;
    }

    @Override
    public  Observable<byte[]> rxWrite(byte[] data) {

        Observable observable = Observable.create(subscriber-> {
            showMessage("doWrite");

            try {
                semaphore.acquire();
            }catch (InterruptedException e){
                e.printStackTrace();
            }

            if (usbEndpoint.getDirection() != UsbConstants.USB_DIR_OUT) {
                subscriber.onError(new RxUsbWriteExeception());
            }

            if (usbEndpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_CONTROL) {
                subscriber.onError(new RxUsbWriteExeception());
            }

            if (!rxUsbInterface.open()) {
                subscriber.onError(new RuntimeException());
            }

            int outMax = usbEndpoint.getMaxPacketSize();
            int length = outMax < data.length ? outMax : data.length;
            ByteBuffer byteBuffer = ByteBuffer.allocate(length);
            byteBuffer.put(data);
            UsbRequest usbRequest = new UsbRequest();
            usbRequest.initialize(usbDeviceConnection, usbEndpoint);

            usbRequest.queue(byteBuffer, length);
            if (usbDeviceConnection.requestWait() == usbRequest) {
                byte[] retData = byteBuffer.array();
                subscriber.onNext(retData);
                subscriber.onCompleted();
                showMessage("doWrite completed");
            }

            subscriber.add(Subscriptions.create(()->{
                showMessage("doWrite release");
                semaphore.release();
                usbRequest.close();
                rxUsbInterface.close();
            }));
        });

        return observable;
    }


    @Override
    public UsbDevice getNativeDevice() {
        return usbDevice;
    }

    @Override
    public UsbDeviceConnection getNativeDeviceConnection() {
        return usbDeviceConnection;
    }

    @Override
    public UsbEndpoint getNativeEndpoint() {
        return usbEndpoint;
    }


    private void showMessage(String msg) {
        Log.e(RxUsbEndpointImpl.class.getSimpleName(), msg);
    }
}
