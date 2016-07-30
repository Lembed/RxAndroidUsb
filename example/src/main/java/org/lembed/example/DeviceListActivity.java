package org.lembed.example;

import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.em.example.R;

import org.lembed.RxAndroidUsb;
import org.lembed.RxUsbDevice;

import java.util.ArrayList;
import java.util.HashMap;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DeviceListActivity extends AppCompatActivity {
    private RecyclerView mListView;
    private UsbAdatper  mAdapter = new UsbAdatper();

    RxAndroidUsb rxAndroidUsb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        rxAndroidUsb =  RxAndroidUsb.getInstance(getApplicationContext());
        mListView = (RecyclerView) findViewById(R.id.deviceList);
        mListView.setHasFixedSize(false);
        mListView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        mListView.setAdapter(mAdapter);
        mAdapter.setOnClickListener(listener);

        scan();

        rxAndroidUsb.rxOnAttached()
        .flatMap(usbDevice -> rxAndroidUsb.rxEstablishConnection(usbDevice))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe((d)->refreshView(d));
    }

    View.OnClickListener listener = new View.OnClickListener () {
        @Override
        public void onClick(View v) {
            final int itemPosition = mListView.getChildAdapterPosition(v);
            RxUsbDevice usbDevice = mAdapter.getItem(itemPosition);
            opActivity(usbDevice);
        }
    };

    private void refreshView(RxUsbDevice usbDevice) {
        mAdapter.addResult(usbDevice);
    }

    private void scan() {

        rxAndroidUsb.rxScanUsbDevice()
        .flatMapIterable(stringUsbDeviceHashMap-> {
            ArrayList<UsbDevice> usbDeviceArrayList = new ArrayList<>();
            for (HashMap.Entry<String, UsbDevice> e : stringUsbDeviceHashMap.entrySet()) {
                usbDeviceArrayList.add(e.getValue());
            }
            return usbDeviceArrayList;
        }).flatMap(usbDevice -> rxAndroidUsb.rxRequestPermission(usbDevice))
        .observeOn(Schedulers.newThread()).subscribe();

    }

    private void opActivity(RxUsbDevice usbDevice) {
        Uapplication.setUsbDevice(usbDevice);
        Intent intent = new Intent();
        intent.setClass(this, OperationActivity.class);
        startActivity(intent);
    }

    private void showMessage(String msg) {
        Log.e(DeviceListActivity.class.getSimpleName(), msg);
    }
}
