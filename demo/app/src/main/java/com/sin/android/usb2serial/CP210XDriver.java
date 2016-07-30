package com.sin.android.usb2serial;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sin on 2016/7/30.
 */
public class CP210XDriver extends USBSerialDriver {
    /* Config request types */
    final static int REQTYPE_HOST_TO_INTERFACE = 0x41;
    final static int REQTYPE_INTERFACE_TO_HOST = 0xc1;
    final static int REQTYPE_HOST_TO_DEVICE = 0x40;
    final static int REQTYPE_DEVICE_TO_HOST = 0xc0;

    /* Config request codes */
    final static int CP210X_IFC_ENABLE = 0x00;
    final static int CP210X_SET_BAUDDIV = 0x01;
    final static int CP210X_GET_BAUDDIV = 0x02;
    final static int CP210X_SET_LINE_CTL = 0x03;
    final static int CP210X_GET_LINE_CTL = 0x04;
    final static int CP210X_SET_BREAK = 0x05;
    final static int CP210X_IMM_CHAR = 0x06;
    final static int CP210X_SET_MHS = 0x07;
    final static int CP210X_GET_MDMSTS = 0x08;
    final static int CP210X_SET_XON = 0x09;
    final static int CP210X_SET_XOFF = 0x0A;
    final static int CP210X_SET_EVENTMASK = 0x0B;
    final static int CP210X_GET_EVENTMASK = 0x0C;
    final static int CP210X_SET_CHAR = 0x0D;
    final static int CP210X_GET_CHARS = 0x0E;
    final static int CP210X_GET_PROPS = 0x0F;
    final static int CP210X_GET_COMM_STATUS = 0x10;
    final static int CP210X_RESET = 0x11;
    final static int CP210X_PURGE = 0x12;
    final static int CP210X_SET_FLOW = 0x13;
    final static int CP210X_GET_FLOW = 0x14;
    final static int CP210X_EMBED_EVENTS = 0x15;
    final static int CP210X_GET_EVENTSTATE = 0x16;
    final static int CP210X_SET_CHARS = 0x19;
    final static int CP210X_GET_BAUDRATE = 0x1D;
    final static int CP210X_SET_BAUDRATE = 0x1E;

    /* CP210X_IFC_ENABLE */
    final static int UART_ENABLE = 0x0001;
    final static int UART_DISABLE = 0x0000;

    /* CP210X_(SET|GET)_BAUDDIV */
    final static int BAUD_RATE_GEN_FREQ = 0x384000;

    /* CP210X_(SET|GET)_LINE_CTL */
    final static int BITS_DATA_MASK = 0X0f00;
    final static int BITS_DATA_5 = 0X0500;
    final static int BITS_DATA_6 = 0X0600;
    final static int BITS_DATA_7 = 0X0700;
    final static int BITS_DATA_8 = 0X0800;
    final static int BITS_DATA_9 = 0X0900;

    final static int BITS_PARITY_MASK = 0x00f0;
    final static int BITS_PARITY_NONE = 0x0000;
    final static int BITS_PARITY_ODD = 0x0010;
    final static int BITS_PARITY_EVEN = 0x0020;
    final static int BITS_PARITY_MARK = 0x0030;
    final static int BITS_PARITY_SPACE = 0x0040;

    final static int BITS_STOP_MASK = 0x000f;
    final static int BITS_STOP_1 = 0x0000;
    final static int BITS_STOP_1_5 = 0x0001;
    final static int BITS_STOP_2 = 0x0002;

    /* CP210X_SET_BREAK */
    final static int BREAK_ON = 0x0001;
    final static int BREAK_OFF = 0x0000;

    /* CP210X_(SET_MHS|GET_MDMSTS) */
    final static int CONTROL_DTR = 0x0001;
    final static int CONTROL_RTS = 0x0002;
    final static int CONTROL_CTS = 0x0010;
    final static int CONTROL_DSR = 0x0020;
    final static int CONTROL_RING = 0x0040;
    final static int CONTROL_DCD = 0x0080;
    final static int CONTROL_WRITE_DTR = 0x0100;
    final static int CONTROL_WRITE_RTS = 0x0200;

