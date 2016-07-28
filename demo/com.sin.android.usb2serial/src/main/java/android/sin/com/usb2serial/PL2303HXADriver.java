package android.sin.com.usb2serial;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.util.Log;

/**
 * Created by Sin on 2016/7/28.
 */
public class PL2303HXADriver extends USBSerialDriver {
    private static final String TAG = "PL2303HXADriver";


    /**
     * PL303HXA Product ID
     */
    public static final int PL2303HXA_PRODUCT_ID = 0x2303;


    protected UsbInterface usbInterface;
    private UsbEndpoint uein;
    private UsbEndpoint ueout;
    private boolean opened = false;


    // sender and receiver control
    private static final int MAX_SENDLEN = 1;
    private static final int SECVBUF_LEN = 4096;
    private static final int SENDBUF_LEN = MAX_SENDLEN;

    // buffer
    private byte[] recv_buf = new byte[SECVBUF_LEN];
    private byte[] send_buf = new byte[SENDBUF_LEN];
    private long receivecount = 0, sendcount = 0;

    public PL2303HXADriver(Context context) {
        super(context);
    }


    /**
     * Judge the UsbDevice is correct type.
     *
     * @param device UsbDevice to judge
     * @return ture if correct, else false
     */
    public static boolean isSupportedDevice(UsbDevice device) {
        return device.getProductId() == PL2303HXA_PRODUCT_ID;
    }

    public boolean init(UsbDevice usbDevice) throws USBSerialException {
        if (!isSupportedDevice(usbDevice)) {
            throw new USBSerialException("not a supported UsbDevice");
        }
        this.usbDevice = usbDevice;
        return true;
    }

    public boolean open() throws USBSerialException {
        usbDeviceConnection = usbManager.openDevice(usbDevice);
        if (usbDeviceConnection == null) {
            throw new USBSerialException("open UsbDevice failed");
        }

        Log.i(TAG, "openDevice()=>ok!");
        Log.i(TAG, "getInterfaceCount()=>" + usbDevice.getInterfaceCount());

        usbInterface = usbDevice.getInterface(0);

        for (int i = 0; i < usbInterface.getEndpointCount(); ++i) {
            UsbEndpoint ue = usbInterface.getEndpoint(i);
            if (ue.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK && ue.getDirection() == UsbConstants.USB_DIR_IN) {
                uein = ue;
            } else if (ue.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK && ue.getDirection() == UsbConstants.USB_DIR_OUT) {
                ueout = ue;
            }
        }
        if (uein != null && ueout != null) {
            Log.i(TAG, "get Endpoint ok!");
            usbDeviceConnection.claimInterface(usbInterface, true);
            byte[] buffer = new byte[1];
            pl2303_vendor_read(0x8484, 0, buffer, 1);
            pl2303_vendor_write(0x0404, 0, null, 0);
            pl2303_vendor_read(0x8484, 0, buffer, 1);
            pl2303_vendor_read(0x8383, 0, buffer, 1);
            pl2303_vendor_read(0x8484, 0, buffer, 1);
            pl2303_vendor_write(0x0404, 1, null, 0);
            pl2303_vendor_read(0x8484, 0, buffer, 1);
            pl2303_vendor_read(0x8383, 0, buffer, 1);

            pl2303_vendor_write(0, 1, null, 0);
            pl2303_vendor_write(1, 0, null, 0);

            // not PL2303_QUIRK_LEGACY
            pl2303_vendor_write(2, 0x44, null, 0);

            reset();
            opened = true;
        }
        return true;
    }


