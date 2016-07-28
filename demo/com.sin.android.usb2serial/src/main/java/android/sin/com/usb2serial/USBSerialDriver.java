package android.sin.com.usb2serial;

import android.content.Context;
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
    private Context context;

    protected int baudRate = 9600; // baudRate bps
    protected byte dataBit = 8;

    final public byte STOPBIT_NONE = 0;
    final public byte STOPBIT_1D5 = 1;   // stop bits = 1.5
    final public byte STOPBIT_2 = 2;   // stop bits = 2
    protected byte stopBit = STOPBIT_NONE;

    final public byte PARITY_NONE = 0;
    final public byte PARITY_MARK = 1;
    final public byte PARITY_ODD = 2;
    final public byte PARITY_SPACE = 3;
    final public byte PARITY_EVEN = 4;
    protected byte parity = PARITY_NONE;

    protected byte flowControl = 0;

    // Timeout Control
    protected static int DefaultTimeOut = 100;   // ms
    protected int transferTimeOut = DefaultTimeOut;
    protected int readTimeOut = DefaultTimeOut;
    protected int writeTimeOut = DefaultTimeOut;

    public int getBaudRate() {
        return baudRate;
    }

    public byte getDataBit() {
        return dataBit;
    }

    public byte getStopBit() {
        return stopBit;
    }

    public byte getParity() {
        return parity;
    }

    public byte getFlowControl() {
        return flowControl;
    }

    public UsbDevice getUsbDevice() {
        return usbDevice;
    }
    public boolean config(int baudRate) throws USBSerialException{
        return  this.config(baudRate,dataBit,stopBit,parity,flowControl);
    }
    public boolean config(int baudRate, byte dataBit, byte stopBit, byte parity, byte flowControl) throws USBSerialException{
        this.baudRate = baudRate;
        this.dataBit = dataBit;
        this.stopBit=stopBit;
        this.parity=parity;
        this.flowControl=flowControl;
        if(isOpened()){
            return this.clear() && this.reset();
        }
        return true;
    }

    public boolean clear(){
        return true;
    }
    abstract public boolean init(UsbDevice usbDevice) throws USBSerialException;
    abstract public boolean open() throws USBSerialException;
    abstract public boolean close() throws USBSerialException;
    abstract public boolean reset() throws USBSerialException;
    abstract public int write(byte data) throws USBSerialException;
    public int write(byte[] datas,int max) throws USBSerialException{
        max = Math.min(datas.length,max);
        int i = 0;
        for(i=0;i<max;++i){
            write(datas[i]);
        }
        return i;
    }
    abstract public int read() throws USBSerialException;
    public int read(byte[] buf,int max) throws USBSerialException{
        max = Math.min(buf.length,max);
        int i = 0;
        for(i=0;i<max;++i){
            int v = read();
            if(v>=0){
                buf[i] = (byte)v;
            }
            else{
                break;
            }
        }
        return i;
    }

    abstract public boolean isOpened();

    public USBSerialDriver(Context context) {
        this.context = context;
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }
}
