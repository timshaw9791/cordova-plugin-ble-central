// (c) 2014-2016 Don Coleman
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.megster.cordova.ble.central;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Build;

import android.provider.Settings;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;


import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.util.*;



public class BLECentralPlugin extends CordovaPlugin{
    // actions
    private static final String PRINT = "print";
    public static final String QSPRINTER_NAME = "QSprinter";
    private static final int REQUEST_ENABLE_BT = 2;

    BlueToothService mService = null;
    CallbackContext callbackContext=null;

    boolean alreadyHaveOnePrinterToConnect = false;
    String msgToPrint = null;
    int counterForPrintCalling = 0;
    boolean callPrintInProgress = false;//TODO 在exec方法中设置为true，在异步或者同步回调时使用...

    //message
    private final static String ERROR_MSG_callPrintInProgress = "正在打印，请不要连续按键！";
    private final static String ERROR_MSG_QSPrinterproblem = "打印机故障，请换一台试试?";
    private final static String ERROR_MSG_QSPrinterNotFound = "没找到打印机，请确认打印机已开机!";

    private static final String MESSGE_PRINT_CANCELED = "canceled";//打印取消了

    private void callback(boolean success, String message) {
        Log.i("Bluetooth", success + ":" + message);
        reset();
        if (success) {
            callbackContext.success();
        } else {
            callbackContext.error(message);
        }
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        LOG.d("BLECentralPlugin", "action = " + action);
        boolean validAction = true;
        this.callbackContext=callbackContext;

        if (action.equals(PRINT)) {
            String message = args.getString(0);
            print(message); //printtest();
        } else {
            validAction = false;
        }
        return validAction;
    }

    /**
     * 最终的接口
     **/
    protected void print(String msg) {
        if ((msg.length() > 0 && !callPrintInProgress)) {
            this.msgToPrint = msg;
            counterForPrintCalling = 0;
            this.alreadyHaveOnePrinterToConnect = false;
            print();
        } else {
            callback(false, ERROR_MSG_callPrintInProgress);
        }
    }

    private void print() {
        if (counterForPrintCalling++ > 2) {
            callback(false, ERROR_MSG_QSPrinterproblem);
        }
        try {
            mService.sendMessage(msgToPrint + "\n", "GBK");
            Log.v("QSPrinter===",msgToPrint);
        } catch (Exception e) {
            //e.printStackTrace();
            reset();
            discoverIfNecessary();
        }
    }

    public void reset() {
        if (mService != null) {
            mService.cancelDiscovery();
            mService.stop();
            mService = null;
        }
        try {
            webView.getContext().unregisterReceiver(mReceiver);
        } catch (Exception e) {

        }
        this.alreadyHaveOnePrinterToConnect = false;
        this.counterForPrintCalling = 0;
    }


    private void discoverIfNecessary() {
        mService = new BlueToothService(null, mHandler);
        if (!mService.isAvailable() || !mService.isBTopen()) {
            //蓝牙未开…
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            cordova.startActivityForResult(this,enableIntent, REQUEST_ENABLE_BT);
        } else {//链接QSPrinter// 1注册receiver
            boolean qsprinterpaired = false;//是否已有配对的打印机
            Set<BluetoothDevice> pairedDevices = mService.getPairedDev();
            // If there are paired devices, add each one to the ArrayAdapter
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (QSPRINTER_NAME.equals(device.getName())) {
                        qsprinterpaired = true;
                        connectPrinter(device);
                        break;
                    }
                }
                if (!qsprinterpaired) {
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    webView.getContext().registerReceiver(mReceiver, filter);
                    filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    webView.getContext().registerReceiver(mReceiver, filter);
                    if (mService.isDiscovering()) {//2发现QSPrinter
                        mService.cancelDiscovery();
                    }
                    mService.startDiscovery();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        reset();
    }


    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED && QSPRINTER_NAME.equals(device.getName())) {//
                    //获取列表项中设备的mac地址
                    connectPrinter(device);
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                callback(false, ERROR_MSG_QSPrinterNotFound);
            }
        }
    };

    private void connectPrinter(BluetoothDevice device) {
        BluetoothDevice con_dev = mService.getDevByMac(device.getAddress());
        if (!alreadyHaveOnePrinterToConnect) {
            alreadyHaveOnePrinterToConnect = true;
            mService.connect(con_dev);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                //请求打开蓝牙
                if (resultCode == Activity.RESULT_OK) {
                    //蓝牙已经打开
                    discoverIfNecessary();
                } else {
                    //用户不允许打开蓝牙
                    callback(false, MESSGE_PRINT_CANCELED);
                }
                break;

        }
    }