    public boolean reset() throws USBSerialException {
        byte[] mPortSetting = new byte[7];
        controlTransfer(0xa1, 0x21, 0, 0, mPortSetting, 7, transferTimeOut);


//        int baseline, mantissa, exponent;
//        int baud = baudRate;
//	/*
//	 * Apparently the formula is:
//	 *   baudrate = 12M * 32 / (mantissa * 4^exponent)
//	 * where
//	 *   mantissa = buf[8:0]
//	 *   exponent = buf[11:9]
//	 */
//        baseline = 12000000 * 32;
//        mantissa = baseline / baud;
//        if (mantissa == 0)
//            mantissa = 1;	/* Avoid dividing by zero if baud > 32*12M. */
//        exponent = 0;
//        while (mantissa >= 512) {
//            if (exponent < 7) {
//                mantissa >>= 2;	/* divide by 4 */
//                exponent++;
//            } else {
//			/* Exponent is maxed. Trim mantissa and leave. */
//                mantissa = 511;
//                break;
//            }
//        }
//
//        mPortSetting[3] = (byte)0x80;
//        mPortSetting[2] = 0;
//        mPortSetting[1] = (byte)(exponent << 1 | mantissa >> 8);
//        mPortSetting[0] = (byte)(mantissa & 0xff);

        mPortSetting[0] = (byte) (baudRate & 0xff);
        mPortSetting[1] = (byte) (baudRate >> 8 & 0xff);
        mPortSetting[2] = (byte) (baudRate >> 16 & 0xff);
        mPortSetting[3] = (byte) (baudRate >> 24 & 0xff);

        mPortSetting[4] = (byte) (stopBit == STOPBIT_1D5 ? 1 : (stopBit == STOPBIT_2 ? 2 : 0));
        mPortSetting[5] = (byte) (parity == PARITY_SPACE ? 4 : (parity == PARITY_MARK ? 3 : (parity == PARITY_EVEN ? 2 : (parity == PARITY_ODD ? 1 : 0))));
        mPortSetting[6] = dataBit;
        controlTransfer(0x21, 0x20, 0, 0, mPortSetting, 7, transferTimeOut);
        controlTransfer(0xa1, 0x21, 0, 0, mPortSetting, 7, transferTimeOut);
        return true;
    }

    public boolean close() throws USBSerialException {
        if (this.opened) {
            if (usbDeviceConnection.releaseInterface(usbInterface))
                Log.i(TAG, "releaseInterface()=>ok!");
            this.usbDeviceConnection = null;
        }
        this.opened = false;
        return true;
    }

    private int pl2303_vendor_read(int value, int index, byte[] buffer, int length) throws USBSerialException {
        return this.controlTransfer(0xC0, 1, value, index, buffer, length, transferTimeOut);
    }

    private int pl2303_vendor_write(int value, int index, byte[] buffer, int length) throws USBSerialException {
        return this.controlTransfer(0x40, 1, value, index, buffer, length, transferTimeOut);
    }

    private int controlTransfer(int requestType, int request, int value, int index, byte[] buffer, int length, int timeout) throws USBSerialException {
        int res = this.usbDeviceConnection.controlTransfer(requestType, request, value, index, buffer, length, timeout);
        if (res < 0) {
            String err = String.format("controlTransfer fail when: %d %d %d %d buffer %d %d", requestType, request, value, index, length, timeout);
            Log.e(TAG, err);
            throw new USBSerialException(err);
        }
        return res;
    }

    public boolean isOpened() {
        return opened;
    }

    public int write(byte data) throws USBSerialException {
        send_buf[0] = data;
        int ret = usbDeviceConnection.bulkTransfer(ueout, send_buf, 1, writeTimeOut);
        ++sendcount;
        return ret;
    }

    private int readix = 0;
    private int readlen = 0;

    public int read() throws USBSerialException {
        int ret = 0;
        if (readix >= readlen) {
            readlen = usbDeviceConnection.bulkTransfer(uein, recv_buf, SECVBUF_LEN, readTimeOut);
            readix = 0;
        }
        if (readix < readlen) {
            ret = (recv_buf[readix] & 0x00ff);
            ++receivecount;
            ++readix;
            return ret;
        } else {
            return -1;
        }
    }
}
