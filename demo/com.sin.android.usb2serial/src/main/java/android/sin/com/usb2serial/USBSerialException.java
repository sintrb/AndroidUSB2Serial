package android.sin.com.usb2serial;

import java.io.IOException;

/**
 * Created by Sin on 2016/7/28.
 */
public class USBSerialException extends Exception {

    public USBSerialException() {
    }

    public USBSerialException(String detailMessage) {
        super(detailMessage);
    }

    public USBSerialException(String message, Throwable cause) {
        super(message, cause);
    }

    public USBSerialException(Throwable cause) {
        super(cause);
    }
}
