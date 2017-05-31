package com.example.zxj.bttest;

/**
 * Created by ZXJ on 2017/5/31.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceListAdapter extends Activity {
    int list_select_index = 0;
    private DeviceListAdapter.DeviceListAdapter1 list_cell_0;
    BluetoothAdapter apter;
    Context context;
    int scan_int = 0;
    int ip = 0;
    public String ibeacon_UUID = "";
    public String ibeacon_MAJOR = "";
    public String ibeacon_MINOR = "";
    public byte sensor_temp;
    public byte sensor_humid;
    public byte sensor_batt;
    public byte[] sensor_VID;
    public JDY_type DEV_TYPE;
    Timer timer = new Timer();
    boolean stop_timer = true;
    byte dev_VID = -120;
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if(msg.what == 1 && DeviceListAdapter.this.stop_timer) {
                DeviceListAdapter.this.loop_list();
            }

            super.handleMessage(msg);
        }
    };
    TimerTask task = new TimerTask() {
        public void run() {
            Message message = new Message();
            message.what = 1;
            DeviceListAdapter.this.handler.sendMessage(message);
        }
    };
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            ++DeviceListAdapter.this.scan_int;
            if(DeviceListAdapter.this.scan_int > 1) {
                DeviceListAdapter.this.scan_int = 0;
                if(Looper.myLooper() == Looper.getMainLooper()) {
                    JDY_type m_tyep = DeviceListAdapter.this.dv_type(scanRecord);
                    if(m_tyep != JDY_type.UNKW && m_tyep != null) {
                        DeviceListAdapter.this.list_cell_0.addDevice(device, scanRecord, Integer.valueOf(rssi), m_tyep);
                        DeviceListAdapter.this.list_cell_0.notifyDataSetChanged();
                    }
                } else {
                    DeviceListAdapter.this.runOnUiThread(new Runnable() {
                        public void run() {
                            JDY_type m_tyep = DeviceListAdapter.this.dv_type(scanRecord);
                            if(m_tyep != JDY_type.UNKW && m_tyep != null) {
                                DeviceListAdapter.this.list_cell_0.addDevice(device, scanRecord, Integer.valueOf(rssi), m_tyep);
                                DeviceListAdapter.this.list_cell_0.notifyDataSetChanged();
                            }

                        }
                    });
                }
            }

        }
    };

    public JDY_type dv_type(byte[] p) {
        if(p.length != 62) {
            return null;
        } else {
            byte m1 = (byte)(p[20] + 1 ^ 17);
            String str = String.format("%02x", new Object[]{Byte.valueOf(m1)});
            byte m2 = (byte)(p[19] + 1 ^ 34);
            str = String.format("%02x", new Object[]{Byte.valueOf(m2)});
            boolean ib1_major = false;
            boolean ib1_minor = false;
            if(p[52] == -1 && p[53] == -1) {
                ib1_major = true;
            }

            if(p[54] == -1 && p[55] == -1) {
                ib1_minor = true;
            }

            if(p[5] == -32 && p[6] == -1 && p[11] == m1 && p[12] == m2 && this.dev_VID == p[13]) {
                byte[] WriteBytes = new byte[]{p[13], p[14], 0, 0};
                Log.d("out_1", "TC" + this.list_cell_0.bytesToHexString1(WriteBytes));
                return p[14] == -96?JDY_type.JDY:(p[14] == -91?JDY_type.JDY_AMQ:(p[14] == -79?JDY_type.JDY_LED1:(p[14] == -78?JDY_type.JDY_LED2:(p[14] == -60?JDY_type.JDY_KG:JDY_type.JDY))));
            } else {
                return p[44] == 16 && p[45] == 22 && (ib1_major || ib1_minor)?JDY_type.sensor_temp:(p[44] == 16 && p[45] == 22?(p[57] == -32?JDY_type.JDY_iBeacon:(p[57] == -31?JDY_type.sensor_temp:(p[57] == -30?JDY_type.sensor_humid:(p[57] == -29?JDY_type.sensor_temp_humid:(p[57] == -28?JDY_type.sensor_fanxiangji:(p[57] == -27?JDY_type.sensor_zhilanshuibiao:(p[57] == -26?JDY_type.sensor_dianyabiao:(p[57] == -25?JDY_type.sensor_dianliu:(p[57] == -24?JDY_type.sensor_zhonglian:(p[57] == -23?JDY_type.sensor_pm2_5:JDY_type.JDY_iBeacon)))))))))):JDY_type.UNKW);
            }
        }
    }

    public DeviceListAdapter(BluetoothAdapter adapter, Context context1) {
        this.apter = adapter;
        this.context = context1;
        this.list_cell_0 = new DeviceListAdapter.DeviceListAdapter1();
        this.timer.schedule(this.task, 1000L, 1000L);
    }

    public DeviceListAdapter.DeviceListAdapter1 init_adapter() {
        return this.list_cell_0;
    }

    public BluetoothDevice get_item_dev(int pos) {
        return (BluetoothDevice)this.list_cell_0.dev_ble.get(pos);
    }

    public JDY_type get_item_type(int pos) {
        return (JDY_type)this.list_cell_0.dev_type.get(pos);
    }

    public int get_count() {
        return this.list_cell_0.getCount();
    }

    public String get_iBeacon_uuid(int pos) {
        return this.list_cell_0.get_ibeacon_uuid(pos);
    }

    public String get_ibeacon_major(int pos) {
        return this.list_cell_0.get_ibeacon_major(pos);
    }

    public String get_ibeacon_minor(int pos) {
        return this.list_cell_0.get_ibeacon_minor(pos);
    }

    public String get_sensor_temp(int pos) {
        return this.list_cell_0.get_sensor_temp(pos);
    }

    public String get_sensor_humid(int pos) {
        return this.list_cell_0.get_sensor_humid(pos);
    }

    public String get_sensor_batt(int pos) {
        return this.list_cell_0.get_sensor_batt(pos);
    }

    public byte get_vid(int pos) {
        return (byte)this.list_cell_0.get_vid(pos);
    }

    public void set_vid(byte vid) {
        this.dev_VID = vid;
    }

    public void loop_list() {
        this.list_cell_0.loop();
    }

    public void stop_flash() {
        this.stop_timer = false;
    }

    public void start_flash() {
        this.stop_timer = true;
    }

    public void clear() {
        this.list_cell_0.clear();
    }

    public void scan_jdy_ble(Boolean p) {
        if(p.booleanValue()) {
            this.list_cell_0.notifyDataSetChanged();
            this.apter.startLeScan(this.mLeScanCallback);
            this.start_flash();
        } else {
            this.apter.stopLeScan(this.mLeScanCallback);
            this.stop_flash();
        }

    }

    class DeviceListAdapter1 extends BaseAdapter {
        private List<BluetoothDevice> dev_ble = new ArrayList();
        private List<JDY_type> dev_type = new ArrayList();
        private List<byte[]> dev_scan_data = new ArrayList();
        private List<Integer> dev_rssi = new ArrayList();
        private List<Integer> remove = new ArrayList();
        private DeviceListAdapter.ViewHolder viewHolder;
        int count = 0;
        int ip = 0;

        public DeviceListAdapter1() {
        }

        public void loop() {
            if(this.remove != null && this.remove.size() > 0 && this.ip == 0) {
                if(this.count >= this.remove.size()) {
                    this.count = 0;
                }

                Integer it = (Integer)this.remove.get(this.count);
                if(it.intValue() >= 3) {
                    this.dev_ble.remove(this.count);
                    this.dev_scan_data.remove(this.count);
                    this.dev_rssi.remove(this.count);
                    this.dev_type.remove(this.count);
                    this.remove.remove(this.count);
                    this.notifyDataSetChanged();
                } else {
                    it = Integer.valueOf(it.intValue() + 1);
                    this.remove.add(this.count + 1, it);
                    this.remove.remove(this.count);
                }

                ++this.count;
            }

        }

        public void addDevice(BluetoothDevice device, byte[] scanRecord, Integer RSSI, JDY_type type) {
            this.ip = 1;
            if(!this.dev_ble.contains(device)) {
                this.dev_ble.add(device);
                this.dev_scan_data.add(scanRecord);
                this.dev_rssi.add(RSSI);
                this.dev_type.add(type);
                Integer i = Integer.valueOf(0);
                this.remove.add(i);
            } else {
                for(int var8 = 0; var8 < this.dev_ble.size(); ++var8) {
                    String btAddress = ((BluetoothDevice)this.dev_ble.get(var8)).getAddress();
                    if(btAddress.equals(device.getAddress())) {
                        this.dev_ble.add(var8 + 1, device);
                        this.dev_ble.remove(var8);
                        this.dev_scan_data.add(var8 + 1, scanRecord);
                        this.dev_scan_data.remove(var8);
                        this.dev_rssi.add(var8 + 1, RSSI);
                        this.dev_rssi.remove(var8);
                        this.dev_type.add(var8 + 1, type);
                        this.dev_type.remove(var8);
                        Integer it = Integer.valueOf(0);
                        this.remove.add(var8 + 1, it);
                        this.remove.remove(var8);
                    }
                }
            }

            this.notifyDataSetChanged();
            this.ip = 0;
        }

        public void clear() {
            this.dev_ble.clear();
            this.dev_scan_data.clear();
            this.dev_rssi.clear();
            this.dev_type.clear();
            this.remove.clear();
        }

        public int getCount() {
            return this.dev_ble.size();
        }

        public BluetoothDevice getItem(int position) {
            return (BluetoothDevice)this.dev_ble.get(position);
        }

        public long getItemId(int position) {
            return (long)position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if(position <= this.dev_ble.size()) {
                JDY_type type_0 = (JDY_type)this.dev_type.get(position);
                BluetoothDevice device;
                String tp;
                String tp1;
                if(type_0 == JDY_type.JDY) {
                    convertView = LayoutInflater.from(DeviceListAdapter.this.context).inflate(R.layout.listitem_switch, (ViewGroup)null);
                    this.viewHolder = DeviceListAdapter.this.new ViewHolder();
                    this.viewHolder.tv_devName = (TextView)convertView.findViewById(R.id.switch_name);
                    this.viewHolder.tv_devAddress = (TextView)convertView.findViewById(R.id.switch_mac);
                    this.viewHolder.device_rssi = (TextView)convertView.findViewById(R.id.switch_rssi);
                    this.viewHolder.type0 = (TextView)convertView.findViewById(R.id.switch_type113);
                    convertView.setTag(this.viewHolder);
                   DeviceListAdapter.this.list_select_index = 1;
                    device = (BluetoothDevice)this.dev_ble.get(position);
                    tp = device.getName();
                    tp = "Name:" + tp;
                    if(this.viewHolder.tv_devName != null) {
                        this.viewHolder.tv_devName.setText(tp);
                    }

                    tp1 = device.getAddress();
                    tp1 = "MAC:" + tp1;
                    if(this.viewHolder.tv_devAddress != null) {
                        this.viewHolder.tv_devAddress.setText(tp1);
                    }

                    String rssi_00 = "" + this.dev_rssi.get(position);
                    rssi_00 = "RSSI:-" + rssi_00;
                    if(this.viewHolder.device_rssi != null) {
                        this.viewHolder.device_rssi.setText(rssi_00);
                    }

                    String tp2 = null;
                    tp2 = "Type:标准模式";
                    if(this.viewHolder.type0 != null) {
                        this.viewHolder.type0.setText(tp2);
                    }

                    if(this.viewHolder.scan_data != null) {
                        this.viewHolder.scan_data.setText("scanRecord:" + this.bytesToHexString1((byte[])this.dev_scan_data.get(position)));
                    }
                }
                return convertView;
            } else {
                return null;
            }
        }

        public String get_ibeacon_uuid(int pos) {
            String uuid = null;
            new HashMap();
            byte[] byte1000 = (byte[])this.dev_scan_data.get(pos);
            if(byte1000.length < 32) {
                return null;
            } else {
                byte[] proximityUuidBytes = new byte[16];
                System.arraycopy(byte1000, 9, proximityUuidBytes, 0, 16);
                String Beacon_UUID = this.bytesToHexString(proximityUuidBytes);
                String uuid_8 = Beacon_UUID.substring(0, 8);
                String uuid_4 = Beacon_UUID.substring(8, 12);
                String uuid_44 = Beacon_UUID.substring(12, 16);
                String uuid_444 = Beacon_UUID.substring(16, 20);
                String uuid_12 = Beacon_UUID.substring(20, 32);
                uuid = uuid_8 + "-" + uuid_4 + "-" + uuid_44 + "-" + uuid_444 + "-" + uuid_12;
                return uuid;
            }
        }

        public String get_ibeacon_major(int pos) {
            String major = null;
            byte[] byte1000 = (byte[])this.dev_scan_data.get(pos);
            if(byte1000.length < 60) {
                return null;
            } else {
                byte[] result = new byte[]{0, 0, byte1000[25], byte1000[26]};
                int ii100 = this.byteArrayToInt1(result);
                major = String.valueOf(ii100);
                return major;
            }
        }

        public String get_ibeacon_minor(int pos) {
            String major = null;
            byte[] byte1000 = (byte[])this.dev_scan_data.get(pos);
            if(byte1000.length < 60) {
                return null;
            } else {
                byte[] result = new byte[]{0, 0, byte1000[27], byte1000[28]};
                int ii100 = this.byteArrayToInt1(result);
                major = String.valueOf(ii100);
                return major;
            }
        }

        public String get_sensor_temp(int pos) {
            byte[] byte1000 = (byte[])this.dev_scan_data.get(pos);
            byte[] result = new byte[]{byte1000[58]};
            return this.bytesToHexString(result);
        }

        public String get_sensor_humid(int pos) {
            byte[] byte1000 = (byte[])this.dev_scan_data.get(pos);
            byte[] result = new byte[]{byte1000[59]};
            return this.bytesToHexString(result);
        }

        public String get_sensor_batt(int pos) {
            byte[] byte1000 = (byte[])this.dev_scan_data.get(pos);
            byte[] result = new byte[]{byte1000[60]};
            return this.bytesToHexString(result);
        }

        public int get_vid(int pos) {
            Object vid = null;
            byte[] byte1000 = (byte[])this.dev_scan_data.get(pos);
            byte[] result = new byte[]{0, 0, 0, 0};
            JDY_type tp = (JDY_type)this.dev_type.get(pos);
            if(tp != JDY_type.JDY && tp != JDY_type.JDY_LED1 && tp != JDY_type.JDY_LED2 && tp != JDY_type.JDY_AMQ && tp != JDY_type.JDY_KG && tp != JDY_type.JDY_WMQ && tp != JDY_type.JDY_LOCK) {
                result[3] = byte1000[56];
            } else {
                result[3] = byte1000[13];
            }

            int ii100 = this.byteArrayToInt1(result);
            return ii100;
        }

        public int byteArrayToInt1(byte[] bytes) {
            int value = 0;

            for(int i = 0; i < 4; ++i) {
                int shift = (3 - i) * 8;
                value += (bytes[i] & 255) << shift;
            }

            return value;
        }

        private String bytesToHexString(byte[] src) {
            StringBuilder stringBuilder = new StringBuilder(src.length);
            byte[] var6 = src;
            int var5 = src.length;

            for(int var4 = 0; var4 < var5; ++var4) {
                byte byteChar = var6[var4];
                stringBuilder.append(String.format("%02X", new Object[]{Byte.valueOf(byteChar)}));
            }

            return stringBuilder.toString();
        }

        private String bytesToHexString1(byte[] src) {
            StringBuilder stringBuilder = new StringBuilder(src.length);
            byte[] var6 = src;
            int var5 = src.length;

            for(int var4 = 0; var4 < var5; ++var4) {
                byte byteChar = var6[var4];
                stringBuilder.append(String.format(" %02X", new Object[]{Byte.valueOf(byteChar)}));
            }

            return stringBuilder.toString();
        }
    }

    class ViewHolder {
        TextView tv_devName;
        TextView tv_devAddress;
        TextView device_rssi;
        TextView type0;
        TextView scan_data;
        TextView ibeacon_name;
        TextView ibeacon_mac;
        TextView ibeacon_uuid;
        TextView ibeacon_major;
        TextView ibeacon_minor;
        TextView ibeacon_rssi;
        TextView sensor_name;
        TextView sensor_mac;
        TextView sensor_rssi;
        TextView sensor_type0;
        TextView sensor_temp;
        TextView sensor_humid;
        TextView sensor_batt;
        TextView switch_name;
        TextView switch_mac;
        TextView switch_rssi;
        TextView switch_type113;
        ImageView type_imageView2;
        TextView massager_name;
        TextView massager_mac;
        TextView massager_rssi;
        TextView massager_type113;
        TextView led_name;
        TextView led_mac;
        TextView led_rssi;
        TextView led_type113;

        ViewHolder() {
        }
    }
}
