package top.gtf35.bleserial;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;


/**
 * 蓝牙工具类 2020/5/18
 * 功能：搜索蓝牙设备
 *      连接蓝牙串口
 *      发送串口数据
 *      接收串口数据
 * @author gtf35 gtf@gtf35.top
 */
class BLESPPUtils {
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private OnBluetoothAction mOnBluetoothAction;
    private ConnectTask mConnectTask = new ConnectTask();

    /**
     * 搜索到新设备广播广播接收器
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mOnBluetoothAction != null) mOnBluetoothAction.onFoundDevice(device);
            }
        }
    };

    /**
     * 搜索结束广播接收器
     */
    private final BroadcastReceiver mFinishFoundReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (mOnBluetoothAction != null) mOnBluetoothAction.onFinishFoundDevice();
            }
        }
    };

    /**
     * 连接任务
     */
    private static class ConnectTask extends AsyncTask<String, Byte[], Void> {
        private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothSocket bluetoothSocket;
        BluetoothDevice romoteDevice;
        OnBluetoothAction onBluetoothAction;
        boolean isRunning = false;

        @Override
        protected Void doInBackground(String... bluetoothDevicesMac) {
            // 记录标志位，开始运行
            isRunning = true;

            // 尝试获取 bluetoothSocket
            try {
                UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                romoteDevice = bluetoothAdapter.getRemoteDevice(bluetoothDevicesMac[0]);
                bluetoothSocket =  romoteDevice.createRfcommSocketToServiceRecord(SPP_UUID);
            } catch (Exception e) {
                Log.d("log", "获取Socket失败");
                isRunning = false;
                e.printStackTrace();
                return null;
            }

            // 检查有没有获取到
            if (bluetoothSocket == null) {
                onBluetoothAction.onConnectFailed("连接失败:获取Socket失败");
                isRunning = false;
                return null;
            }

            // 尝试连接
            try {
                // 等待连接，会阻塞线程
                bluetoothSocket.connect();
                Log.d("BLEUTILS", "连接成功");
                onBluetoothAction.onConnectSuccess(romoteDevice);
            } catch (Exception connectException) {
                connectException.printStackTrace();
                Log.d("BLEUTILS", "连接失败:" + connectException.getMessage());
                onBluetoothAction.onConnectFailed("连接失败:" + connectException.getMessage());
                return null;
            }

            // 开始监听数据接收
            // todo 验证是不是真能收到数据
            try {
                InputStream inputStream = bluetoothSocket.getInputStream();
                StringBuilder sb = new StringBuilder();
                while (isRunning) {
                    byte[] buffer = new byte[1000];
                    Log.d("BLEUTILS", "looping");
                    int result = inputStream.read(buffer);
                    sb.append(Arrays.toString(buffer));
                    //  -1 代表没有数据了，这次接收完成
                    if (result == -1) {
                        // 返回数据
                        onBluetoothAction.onReceiveBytes(sb.toString().getBytes());
                        // 清空
                        sb = new StringBuilder();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                onBluetoothAction.onConnectFailed("接收数据失败：" + e.getMessage());
            }

            return null;
        }

        @Override
        protected void onCancelled() {
            try {
                isRunning = false;
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 发送
         * @param msg 内容
         */
        void send(byte[] msg) {
            try {
                bluetoothSocket.getOutputStream().write(msg);
                onBluetoothAction.onSendBytes(msg);
            } catch (Exception e){e.printStackTrace();}
        }
    }

    /**
     * 蓝牙活动回调
     */
    public interface OnBluetoothAction {
        /**
         * 发现新设备
         * @param device 设备
         */
        void onFoundDevice(BluetoothDevice device);

        /**
         * 连接成功
         */
        void onConnectSuccess(BluetoothDevice device);

        /**
         * 连接失败
         * @param msg 失败信息
         */
        void onConnectFailed(String msg);

        /**
         * 接收到 byte 数组
         * @param bytes 内容
         */
        void onReceiveBytes(byte[] bytes);

        /**
         * 发送 byte 数组
         * @param bytes 内容
         */
        void onSendBytes(byte[] bytes);

        /**
         * 结束搜索设备
         */
        void onFinishFoundDevice();
    }

    /**
     * 构造蓝牙工具
     * @param context 上下文
     * @param onBluetoothAction 蓝牙状态改变回调
     */
    BLESPPUtils(Context context, OnBluetoothAction onBluetoothAction) {
        mContext = context;
        mOnBluetoothAction = onBluetoothAction;
    }

    /**
     * 初始化回调
     */
    void onCreate() {
        IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mReceiver, foundFilter);
        IntentFilter finishFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mFinishFoundReceiver, finishFilter);
    }

    /**
     * 销毁回调
     */
    void onDestroy() {
        try {
            mContext.unregisterReceiver(mReceiver);
            mContext.unregisterReceiver(mFinishFoundReceiver);
            mConnectTask.cancel(true);
        } catch (Exception e) {e.printStackTrace();}
    }

    /**
     * 开始搜索
     */
    void startDiscovery() {
        mBluetoothAdapter.startDiscovery();
    }

    /**
     * 使用搜索到的数据连接
     * @param device 设备
     */
    void connect(BluetoothDevice device) {
        connect(device.getAddress());
    }

    /**
     * 使用Mac地址来连接
     * @param deviceMac 要连接的设备的 MAC
     */
    private void connect(String deviceMac) {
        if (mConnectTask.getStatus() == AsyncTask.Status.RUNNING && mConnectTask.isRunning) {
            if (mOnBluetoothAction != null) mOnBluetoothAction.onConnectFailed("有正在连接的任务");
            return;
        }
        mConnectTask.onBluetoothAction = mOnBluetoothAction;
        try {
            mConnectTask.execute(deviceMac);
        } catch (Exception e) {e.printStackTrace();}
    }

    /**
     * 发送 byte 数组到串口
     * @param bytes 要发送的数据
     */
    void send(byte[] bytes) {
        if (mConnectTask != null) mConnectTask.send(bytes);
    }

}
