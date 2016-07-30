package org.lembed.example;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.em.example.R;

import org.lembed.RxAndroidUsb;
import org.lembed.RxUsbEndpoint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;


public class OperationActivity extends AppCompatActivity {

    private TextView display;
    private EditText editText;

    private Button writeButton,readButton;
    RxAndroidUsb rxAndroidUsb;

    PublishSubject<String> writePublishSubject = PublishSubject.create();
    PublishSubject<String> actionPublishSubject = PublishSubject.create();

    PublishSubject<RxUsbEndpoint> rxUsbEndpointInPublishSubject = PublishSubject.create();
    PublishSubject<RxUsbEndpoint> rxUsbEndpointOutPublishSubject = PublishSubject.create();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rxAndroidUsb =  RxAndroidUsb.getInstance(getApplicationContext());

        display = (TextView) findViewById(R.id.textView1);
        display.setMovementMethod(new ScrollingMovementMethod());

        editText = (EditText)findViewById(R.id.editText);
        editText.setText("Hello World");

        writeButton = (Button) findViewById(R.id.buttonWrite);
        readButton = (Button) findViewById(R.id.buttonRead);

        startup();
    }

    private void startup(){

        Observable.create(subscriber -> {
            writeButton.setOnClickListener(this::onWrite);
            readButton.setOnClickListener(this::onRead);
            subscriber.add(Subscriptions.create(()->{
                writeButton.setOnClickListener(null);
                readButton.setOnClickListener(null);
            }));
        }).observeOn(AndroidSchedulers.mainThread()).subscribe();

        rxAndroidUsb.rxOnDetached()
                .doOnNext(usbDevice -> displayMessage("DETACH: " + usbDevice.toString()))
                .observeOn(Schedulers.newThread()).subscribe();

       actionPublishSubject.asObservable()
               .flatMap(action->rxUsbEndpointInPublishSubject.asObservable())
               .map(rxUsbEndpoint1 -> rxUsbEndpoint1.rxRead())
               .flatMap(observable -> observable)
                .timeout(1000, TimeUnit.MILLISECONDS)
                .onErrorReturn((t)->null)
                .filter((data)->data!=null)
                .doOnNext(bytes -> displayMessage(new String(bytes)))
                .observeOn(Schedulers.newThread())
                .subscribe();

        Uapplication.getUsbDevice()
                .flatMap(rxUsbDevice -> rxUsbDevice.rxGetUsbOutEndpoint())
                .flatMapIterable(rxUsbEndpoints->rxUsbEndpoints)
                .doOnNext(rxUsbEndpoint -> rxUsbEndpointOutPublishSubject.onNext(rxUsbEndpoint))
                .observeOn(Schedulers.newThread()).subscribe();

        Uapplication.getUsbDevice().distinctUntilChanged()
                .flatMap(rxUsbDevice -> rxUsbDevice.rxGetUsbInEndpoint())
                .flatMapIterable(rxUsbEndpoints->rxUsbEndpoints)
                .doOnNext(rxUsbEndpoint -> rxUsbEndpointInPublishSubject.onNext(rxUsbEndpoint))
                .observeOn(Schedulers.newThread()).subscribe();
    }

    private void onWrite(View clicked){
        String msg = editText.getText().toString();
        writePublishSubject.onNext(msg);
    }

    private void onRead(View clicked){
        actionPublishSubject.onNext("action");
    }

    private void displayMessage(String msg) {
        DateFormat df = new SimpleDateFormat("HH:mm");
        String date = df.format(Calendar.getInstance().getTime());

        display.append(date + " : ");
        display.append(msg);
        display.append("\n");
    }

    private void showMessage(String msg) {
        Log.e(OperationActivity.class.getSimpleName(), msg);
    }

}