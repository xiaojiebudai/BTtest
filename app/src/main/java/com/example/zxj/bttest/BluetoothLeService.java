package com.example.zxj.bttest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service {
    private static final String TAG = BluetoothLeService.class.getSimpleName();
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = 0;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    public static final String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public static final String ACTION_DATA_AVAILABLE1 = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE1";
    public static final String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
    public static final String EXTRA_DATA1 = "com.example.bluetooth.le.EXTRA_DATA1";
    public static final String EXTRA_UUID = "com.example.bluetooth.le.uuid_DATA";
    public static final String EXTRA_NAME = "com.example.bluetooth.le.name_DATA";
    public static final String EXTRA_PASSWORD = "com.example.bluetooth.le.password_DATA";
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList();
    public static final UUID UUID_HEART_RATE_MEASUREMENT;
    public static String Service_uuid;
    public static String Characteristic_uuid_TX;
    public static String Characteristic_uuid_FUNCTION;
    byte[] WriteBytes = new byte[200];
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if(newState == 2) {
                intentAction = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
                BluetoothLeService.this.mConnectionState = 2;
                BluetoothLeService.this.broadcastUpdate(intentAction);
                Log.i(BluetoothLeService.TAG, "Connected to GATT server.");
                Log.i(BluetoothLeService.TAG, "Attempting to start service discovery:" + BluetoothLeService.this.mBluetoothGatt.discoverServices());
            } else if(newState == 0) {
                intentAction = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
                BluetoothLeService.this.mConnectionState = 0;
                Log.i(BluetoothLeService.TAG, "Disconnected from GATT server.");
                BluetoothLeService.this.broadcastUpdate(intentAction);
            }

        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(status == 0) {
                BluetoothLeService.this.broadcastUpdate("com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED");
            } else {
                Log.w(BluetoothLeService.TAG, "onServicesDiscovered received: " + status);
            }

        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("data","onCharacteristicRead----"+String.valueOf(characteristic.getValue()));
            if(status == 0) {
                if(UUID.fromString(BluetoothLeService.Characteristic_uuid_TX).equals(characteristic.getUuid())) {
                    BluetoothLeService.this.broadcastUpdate("com.example.bluetooth.le.ACTION_DATA_AVAILABLE", characteristic);
                } else if(UUID.fromString(BluetoothLeService.Characteristic_uuid_FUNCTION).equals(characteristic.getUuid())) {
                    BluetoothLeService.this.broadcastUpdate("com.example.bluetooth.le.ACTION_DATA_AVAILABLE1", characteristic);
                }
            }

        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            Log.i("data","onCharacteristicChanged----"+characteristic.getValue().toString());

            if(UUID.fromString(BluetoothLeService.Characteristic_uuid_TX).equals(characteristic.getUuid())) {
                BluetoothLeService.this.broadcastUpdate("com.example.bluetooth.le.ACTION_DATA_AVAILABLE", characteristic);
            } else if(UUID.fromString(BluetoothLeService.Characteristic_uuid_FUNCTION).equals(characteristic.getUuid())) {
                BluetoothLeService.this.broadcastUpdate("com.example.bluetooth.le.ACTION_DATA_AVAILABLE1", characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("data","onCharacteristicWrite----"+characteristic.getValue().toString());
        }
    };
    private final IBinder mBinder = new BluetoothLeService.LocalBinder();

    static {
        UUID_HEART_RATE_MEASUREMENT = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
        Service_uuid = "0000ffe0-0000-1000-8000-00805f9b34fb";
        Characteristic_uuid_TX = "0000ffe1-0000-1000-8000-00805f9b34fb";
        Characteristic_uuid_FUNCTION = "0000ffe2-0000-1000-8000-00805f9b34fb";
    }

    public BluetoothLeService() {
    }

    public String bin2hex(String bin) {
        char[] digital = "0123456789ABCDEF".toCharArray();
        StringBuffer sb = new StringBuffer("");
        byte[] bs = bin.getBytes();

        for(int i = 0; i < bs.length; ++i) {
            int bit = (bs[i] & 240) >> 4;
            sb.append(digital[bit]);
            bit = bs[i] & 15;
            sb.append(digital[bit]);
        }

        return sb.toString();
    }

    public byte[] hex2byte(byte[] b) {
        if(b.length % 2 != 0) {
            throw new IllegalArgumentException("长度不是偶数");
        } else {
            byte[] b2 = new byte[b.length / 2];

            for(int n = 0; n < b.length; n += 2) {
                String item = new String(b, n, 2);
                b2[n / 2] = (byte)Integer.parseInt(item, 16);
            }

            Object b1 = null;
            return b2;
        }
    }

    void deley(int ms) {
        try {
            Thread.currentThread();
            Thread.sleep((long)ms);
        } catch (InterruptedException var3) {
            var3.printStackTrace();
        }

    }

    private static byte charToByte(char c) {
        return (byte)"0123456789ABCDEF".indexOf(c);
    }

    public static byte[] getBytesByString(String data) {
        byte[] bytes = null;
        if(data != null) {
            data = data.toUpperCase();
            int length = data.length() / 2;
            char[] dataChars = data.toCharArray();
            bytes = new byte[length];

            for(int i = 0; i < length; ++i) {
                int pos = i * 2;
                bytes[i] = (byte)(charToByte(dataChars[pos]) << 4 | charToByte(dataChars[pos + 1]));
            }
        }

        return bytes;
    }

    public String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder(src.length);
        byte[] var6 = src;
        int var5 = src.length;

        for(int var4 = 0; var4 < var5; ++var4) {
            byte byteChar = var6[var4];
            stringBuilder.append(String.format("%02X", new Object[]{Byte.valueOf(byteChar)}));
        }

        return stringBuilder.toString();
    }

    public String bytesToHexString1(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder(src.length);
        byte[] var6 = src;
        int var5 = src.length;

        for(int var4 = 0; var4 < var5; ++var4) {
            byte byteChar = var6[var4];
            stringBuilder.append(String.format(" %02X", new Object[]{Byte.valueOf(byteChar)}));
        }

        return stringBuilder.toString();
    }

    public String bytesToHexString1(byte[] src, int index) {
        if(src == null) {
            return null;
        } else {
            StringBuilder stringBuilder = new StringBuilder(src.length);

            for(int i = index; i < src.length; ++i) {
                stringBuilder.append(String.format(" %02X", new Object[]{Byte.valueOf(src[i])}));
            }

            return stringBuilder.toString();
        }
    }

    public String String_to_HexString0(String str) {
        String st = str.toString();
        byte[] WriteBytes = new byte[st.length()];
        WriteBytes = st.getBytes();
        return this.bytesToHexString(WriteBytes);
    }

    public String String_to_HexString(String str) {
        String st = str.toString();
        byte[] WriteBytes = new byte[st.length()];
        WriteBytes = st.getBytes();
        return this.bytesToHexString1(WriteBytes);
    }

    public byte[] String_to_byte(String str) {
        String st = str.toString();
        byte[] WriteBytes = new byte[st.length()];
        return WriteBytes;
    }

    public String byte_to_String(byte[] byt) {
        String t = new String(byt);
        return t;
    }



    public int txxx(String g, boolean string_or_hex_data) {
        int ic = 0;
        if(string_or_hex_data) {
            this.WriteBytes = g.getBytes();
        } else {
            this.WriteBytes = getBytesByString(g);
        }

        int length = this.WriteBytes.length;
        int data_len_20 = length / 20;
        int data_len_0 = length % 20;
        int i = 0;
        byte[] da;
        int gg;
        BluetoothGattCharacteristic var10;
        if(data_len_20 > 0) {
            while(i < data_len_20) {
                da = new byte[20];

                for(gg = 0; gg < 20; ++gg) {
                    da[gg] = this.WriteBytes[20 * i + gg];
                }

                var10 = this.mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
                var10.setValue(da);
                this.mBluetoothGatt.writeCharacteristic(var10);
                this.deley(20);
                ic += 20;
                ++i;
            }
        }

        if(data_len_0 > 0) {
            da = new byte[data_len_0];

            for(gg = 0; gg < data_len_0; ++gg) {
                da[gg] = this.WriteBytes[20 * i + gg];
            }

            var10 = this.mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
            var10.setValue(da);
            this.mBluetoothGatt.writeCharacteristic(var10);
            ic += data_len_0;
        }

        return ic;
    }

    public void function_data(byte[] data) {
        this.WriteBytes = data;
        BluetoothGattCharacteristic gg = this.mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
        gg.setValue(this.WriteBytes);
        this.mBluetoothGatt.writeCharacteristic(gg);
    }


    public void enable_JDY_ble(int p) {
        try {
            BluetoothGattService e = this.mBluetoothGatt.getService(UUID.fromString(Service_uuid));
            BluetoothGattCharacteristic ale;
            switch(p) {
                case 0:
                    ale = e.getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
                    break;
                case 1:
                    ale = e.getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
                    break;
                default:
                    ale = e.getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
            }

            this.mBluetoothGatt.setCharacteristicNotification(ale, true);
            BluetoothGattDescriptor dsc = ale.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            byte[] bytes = new byte[]{1, 0};
            dsc.setValue(bytes);
            this.mBluetoothGatt.writeDescriptor(dsc);
        } catch (NumberFormatException var8) {
            var8.printStackTrace();
        }

    }

    public void Delay_ms(int ms) {
        try {
            Thread.currentThread();
            Thread.sleep((long)ms);
        } catch (InterruptedException var3) {
            var3.printStackTrace();
        }

    }

    public int get_connected_status(List<BluetoothGattService> gattServices) {
        boolean jdy_ble_server = false;
        boolean jdy_ble_ffe1 = false;
        boolean jdy_ble_ffe2 = false;
        String LIST_NAME1 = "NAME";
        String LIST_UUID1 = "UUID";
        String uuid = null;
        String unknownServiceString = this.getResources().getString(R.string.unknown_service);
        String unknownCharaString = this.getResources().getString(R.string.unknown_characteristic);
        ArrayList gattServiceData = new ArrayList();
        ArrayList gattCharacteristicData = new ArrayList();
        int count_char = 0;
        Iterator var14 = gattServices.iterator();

        while(var14.hasNext()) {
            BluetoothGattService gattService = (BluetoothGattService)var14.next();
            HashMap currentServiceData = new HashMap();
            uuid = gattService.getUuid().toString();
            currentServiceData.put("NAME", SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put("UUID", uuid);
            gattServiceData.add(currentServiceData);
            ArrayList gattCharacteristicGroupData = new ArrayList();
            List gattCharacteristics = gattService.getCharacteristics();
            ArrayList charas = new ArrayList();
            if(Service_uuid.equals(uuid)) {
                jdy_ble_server = true;
            }

            Iterator var20 = gattCharacteristics.iterator();

            while(var20.hasNext()) {
                BluetoothGattCharacteristic gattCharacteristic = (BluetoothGattCharacteristic)var20.next();
                charas.add(gattCharacteristic);
                HashMap currentCharaData = new HashMap();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put("NAME", SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put("UUID", uuid);
                gattCharacteristicGroupData.add(currentCharaData);
                ++count_char;
                if(jdy_ble_server) {
                    if(Characteristic_uuid_TX.equals(uuid)) {
                        jdy_ble_ffe1 = true;
                    } else if(Characteristic_uuid_FUNCTION.equals(uuid)) {
                        jdy_ble_ffe2 = true;
                    }
                }
            }

            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        if(jdy_ble_ffe1 && jdy_ble_ffe2) {
            return 2;
        } else if(jdy_ble_ffe1 && !jdy_ble_ffe2) {
            return 1;
        } else {
            return 0;
        }
    }

    private void broadcastUpdate(String action) {
        Intent intent = new Intent(action);
        this.sendBroadcast(intent);
    }

    private void broadcastUpdate(String action, BluetoothGattCharacteristic characteristic) {
        Intent intent = new Intent(action);
        Log.d("getUuid", " len = " + characteristic.getUuid());
        if(UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int data = characteristic.getProperties();
            boolean format = true;
            byte format1;
            if((data & 1) != 0) {
                format1 = 18;
            } else {
                format1 = 17;
            }

            int heartRate = characteristic.getIntValue(format1, 1).intValue();
            intent.putExtra("com.example.bluetooth.le.EXTRA_DATA", String.valueOf(heartRate));
        } else {
            byte[] data1;
            if(UUID.fromString(Characteristic_uuid_TX).equals(characteristic.getUuid())) {
                data1 = characteristic.getValue();
                if(data1 != null && data1.length > 0) {
                    intent.putExtra("com.example.bluetooth.le.EXTRA_DATA", data1);
                }
            } else if(UUID.fromString(Characteristic_uuid_FUNCTION).equals(characteristic.getUuid())) {
                data1 = characteristic.getValue();
                if(data1 != null && data1.length > 0) {
                    intent.putExtra("com.example.bluetooth.le.EXTRA_DATA1", data1);
                }
            }
        }

        this.sendBroadcast(intent);
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public boolean onUnbind(Intent intent) {
        this.close();
        return super.onUnbind(intent);
    }

    public boolean initialize() {
        if(this.mBluetoothManager == null) {
            this.mBluetoothManager = (BluetoothManager)this.getSystemService(Context.BLUETOOTH_SERVICE);
            if(this.mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        this.mBluetoothAdapter = this.mBluetoothManager.getAdapter();
        if(this.mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        } else {
            return true;
        }
    }

    public boolean connect(String address) {
        if(this.mBluetoothAdapter != null && address != null) {
            if(this.mBluetoothDeviceAddress != null && address.equals(this.mBluetoothDeviceAddress) && this.mBluetoothGatt != null) {
                Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
                if(this.mBluetoothGatt.connect()) {
                    this.mConnectionState = 1;
                    return true;
                } else {
                    return false;
                }
            } else {
                BluetoothDevice device = this.mBluetoothAdapter.getRemoteDevice(address);
                if(device == null) {
                    Log.w(TAG, "Device not found.  Unable to connect.");
                    return false;
                } else {
                    this.mBluetoothGatt = device.connectGatt(this, false, this.mGattCallback);
                    Log.d(TAG, "Trying to create a new connection.");
                    this.mBluetoothDeviceAddress = address;
                    this.mConnectionState = 1;
                    return true;
                }
            }
        } else {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
    }

    public void disconnect() {
        if(this.mBluetoothAdapter != null && this.mBluetoothGatt != null) {
            this.mBluetoothGatt.disconnect();
        } else {
            Log.w(TAG, "BluetoothAdapter not initialized");
        }
    }

    public boolean isconnect() {
        return this.mBluetoothGatt.connect();
    }

    public void close() {
        if(this.mBluetoothGatt != null) {
            this.mBluetoothGatt.close();
            this.mBluetoothGatt = null;
        }
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if(this.mBluetoothAdapter != null && this.mBluetoothGatt != null) {
            this.mBluetoothGatt.readCharacteristic(characteristic);
        } else {
            Log.w(TAG, "BluetoothAdapter not initialized");
        }
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if(this.mBluetoothAdapter != null && this.mBluetoothGatt != null) {
            this.mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
            if(UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                this.mBluetoothGatt.writeDescriptor(descriptor);
            }

        } else {
            Log.w(TAG, "BluetoothAdapter not initialized");
        }
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        return this.mBluetoothGatt == null?null:this.mBluetoothGatt.getServices();
    }

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }
}