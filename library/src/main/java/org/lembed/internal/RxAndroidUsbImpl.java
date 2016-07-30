package org.lembed.internal;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v4.util.Pair;
import android.util.Log;

import org.lembed.RxAndroidUsb;
import org.lembed.RxUsbDevice;
import org.lembed.RxUsbMessage;
import org.lembed.exeception.RxPermissionExeception;
import org.lembed.exeception.RxUsbExeception;

import java.util.HashMap;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.ReplaySubject;
import rx.subscriptions.Subscriptions;

public class RxAndroidUsbImpl extends RxAndroidUsb {

    private final Context context;
    ReplaySubject<UsbDevice> attachedPublishSubject = ReplaySubject.create();
    ReplaySubject<UsbDevice> detachedPublishSubject = ReplaySubject.create();

    public RxAndroidUsbImpl(Context context) {
        this.context = context;

        rxOnUsbDevicePlugEvent()
        .onErrorReturn(e->null)
        .filter(p->p != null)
        .map(usbDeviceStringPair -> {
            if (usbDeviceStringPair.second == RxUsbMessage.ACTION_USB_PERMISSION) {
                attachedPublishSubject.onNext(usbDeviceStringPair.first);
            }
            if (usbDeviceStringPair.second == RxUsbMessage.ACTION_USB_DETACHED) {
                detachedPublishSubject.onNext(usbDeviceStringPair.first);
            }
            return usbDeviceStringPair;
        }).observeOn(Schedulers.newThread())
        .subscribe();
    }

    private   Observable<Pair<UsbDevice, String>> rxOnUsbDevicePlugEvent() {
        return Observable.create(subscriber-> {
            final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();

                    if (RxUsbMessage.ACTION_USB_PERMISSION.equals(action)) {
                        synchronized (this) {
                            UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                if (device != null) {
                                    subscriber.onNext(new Pair<UsbDevice, String>(device, action));
                                }else{
                                    subscriber.onError(new RxUsbExeception());
                                }
                            } else {
                                subscriber.onError(new RxPermissionExeception());
                            }
                        }
                    }

                    if (RxUsbMessage.ACTION_USB_DETACHED.equals(action)) {
                        UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (device != null) {
                            subscriber.onNext(new Pair<UsbDevice, String>(device, action));
                        }
                    }

                    if (RxUsbMessage.ACTION_USB_ATTACHED.equals(action)) {
                        UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (device != null) {
                            UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
                            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(RxUsbMessage.ACTION_USB_PERMISSION), PendingIntent.FLAG_UPDATE_CURRENT);
                            manager.requestPermission(device, mPermissionIntent);
                        }
                    }
                }
            };

            IntentFilter filter = new IntentFilter();
            filter.addAction(RxUsbMessage.ACTION_USB_ATTACHED);
            filter.addAction(RxUsbMessage.ACTION_USB_DETACHED);
            filter.addAction(RxUsbMessage.ACTION_USB_PERMISSION);

            context.registerReceiver(broadcastReceiver, filter);

            subscriber.add(Subscriptions.create(()->context.unregisterReceiver(broadcastReceiver)));
        });
    }

    @Override
    public  Observable<UsbDevice> rxOnDetached() {
        return detachedPublishSubject.asObservable();
    }

    @Override
    public  Observable<UsbDevice> rxOnAttached() {
        return attachedPublishSubject.asObservable();
    }

    @Override
    public Observable<HashMap<String, UsbDevice>> rxScanUsbDevice() {
        return Observable.create(subscriber-> {
            UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> list = manager.getDeviceList();
            subscriber.onNext(list);
        });
    }

    @Override
    public Observable<Void> rxRequestPermission(UsbDevice usbDevice) {
        return Observable.create(subscriber-> {
            UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(RxUsbMessage.ACTION_USB_PERMISSION), PendingIntent.FLAG_UPDATE_CURRENT);
            manager.requestPermission(usbDevice, mPermissionIntent);
            subscriber.onNext(null);
            subscriber.onCompleted();
        });
    }

    @Override
    public Observable<RxUsbDevice> rxEstablishConnection(UsbDevice usbDevice) {
        Observable observable = Observable.create(subscriber-> {
            UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            UsbDeviceConnection usbDeviceConnection = manager.openDevice(usbDevice);

            if (usbDeviceConnection == null) {
                subscriber.onError(new Exception());
            } else {
                RxUsbDevice rxUsbDevice =  RxUsbDevice.getInstance(context, usbDeviceConnection, usbDevice);
                subscriber.onNext(rxUsbDevice);
                subscriber.onCompleted();
            }
        });

        return observable;
    }


    private void showMessage(String msg) {
        Log.e(RxAndroidUsb.class.getSimpleName(), msg);
    }
}