    final public static USBSerialType[] ID_TABLE = new USBSerialType[]{//
            USB_DEVICE(0x045B, 0x0053), /* Renesas RX610 RX-Stick */
            USB_DEVICE(0x0471, 0x066A), /* AKTAKOM ACE-1001 cable */
            USB_DEVICE(0x0489, 0xE000), /* Pirelli Broadband S.p.A, DP-L10 SIP/GSM Mobile */
            USB_DEVICE(0x0489, 0xE003), /* Pirelli Broadband S.p.A, DP-L10 SIP/GSM Mobile */
            USB_DEVICE(0x0745, 0x1000), /* CipherLab USB CCD Barcode Scanner 1000 */
            USB_DEVICE(0x0846, 0x1100), /* NetGear Managed Switch M4100 series, M5300 series, M7100 series */
            USB_DEVICE(0x08e6, 0x5501), /* Gemalto Prox-PU/CU contactless smartcard reader */
            USB_DEVICE(0x08FD, 0x000A), /* Digianswer A/S , ZigBee/802.15.4 MAC Device */
            USB_DEVICE(0x0908, 0x01FF), /* Siemens RUGGEDCOM USB Serial Console */
            USB_DEVICE(0x0BED, 0x1100), /* MEI (TM) Cashflow-SC Bill/Voucher Acceptor */
            USB_DEVICE(0x0BED, 0x1101), /* MEI series 2000 Combo Acceptor */
            USB_DEVICE(0x0FCF, 0x1003), /* Dynastream ANT development board */
            USB_DEVICE(0x0FCF, 0x1004), /* Dynastream ANT2USB */
            USB_DEVICE(0x0FCF, 0x1006), /* Dynastream ANT development board */
            USB_DEVICE(0x0FDE, 0xCA05), /* OWL Wireless Electricity Monitor CM-160 */
            USB_DEVICE(0x10A6, 0xAA26), /* Knock-off DCU-11 cable */
            USB_DEVICE(0x10AB, 0x10C5), /* Siemens MC60 Cable */
            USB_DEVICE(0x10B5, 0xAC70), /* Nokia CA-42 USB */
            USB_DEVICE(0x10C4, 0x0F91), /* Vstabi */
            USB_DEVICE(0x10C4, 0x1101), /* Arkham Technology DS101 Bus Monitor */
            USB_DEVICE(0x10C4, 0x1601), /* Arkham Technology DS101 Adapter */
            USB_DEVICE(0x10C4, 0x800A), /* SPORTident BSM7-D-USB main station */
            USB_DEVICE(0x10C4, 0x803B), /* Pololu USB-serial converter */
            USB_DEVICE(0x10C4, 0x8044), /* Cygnal Debug Adapter */
            USB_DEVICE(0x10C4, 0x804E), /* Software Bisque Paramount ME build-in converter */
            USB_DEVICE(0x10C4, 0x8053), /* Enfora EDG1228 */
            USB_DEVICE(0x10C4, 0x8054), /* Enfora GSM2228 */
            USB_DEVICE(0x10C4, 0x8066), /* Argussoft In-System Programmer */
            USB_DEVICE(0x10C4, 0x806F), /* IMS USB to RS422 Converter Cable */
            USB_DEVICE(0x10C4, 0x807A), /* Crumb128 board */
            USB_DEVICE(0x10C4, 0x80C4), /* Cygnal Integrated Products, Inc., Optris infrared thermometer */
            USB_DEVICE(0x10C4, 0x80CA), /* Degree Controls Inc */
            USB_DEVICE(0x10C4, 0x80DD), /* Tracient RFID */
            USB_DEVICE(0x10C4, 0x80F6), /* Suunto sports instrument */
            USB_DEVICE(0x10C4, 0x8115), /* Arygon NFC/Mifare Reader */
            USB_DEVICE(0x10C4, 0x813D), /* Burnside Telecom Deskmobile */
            USB_DEVICE(0x10C4, 0x813F), /* Tams Master Easy Control */
            USB_DEVICE(0x10C4, 0x814A), /* West Mountain Radio RIGblaster P&P */
            USB_DEVICE(0x10C4, 0x814B), /* West Mountain Radio RIGtalk */
            USB_DEVICE(0x2405, 0x0003), /* West Mountain Radio RIGblaster Advantage */
            USB_DEVICE(0x10C4, 0x8156), /* B&G H3000 link cable */
            USB_DEVICE(0x10C4, 0x815E), /* Helicomm IP-Link 1220-DVM */
            USB_DEVICE(0x10C4, 0x815F), /* Timewave HamLinkUSB */
            USB_DEVICE(0x10C4, 0x818B), /* AVIT Research USB to TTL */
            USB_DEVICE(0x10C4, 0x819F), /* MJS USB Toslink Switcher */
            USB_DEVICE(0x10C4, 0x81A6), /* ThinkOptics WavIt */
            USB_DEVICE(0x10C4, 0x81A9), /* Multiplex RC Interface */
            USB_DEVICE(0x10C4, 0x81AC), /* MSD Dash Hawk */
            USB_DEVICE(0x10C4, 0x81AD), /* INSYS USB Modem */
            USB_DEVICE(0x10C4, 0x81C8), /* Lipowsky Industrie Elektronik GmbH, Baby-JTAG */
            USB_DEVICE(0x10C4, 0x81D7), /* IAI Corp. RCB-CV-USB USB to RS485 Adaptor */
            USB_DEVICE(0x10C4, 0x81E2), /* Lipowsky Industrie Elektronik GmbH, Baby-LIN */
            USB_DEVICE(0x10C4, 0x81E7), /* Aerocomm Radio */
            USB_DEVICE(0x10C4, 0x81E8), /* Zephyr Bioharness */
            USB_DEVICE(0x10C4, 0x81F2), /* C1007 HF band RFID controller */
            USB_DEVICE(0x10C4, 0x8218), /* Lipowsky Industrie Elektronik GmbH, HARP-1 */
            USB_DEVICE(0x10C4, 0x822B), /* Modem EDGE(GSM) Comander 2 */
            USB_DEVICE(0x10C4, 0x826B), /* Cygnal Integrated Products, Inc., Fasttrax GPS demonstration module */
            USB_DEVICE(0x10C4, 0x8281), /* Nanotec Plug & Drive */
            USB_DEVICE(0x10C4, 0x8293), /* Telegesis ETRX2USB */
            USB_DEVICE(0x10C4, 0x82F4), /* Starizona MicroTouch */
            USB_DEVICE(0x10C4, 0x82F9), /* Procyon AVS */
            USB_DEVICE(0x10C4, 0x8341), /* Siemens MC35PU GPRS Modem */
            USB_DEVICE(0x10C4, 0x8382), /* Cygnal Integrated Products, Inc. */
            USB_DEVICE(0x10C4, 0x83A8), /* Amber Wireless AMB2560 */
            USB_DEVICE(0x10C4, 0x83D8), /* DekTec DTA Plus VHF/UHF Booster/Attenuator */
            USB_DEVICE(0x10C4, 0x8411), /* Kyocera GPS Module */
            USB_DEVICE(0x10C4, 0x8418), /* IRZ Automation Teleport SG-10 GSM/GPRS Modem */
            USB_DEVICE(0x10C4, 0x846E), /* BEI USB Sensor Interface (VCP) */
            USB_DEVICE(0x10C4, 0x8477), /* Balluff RFID */
            USB_DEVICE(0x10C4, 0x84B6), /* Starizona Hyperion */
            USB_DEVICE(0x10C4, 0x85EA), /* AC-Services IBUS-IF */
            USB_DEVICE(0x10C4, 0x85EB), /* AC-Services CIS-IBUS */
            USB_DEVICE(0x10C4, 0x85F8), /* Virtenio Preon32 */
            USB_DEVICE(0x10C4, 0x8664), /* AC-Services CAN-IF */
            USB_DEVICE(0x10C4, 0x8665), /* AC-Services OBD-IF */
            USB_DEVICE(0x10C4, 0x8856),	/* CEL EM357 ZigBee USB Stick - LR */
            USB_DEVICE(0x10C4, 0x8857),	/* CEL EM357 ZigBee USB Stick */
            USB_DEVICE(0x10C4, 0x88A4), /* MMB Networks ZigBee USB Device */
            USB_DEVICE(0x10C4, 0x88A5), /* Planet Innovation Ingeni ZigBee USB Device */
            USB_DEVICE(0x10C4, 0x8946), /* Ketra N1 Wireless Interface */
            USB_DEVICE(0x10C4, 0x8977),	/* CEL MeshWorks DevKit Device */
            USB_DEVICE(0x10C4, 0x8998), /* KCF Technologies PRN */
            USB_DEVICE(0x10C4, 0x8A2A), /* HubZ dual ZigBee and Z-Wave dongle */
            USB_DEVICE(0x10C4, 0xEA60), /* Silicon Labs factory default */
            USB_DEVICE(0x10C4, 0xEA61), /* Silicon Labs factory default */
            USB_DEVICE(0x10C4, 0xEA70), /* Silicon Labs factory default */
            USB_DEVICE(0x10C4, 0xEA71), /* Infinity GPS-MIC-1 Radio Monophone */
            USB_DEVICE(0x10C4, 0xF001), /* Elan Digital Systems USBscope50 */
            USB_DEVICE(0x10C4, 0xF002), /* Elan Digital Systems USBwave12 */
            USB_DEVICE(0x10C4, 0xF003), /* Elan Digital Systems USBpulse100 */
            USB_DEVICE(0x10C4, 0xF004), /* Elan Digital Systems USBcount50 */
            USB_DEVICE(0x10C5, 0xEA61), /* Silicon Labs MobiData GPRS USB Modem */
            USB_DEVICE(0x10CE, 0xEA6A), /* Silicon Labs MobiData GPRS USB Modem 100EU */
            USB_DEVICE(0x12B8, 0xEC60), /* Link G4 ECU */
            USB_DEVICE(0x12B8, 0xEC62), /* Link G4+ ECU */
            USB_DEVICE(0x13AD, 0x9999), /* Baltech card reader */
            USB_DEVICE(0x1555, 0x0004), /* Owen AC4 USB-RS485 Converter */
            USB_DEVICE(0x166A, 0x0201), /* Clipsal 5500PACA C-Bus Pascal Automation Controller */
            USB_DEVICE(0x166A, 0x0301), /* Clipsal 5800PC C-Bus Wireless PC Interface */
            USB_DEVICE(0x166A, 0x0303), /* Clipsal 5500PCU C-Bus USB interface */
            USB_DEVICE(0x166A, 0x0304), /* Clipsal 5000CT2 C-Bus Black and White Touchscreen */
            USB_DEVICE(0x166A, 0x0305), /* Clipsal C-5000CT2 C-Bus Spectrum Colour Touchscreen */
            USB_DEVICE(0x166A, 0x0401), /* Clipsal L51xx C-Bus Architectural Dimmer */
            USB_DEVICE(0x166A, 0x0101), /* Clipsal 5560884 C-Bus Multi-room Audio Matrix Switcher */
            USB_DEVICE(0x16C0, 0x09B0), /* Lunatico Seletek */
            USB_DEVICE(0x16C0, 0x09B1), /* Lunatico Seletek */
            USB_DEVICE(0x16D6, 0x0001), /* Jablotron serial interface */
            USB_DEVICE(0x16DC, 0x0010), /* W-IE-NE-R Plein & Baus GmbH PL512 Power Supply */
            USB_DEVICE(0x16DC, 0x0011), /* W-IE-NE-R Plein & Baus GmbH RCM Remote Control for MARATON Power Supply */
            USB_DEVICE(0x16DC, 0x0012), /* W-IE-NE-R Plein & Baus GmbH MPOD Multi Channel Power Supply */
            USB_DEVICE(0x16DC, 0x0015), /* W-IE-NE-R Plein & Baus GmbH CML Control, Monitoring and Data Logger */
            USB_DEVICE(0x17A8, 0x0001), /* Kamstrup Optical Eye/3-wire */
            USB_DEVICE(0x17A8, 0x0005), /* Kamstrup M-Bus Master MultiPort 250D */
            USB_DEVICE(0x17F4, 0xAAAA), /* Wavesense Jazz blood glucose meter */
            USB_DEVICE(0x1843, 0x0200), /* Vaisala USB Instrument Cable */
            USB_DEVICE(0x18EF, 0xE00F), /* ELV USB-I2C-Interface */
            USB_DEVICE(0x18EF, 0xE025), /* ELV Marble Sound Board 1 */
            USB_DEVICE(0x1901, 0x0190), /* GE B850 CP2105 Recorder interface */
            USB_DEVICE(0x1901, 0x0193), /* GE B650 CP2104 PMC interface */
            USB_DEVICE(0x1901, 0x0194),	/* GE Healthcare Remote Alarm Box */
            USB_DEVICE(0x19CF, 0x3000), /* Parrot NMEA GPS Flight Recorder */
            USB_DEVICE(0x1ADB, 0x0001), /* Schweitzer Engineering C662 Cable */
            USB_DEVICE(0x1B1C, 0x1C00), /* Corsair USB Dongle */
            USB_DEVICE(0x1BA4, 0x0002),	/* Silicon Labs 358x factory default */
            USB_DEVICE(0x1BE3, 0x07A6), /* WAGO 750-923 USB Service Cable */
            USB_DEVICE(0x1D6F, 0x0010), /* Seluxit ApS RF Dongle */
            USB_DEVICE(0x1E29, 0x0102), /* Festo CPX-USB */
            USB_DEVICE(0x1E29, 0x0501), /* Festo CMSP */
            USB_DEVICE(0x1FB9, 0x0100), /* Lake Shore Model 121 Current Source */
            USB_DEVICE(0x1FB9, 0x0200), /* Lake Shore Model 218A Temperature Monitor */
            USB_DEVICE(0x1FB9, 0x0201), /* Lake Shore Model 219 Temperature Monitor */
            USB_DEVICE(0x1FB9, 0x0202), /* Lake Shore Model 233 Temperature Transmitter */
            USB_DEVICE(0x1FB9, 0x0203), /* Lake Shore Model 235 Temperature Transmitter */
            USB_DEVICE(0x1FB9, 0x0300), /* Lake Shore Model 335 Temperature Controller */
            USB_DEVICE(0x1FB9, 0x0301), /* Lake Shore Model 336 Temperature Controller */
            USB_DEVICE(0x1FB9, 0x0302), /* Lake Shore Model 350 Temperature Controller */
            USB_DEVICE(0x1FB9, 0x0303), /* Lake Shore Model 371 AC Bridge */
            USB_DEVICE(0x1FB9, 0x0400), /* Lake Shore Model 411 Handheld Gaussmeter */
            USB_DEVICE(0x1FB9, 0x0401), /* Lake Shore Model 425 Gaussmeter */
            USB_DEVICE(0x1FB9, 0x0402), /* Lake Shore Model 455A Gaussmeter */
            USB_DEVICE(0x1FB9, 0x0403), /* Lake Shore Model 475A Gaussmeter */
            USB_DEVICE(0x1FB9, 0x0404), /* Lake Shore Model 465 Three Axis Gaussmeter */
            USB_DEVICE(0x1FB9, 0x0600), /* Lake Shore Model 625A Superconducting MPS */
            USB_DEVICE(0x1FB9, 0x0601), /* Lake Shore Model 642A Magnet Power Supply */
            USB_DEVICE(0x1FB9, 0x0602), /* Lake Shore Model 648 Magnet Power Supply */
            USB_DEVICE(0x1FB9, 0x0700), /* Lake Shore Model 737 VSM Controller */
            USB_DEVICE(0x1FB9, 0x0701), /* Lake Shore Model 776 Hall Matrix */
            USB_DEVICE(0x2626, 0xEA60), /* Aruba Networks 7xxx USB Serial Console */
            USB_DEVICE(0x3195, 0xF190), /* Link Instruments MSO-19 */
            USB_DEVICE(0x3195, 0xF280), /* Link Instruments MSO-28 */
            USB_DEVICE(0x3195, 0xF281), /* Link Instruments MSO-28 */
            USB_DEVICE(0x413C, 0x9500), /* DW700 GPS USB interface */
    };

