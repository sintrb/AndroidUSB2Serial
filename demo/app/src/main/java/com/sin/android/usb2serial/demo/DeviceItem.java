package com.sin.android.usb2serial.demo;

import android.hardware.usb.UsbDevice;

import com.sin.android.usb2serial.USBSerialDriver;

/**
 * Created by Sin on 2016/7/30.
 */
public class DeviceItem {
    public UsbDevice device;
    public USBSerialDriver driver;

    public DeviceItem(UsbDevice device, USBSerialDriver driver) {
        this.device = device;
        this.driver = driver;
    }

    public String getDeviceName() {
        return device.getDeviceName();
    }

    public String getDeviceId() {
        return String.format("0x%04x", device.getDeviceId());
    }

    public String getVendorId() {
        return String.format("0x%04x", device.getVendorId());
    }

    public String getProductId() {
        return String.format("0x%04x", device.getProductId());
    }

    public String getDriverName() {
        return driver == null ? "No Driver" : driver.getClass().getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        else if (o instanceof DeviceItem && o != null) {
            DeviceItem di = (DeviceItem) o;
            return this.device != null && di.device != null && this.device.getDeviceId() != di.device.getDeviceId();
        } else {
            return false;
        }
    }
}
