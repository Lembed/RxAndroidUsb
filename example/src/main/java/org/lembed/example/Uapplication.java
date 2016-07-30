package org.lembed.example;

import android.app.Application;

import org.lembed.RxUsbDevice;

import rx.Observable;
import rx.subjects.ReplaySubject;

public class Uapplication extends Application {

   static ReplaySubject<RxUsbDevice> rxUsbDevicePublishSubject = ReplaySubject.create();

    public static  Observable<RxUsbDevice> getUsbDevice() {
        return rxUsbDevicePublishSubject.asObservable();
    }

    public static void setUsbDevice(RxUsbDevice rxUsbDevice) {
        rxUsbDevicePublishSubject.onNext(rxUsbDevice);
    }
}
