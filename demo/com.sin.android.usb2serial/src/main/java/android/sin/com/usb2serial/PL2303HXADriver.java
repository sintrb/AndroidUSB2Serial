package android.sin.com.usb2serial;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
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

    // Time Control
    private static int TimeOut = 100;   // ms
    private int transferTimeOut = TimeOut;
    private int readTimeOut = TimeOut;
    private int writeTimeOut = TimeOut;

    private Context context;

    private UsbInterface usbInterface;
    private UsbEndpoint uein;
    private UsbEndpoint ueout;
    private boolean opened = false;
    private int baudRate = 115200; // baudRate bps

    // sender and receiver control
    private static final int MAX_SENDLEN = 1;
    private static final int SECVBUF_LEN = 4096;
    private static final int SENDBUF_LEN = MAX_SENDLEN;

    // buffer
    private byte[] recv_buf = new byte[SECVBUF_LEN];
    private byte[] send_buf = new byte[SENDBUF_LEN];
    private long receivecount = 0, sendcount = 0;



    /**
     * Judge the UsbDevice is correct type.
     *
     * @param device UsbDevice to judge
     * @return ture if correct, else false
     */
    public static boolean isSupportedDevice(UsbDevice device) {
        return device.getProductId() == PL2303HXA_PRODUCT_ID;
    }

    public boolean initDriver(UsbDevice usbDevice) throws USBSerialException {
        if (!isSupportedDevice(usbDevice)) {
            throw new USBSerialException("not a supported UsbDevice");
        }
        this.usbDevice = usbDevice;
        return true;
    }

    public boolean resetDriver() throws USBSerialException {
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
            controlTransfer(192, 1, 33924, 0, buffer, 1, transferTimeOut);
            controlTransfer(64, 1, 1028, 0, null, 0, transferTimeOut);
            controlTransfer(192, 1, 33924, 0, buffer, 1, transferTimeOut);
            controlTransfer(192, 1, 33667, 0, buffer, 1, transferTimeOut);
            controlTransfer(192, 1, 33924, 0, buffer, 1, transferTimeOut);
            controlTransfer(64, 1, 1028, 1, null, 0, transferTimeOut);
            controlTransfer(192, 1, 33924, 0, buffer, 1, transferTimeOut);
            controlTransfer(192, 1, 33667, 0, buffer, 1, transferTimeOut);
            controlTransfer(64, 1, 0, 1, null, 0, transferTimeOut);
            controlTransfer(64, 1, 1, 0, null, 0, transferTimeOut);
            controlTransfer(64, 1, 2, 68, null, 0, transferTimeOut);
            reset();
            opened = true;
        }
        return true;
    }

    public void reset() throws USBSerialException {
        byte[] mPortSetting = new byte[7];
        controlTransfer(161, 33, 0, 0, mPortSetting, 7, transferTimeOut);
        mPortSetting[0] = (byte) (baudRate & 0xff);
        mPortSetting[1] = (byte) (baudRate >> 8 & 0xff);
        mPortSetting[2] = (byte) (baudRate >> 16 & 0xff);
        mPortSetting[3] = (byte) (baudRate >> 24 & 0xff);
        mPortSetting[4] = 0;
        mPortSetting[5] = 0;
        mPortSetting[6] = 8;
        controlTransfer(33, 32, 0, 0, mPortSetting, 7, transferTimeOut);
        controlTransfer(161, 33, 0, 0, mPortSetting, 7, transferTimeOut);
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

    public int write(byte data) {
        send_buf[0] = data;
        int ret = usbDeviceConnection.bulkTransfer(ueout, send_buf, 1, writeTimeOut);
        ++sendcount;
        return ret;
    }

    public int write(byte[] datas) {
        int ret = usbDeviceConnection.bulkTransfer(ueout, datas, datas.length, writeTimeOut);
        sendcount += ret;
        return ret;
    }

    private int readix = 0;
    private int readlen = 0;
    public int read() {
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

    public int getReadTimeOut() {
        return readTimeOut;
    }

    public void setReadTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
    }

    public int getWriteTimeOut() {
        return writeTimeOut;
    }

    public void setWriteTimeOut(int writeTimeOut) {
        this.writeTimeOut = writeTimeOut;
    }

    public void setBaudRate(int baudRate) throws USBSerialException {
        this.baudRate = baudRate;
        if (this.isOpened()) {
            this.reset();
        }
    }
}
