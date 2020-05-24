蓝牙工具类：```BLESPPUtils.java``` [直达链接](https://github.com/gtf35/BLESerial/blob/master/app/src/main/java/top/gtf35/bleserial/BLESPPUtils.java)

调用示例：MainActivity.java  [直达链接](https://github.com/gtf35/BLESerial/blob/master/app/src/main/java/top/gtf35/bleserial/MainActivity.java)

配套讲解文章：快速上手 Android 蓝牙串口 SPP 开发 [直达链接](https://blog.gtf35.top/bluetooth_spp/)

使用方式：

-   1 加入权限并确认处理好运行时权限，已经授予了定位权限（用于扫描蓝牙设备）

    ```xml
    <!--管理蓝牙需要-->
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <!--搜索蓝牙需要，因为蓝牙可以被用来定位，所以需要定位权限-->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    ```

-   2 实例化工具类，第一个参数是 context，可以为任意 context，不涉及 UI 操作；第二个参数是「蓝牙活动回调」

    ```java
    // 初始化
    mBLESPPUtils = new BLESPPUtils(MainActivity.this, new BLESPPUtils.OnBluetoothAction() {
        /**
         * 当发现新设备
         *
         * @param device 设备
         */
        @Override
        public void onFoundDevice(BluetoothDevice device) {
            
        }
    
        /**
         * 当连接成功
         *
         * @param device
         */
        @Override
        public void onConnectSuccess(BluetoothDevice device) {
    
        }
    
        /**
         * 当连接失败
         *
         * @param msg 失败信息
         */
        @Override
        public void onConnectFailed(String msg) {
    
        }
    
        /**
         * 当接收到 byte 数组
         *
         * @param bytes 内容
         */
        @Override
        public void onReceiveBytes(byte[] bytes) {
    
        }
    
        /**
         * 当调用接口发送了 byte 数组
         *
         * @param bytes 内容
         */
        @Override
        public void onSendBytes(byte[] bytes) {
    
        }
    
        /**
         * 当结束搜索设备
         */
        @Override
        public void onFinishFoundDevice() {
    
        }
    });
    ```

-   3 设置接收停止标志位字符串

    ```java
    mBLESPPUtils.setStopString("\r\n");
    ```

-   4 启用工具类

    ```java
    mBLESPPUtils.onCreate();
    ```

-   5 在适当的时候销毁工具类，譬如 Activity 的 ```onDestroy()```

    ```java
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBLESPPUtils.onDestroy();
    }
    ```

-   6 检查蓝牙开启情况

    ```java
    // 用户没有开启蓝牙的话打开蓝牙
    if (!mBLESPPUtils.isBluetoothEnable()) mBLESPPUtils.enableBluetooth();
    ```

-   7 搜索蓝牙设备，搜索结果在上面的回调里了

    ```java
    mBLESPPUtils.startDiscovery();
    ```

-   8 使用搜索到的 BluetoothDevice 连接设备

    ```java
    mBLESPPUtils.connect(bluetoothDevice);
    ```

-   9 使用之前保存的 MAC 地址连接

    ```java
    mBLESPPUtils.connect("mac地址");
    ```

-   10 发送 byte 数组到串口

    ```java
    mBLESPPUtils.send("hello\r\n".getBytes());
    ```

-   11 接收到串口发送的 byte 数组在上面第二步的```onReceiveBytes(byte[] bytes)```回调中

-   12 启用工具类日志输出

    ```java
    mBLESPPUtils.enableBluetooth();
    ```