package android.sin.com.usb2serial;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

/**
 * Created by Sin on 2016/7/27.
 */
public abstract class USBSerialDriver {
    protected UsbManager usbManager;
    protected UsbDevice usbDevice;
    protected UsbDeviceConnection usbDeviceConnection;


}
