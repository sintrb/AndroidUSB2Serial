package com.sin.android.usb2serial;

/**
 * Created by Sin on 2016/7/30.
 */
public class USBSerialType {
    public int vendor_id = 0;
    public int product_id = 0;
    public int driver_info = 0;
    Class<? extends USBSerialDriver> driveClass = null;

    public USBSerialType(int vendor_id, int product_id) {
        this.vendor_id = vendor_id;
        this.product_id = product_id;
    }

    public USBSerialType setDriverInfo(int driver_info) {
        this.driver_info = driver_info;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof USBSerialType) {
            USBSerialType b = (USBSerialType) o;
            return this.vendor_id == b.vendor_id && this.product_id == b.product_id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return vendor_id ^ product_id;
    }


    @Override
    public String toString() {
        return String.format("%04x:%04x", vendor_id, product_id);
    }
}