private void printtest(){
            //开启打印
            //打印的信息
            StringBuffer buffer = new StringBuffer();

            List<Clothes> mClothes = new ArrayList<>();

            mClothes.add(new Clothes("6651608", "连衣裙(短)", "白色", "精洗", "30.00",
                    "油性污渍|洗尽量|渍迹会残留|面料发黄", "", ""));
            mClothes.add(new Clothes("6651609", "T恤", "黑色", "精洗", "40.00", "油性污渍|尽量洗|尽量洗|渍迹会残留|面料发黄", "", ""));
            mClothes.add(new Clothes("6651610", "真丝T恤", "白色", "精洗", "40.00", "面料发黄|尽量洗|油性污渍|渍迹会残留", "大量去渍", "70.00"));

            String[] detail = {"10001", "3", "1998-01-01 13:22:25", "2018-06-06 18:25:21", "1801856851", "瓯海区茶山街道XXXXXXX", "160.00", "70.00"};

//                    String currTime = DateUtils.getStandardDate(System.currentTimeMillis()); //当前时间

            buffer.append(PrintUtils.print());
            buffer.append(PrintUtils.printTitle("交易单号:" + detail[0])).append("\n");
            buffer.append(PrintUtils.printTitle("衣物数量:" + detail[1] + "件")).append("\n\n");

            buffer.append("收衣日期:").append(detail[2]).append("\n");
            buffer.append("取衣日期:").append(detail[3]).append("\n\n");

            buffer.append("顾客签字:").append("\n");
            buffer.append("顾客电话:").append(detail[4]).append("\n");
            buffer.append("顾客地址:").append(detail[5]).append("\n\n");

            buffer.append(PrintUtils.print());
            buffer.append(PrintUtils.printFourData("名称/条码", "颜色", "服务档次", "价格")).append("\n");
            buffer.append(PrintUtils.print());

            for (Clothes clothes : mClothes) {
                buffer.append(PrintUtils.printFourData(clothes.getClothesName(), clothes.getClothesColor(), clothes.getClothesGrade(), clothes.getClothesPrice())).append("\n");
                buffer.append("|-(" + clothes.getClothesId() + ")" + "@瑕疵(" + clothes.getClothesDefect() + ")").append("\n");
                if (!clothes.getClothesAdditional().isEmpty()) {
                    buffer.append("|-@附加服务(不享受折扣):").append("\n");
                    buffer.append("|-" + clothes.getClothesAdditional() + ": " + "               " + clothes.getClothesAdditionalPrice()).append("\n\n");
                } else {
                    buffer.append("\n");
                }
            }

/*                for (int i = 0; i < mClothes.size(); i++) {
                    buffer.append(PrintUtils.printFourData(mClothes.get(i).getClothesName(), mClothes.get(i).getClothesColor(), mClothes.get(i).getClothesGrade(), mClothes.get(i).getClothesPrice())).append("\n");
                    buffer.append("|-(" + mClothes.get(i).getClothesId() + ")" + "@瑕疵(" + mClothes.get(i).getClothesDefect() + ")").append("\n");
                    if (!mClothes.get(i).getClothesAdditional().isEmpty()) {
                        buffer.append("|-@附加服务(不享受折扣):").append("\n");
                        buffer.append("|-" + mClothes.get(i).getClothesAdditional() + ": " + "               " + mClothes.get(i).getClothesAdditionalPrice()).append("\n\n");
                    } else {
                        buffer.append("\n");
                    }
                }*/

            buffer.append("交易总额:" + detail[6] + "元(服务费" + detail[7] + "元)").append("\n");
            buffer.append(PrintUtils.print()).append("\n");
            buffer.append("应收金额：" + detail[6] + "元").append("\n\n");
            buffer.append("本店地址：" + "温州市瓯海区xxxxxx").append("\n");
            buffer.append("服务热线：" + "88668866").append("\n");
            buffer.append("店员：" + "XXX" + "    " + "店号：" + "007").append("\n");
            buffer.append(PrintUtils.print());

            String msg = buffer.toString();
            print(msg);
        }


    /**
     * 创建一个Handler实例，用于接收BluetoothService类返回回来的消息
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BlueToothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BlueToothService.STATE_CONNECTED:
                            //已连接
                            Log.i("Bluetooth", "Connect successful");
                            print();
                            break;
                        case BlueToothService.STATE_CONNECTING:
                            //正在连接
                            Log.i("Bluetooth", ".....is connecting");
                            break;
                        case BlueToothService.STATE_LISTEN:
                            //监听连接的到来
                        case BlueToothService.STATE_NONE:
                            Log.i("Bluetooth", ".....wait connecting");
                            break;
                    }
                    break;
                case BlueToothService.MESSAGE_CONNECTION_LOST:
                    //蓝牙已断开连接
                    Log.i("Bluetooth", "Device connection was lost");
                    break;
                case BlueToothService.MESSAGE_UNABLE_CONNECT:
                    //无法连接设备
                    Log.i("Bluetooth", "Unable to connect device");
                    callback(false, ERROR_MSG_QSPrinterNotFound);
                    break;
            }
        }

    };

}