    public CP210XDriver(Context context) {
        super(context);
    }

    int bInterfaceNumber = 0;

    private int cp210x_read_reg_block(int req, byte[] buffer, int length) throws USBSerialException {
        int result = usb_control_msg(req, REQTYPE_INTERFACE_TO_HOST, 0, bInterfaceNumber, buffer, length, transferTimeOut, false);
        if (result != length) {
//            throw new USBSerialException("cp210x_read_reg_block:" + result);
        }
        return result;
    }

    private int cp210x_read_u32_reg(int req) throws USBSerialException {
        byte[] buf = new byte[4];
        int r = cp210x_read_reg_block(req, buf, buf.length);
        return ((buf[0] & 0x00ff) | (((buf[1] & 0x00ff)) << 8) | (((buf[2] & 0x00ff)) << 16) | (((buf[3] & 0x00ff)) << 24));
    }

    private int cp210x_read_u16_reg(int req) throws USBSerialException {
        byte[] buf = new byte[2];
        int r = cp210x_read_reg_block(req, buf, buf.length);
        return ((buf[0] & 0x00ff) | (((buf[1] & 0x00ff)) << 8));
    }

    private int cp210x_read_u8_reg(int req) throws USBSerialException {
        byte[] buf = new byte[1];
        int r = cp210x_read_reg_block(req, buf, buf.length);
        return (buf[0] & 0x00ff);
    }

