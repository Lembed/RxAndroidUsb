## Rxjava style wrapped Android usb host library

Android usb host library with rxjava support

[![RxAndroidUsb](https://img.shields.io/badge/build-passing-blue.svg)]()
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/Lembed/RxAndroidUsb/master/LICENSE)
[![RxAndroidUsb](https://img.shields.io/badge/version-1.0-yellow.svg)]()

## Usage

```java
RxAndroidUsb  rxAndroidUsb = RxAndroidUsb.getInstance(getApplicationContext());
rxAndroidUsb
  .rxOnDetached()
  .doOnNext(usbDevice -> displayMessage("DETACH: " + usbDevice.toString()))
  .observeOn(Schedulers.newThread()).subscribe();
```

## License
[MIT](https://github.com/Lembed/RxAndroidUsb/blob/master/LICENSE)
