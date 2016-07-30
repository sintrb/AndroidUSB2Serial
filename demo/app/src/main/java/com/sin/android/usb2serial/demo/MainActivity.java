package com.sin.android.usb2serial.demo;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.sin.android.sinlibs.activities.BaseActivity;
import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.sin.android.sinlibs.base.Callable;
import com.sin.android.sinlibs.tagtemplate.ViewRender;
import com.sin.android.sinlibs.utils.InjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.sin.android.usb2serial.*;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private TextView tv_log = null;
    private ListView lv_devices = null;


    private static final String ACTION_FOR_PERMISSION = MainActivity.class.getName() + ".ACTION_FOR_PERMISSION";
    private UsbManager usbManager;
    //    private UsbDevice usbDevice;
    BroadcastReceiver permissionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_FOR_PERMISSION.equals(action)) {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    addLog("授权成功!");
                    whenHadPermission();
                } else {
                    addLog("授权失败!");
                }
            }
        }
    };

//    private USBSerialDriver driver = null;

    private DeviceItem deviceItem = null;
    private List<DeviceItem> deviceItems = new ArrayList<DeviceItem>();
    private ViewRender render = new ViewRender();
    private SimpleListAdapter adapter = new SimpleListAdapter(this, deviceItems, new SimpleViewInitor() {

        @Override
        public View initView(Context context, int i, View view, ViewGroup viewGroup, Object o) {
            if (view == null) {
                view = LinearLayout.inflate(MainActivity.this, R.layout.item_device, null);
            }
            view.setBackgroundResource(deviceItem == o ? R.drawable.bg_ligth : 0);
            render.renderView(view, o);
            return view;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
        IntentFilter filter = new IntentFilter(ACTION_FOR_PERMISSION);
        filter.addAction(ACTION_FOR_PERMISSION);
        this.registerReceiver(permissionReceiver, filter);

        InjectUtils.injectViews(this, R.id.class);

        findViewById(R.id.btn_list).setOnClickListener(this);
        findViewById(R.id.btn_open).setOnClickListener(this);
        findViewById(R.id.btn_send).setOnClickListener(this);
        findViewById(R.id.btn_read).setOnClickListener(this);
        findViewById(R.id.btn_test).setOnClickListener(this);
        findViewById(R.id.tv_log).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                tv_log.setText("");
                return false;
            }
        });


        // Devices
        lv_devices.setAdapter(adapter);
        lv_devices.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                deviceItem = deviceItems.get(position);
                adapter.notifyDataSetChanged();
            }
        });

        // driver
        USBSerial.registerDriver(CP210XDriver.ID_TABLE, CP210XDriver.class);
        USBSerial.registerDriver(PL2303Driver.ID_TABLE, PL2303Driver.class);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(permissionReceiver);
        super.onDestroy();
    }

    private void addLog(final CharSequence log) {
        safeCall(new Callable() {
            @Override
            public void call(Object... objects) {
                tv_log.append(log);
                tv_log.append("\r\n");
            }
        });
    }

    @Override
    public void onClick(View v) {
        final USBSerialDriver driver = deviceItem != null ? deviceItem.driver : null;
        final UsbDevice usbDevice = deviceItem != null ? deviceItem.device : null;
        switch (v.getId()) {
            case R.id.btn_list: {
                addLog("List:");
                asynCallAndShowProcessDlg("waitting...", new Callable() {
                    @Override
                    public void call(Object... arg0) {
                        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
                        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                        deviceItems.clear();
                        while (deviceIterator.hasNext()) {
                            UsbDevice d = deviceIterator.next();
                            DeviceItem di= new DeviceItem(d,null);
                            try{
                                di.driver = USBSerial.createDriver(MainActivity.this, d.getVendorId(),d.getProductId());
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                            deviceItems.add(di);
                            addLog(d.getDeviceName());
                        }
                        safeCall(new Callable() {
                            @Override
                            public void call(Object... objects) {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
                break;
            }
            case R.id.btn_open: {
                if (usbDevice == null) {
                    addLog("没有设备");
                } else if (!usbManager.hasPermission(usbDevice)) {
                    PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_FOR_PERMISSION), 0);
                    usbManager.requestPermission(usbDevice, mPermissionIntent);
                } else {
                    addLog("打开设备!");
                    if (usbDevice == null) {
                        addLog("设备不存在!");
                        return;
                    }
                    if(driver==null){
                        addLog("没有合适的驱动!");
                        return;
                    }
                    if (driver.isOpened()) {
                        addLog("设备已打开!!");
                    } else {
                        try {
                            driver.config(115200);
                            driver.init(usbDevice);
                            driver.open();
                        } catch (USBSerialException e) {
                            e.printStackTrace();
                            addLog(e.getMessage());
                        }
                    }
                }
                break;
            }
            case R.id.btn_send: {
                if (driver == null) {
                    addLog("没有设备驱动");
                } else if (!driver.isOpened()) {
                    addLog("设备未打开!");
                } else {
                    asynCall(new Callable() {
                        @Override
                        public void call(Object... objects) {
                            try {
                                int v = driver.write((byte) (System.currentTimeMillis() / 1000));
                                addLog("write:" + v);
                            } catch (USBSerialException e) {
                                e.printStackTrace();
                                addLog(e.getMessage());
                            }
                        }
                    });
                }
                break;
            }
            case R.id.btn_read: {
                if (driver == null) {
                    addLog("没有设备驱动");
                } else if (!driver.isOpened()) {
                    addLog("设备未打开!");
                } else {
                    asynCall(new Callable() {
                        @Override
                        public void call(Object... objects) {
                            try {
                                int v = driver.read();
                                addLog("read:" + v);
                            } catch (USBSerialException e) {
                                e.printStackTrace();
                                addLog(e.getMessage());
                            }
                        }
                    });
                }
                break;
            }
            case R.id.btn_test: {
                if (driver == null) {
                    addLog("没有设备驱动");
                } else if (!driver.isOpened()) {
                    addLog("设备未打开!");
                } else {
                    final long endtime = System.currentTimeMillis() + 10 * 1000;
                    asynCall(new Callable() {
                        @Override
                        public void call(Object... objects) {
                            int count = 0;
                            while (System.currentTimeMillis() < (endtime + 1000)) {
                                try {
                                    int v = driver.read();
                                    if (v > 0) {
                                        ++count;
                                        if (count % 500 == 0)
                                            addLog("read:" + count + "count");
                                    }

                                } catch (USBSerialException e) {
                                    e.printStackTrace();
                                    addLog(e.getMessage());
                                }
                            }
                            addLog("read:" + count + "count, end");
                        }
                    });
                    asynCall(new Callable() {
                        @Override
                        public void call(Object... objects) {
                            int count = 0;
                            while (System.currentTimeMillis() < (endtime)) {
                                try {
                                    int v = driver.write((byte) (System.currentTimeMillis()));
                                    if (v > 0) {
                                        ++count;
                                        if (count % 500 == 0)
                                            addLog("write:" + count + "count");
                                    }
                                } catch (USBSerialException e) {
                                    e.printStackTrace();
                                    addLog(e.getMessage());
                                }
                            }
                            addLog("write:" + count + "count, end");
                        }
                    });
                }
                break;
            }
        }
    }

    private void whenHadPermission() {

    }
}