    private int cp210x_write_reg_block(int req, byte[] buffer, int length) throws USBSerialException {
        return usb_control_msg(req, REQTYPE_HOST_TO_INTERFACE, 0, bInterfaceNumber, buffer, length, transferTimeOut);
    }

    private int cp210x_write_u32_reg(int req, int val) throws USBSerialException {
        byte[] buf = new byte[]{
                (byte) (val & 0x00ff),
                (byte) ((val >> 8) & 0x00ff),
                (byte) ((val >> 16) & 0x00ff),
                (byte) ((val >> 24) & 0x00ff),
        };
        return cp210x_write_reg_block(req, buf, buf.length);
    }

    private int cp210x_write_u16_reg(int req, int val) throws USBSerialException {
        return usb_control_msg(req, REQTYPE_HOST_TO_INTERFACE, val, bInterfaceNumber, new byte[]{1}, 0, transferTimeOut);
    }

    @Override
    public boolean init(UsbDevice usbDevice) throws USBSerialException {
        boolean ret = super.init(usbDevice);
        if (ret) {
            int r = cp210x_read_u16_reg(CP210X_GET_LINE_CTL);
        }
        return ret;
    }

    @Override
    public boolean open() throws USBSerialException {
        cp210x_write_u16_reg(CP210X_IFC_ENABLE, UART_ENABLE);
        this.reset();
        this.setOpened(true);
        return true;
    }

    @Override
    public boolean reset() throws USBSerialException {
        cp210x_write_u32_reg(CP210X_SET_BAUDRATE, baudRate);
        return true;
    }
}
