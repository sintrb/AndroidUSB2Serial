package android.sin.com.usb2serial;

import android.app.PendingIntent;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import java.util.ArrayList;

/**
 * Created by Sin on 2016/7/28.
 *
 * https://github.com/qq282629379/CH340-Android-/blob/5c18efd356f5a2661880ec646c2c525057c272dc/src/main/java/com/won/usb_ch340/CH340AndroidDriver.java
 */
public class CH340Driver extends USBSerialDriver{
    public static String TAG = "CH340Driver";

    public CH340Driver(Context context) {
        super(context);
    }

    @Override
    public boolean init(UsbDevice usbDevice) throws USBSerialException {
        return false;
    }

    @Override
    public boolean open() throws USBSerialException {
        return false;
    }

    @Override
    public boolean close() throws USBSerialException {
        return false;
    }

    @Override
    public boolean reset() throws USBSerialException {
        return false;
    }

    @Override
    public int write(byte data) throws USBSerialException {
        return 0;
    }

    @Override
    public int read() throws USBSerialException {
        return 0;
    }

    @Override
    public boolean isOpened() {
        return false;
    }
}
