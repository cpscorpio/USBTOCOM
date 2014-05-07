package com.uboxol.serialcomport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.uboxol.serialcomport.SerialComPort.SerialComPortStatus;
import com.uboxol.usbtocom.Tools;

/**
 * <p>端口控制类</p>
 * @author  chenpeng
 * @version Version 0.1
 */
public class SerialComPortControl {

    private String receiveAction = "";

    private Handler receiveHandler;

    private Context context;

    private USBDeviceStatus usbDeviceStatus = USBDeviceStatus.NOT_CONNECT;

    private SerialComPort port;



    enum USBDeviceStatus {

        NOT_CONNECT(0),
        NO_PERMISSION(1),
        CONNECTED(2);

        private int value;
        USBDeviceStatus(int v)
        {
            value = v;
        }
        int getValue()
        {
            return this.value;
        }
    }
    /**
     * 构造函数
     * @param action  使用BroadcastReceiver接受消息，需自定义一个action字符串
     * @param handler 可传入接收消息handler ，代替read方法获取串口消息
     * @param context android.content.Context
     * @exception NullPointerException action or context 为空时抛出
     */
    public SerialComPortControl(String action ,Handler handler, Context context)
    {
        if (action == null || action.length() == 0) throw new NullPointerException("action is null or empty");
        if (context == null ) throw new NullPointerException("context is null ");

        this.receiveAction = action;
        this.context = context;
        this.receiveHandler = handler;

        port = new SerialComPort(this.context, this.receiveAction);
    }
    /**
     * 构造函数
     * @param action  使用BroadcastReceiver接受消息，需自定义一个action字符串
     * @param context android.content.Context
     * @exception NullPointerException action or context 为空时抛出
     */
    public SerialComPortControl(String action , Context context)
    {
        if (action == null || action.length() == 0) throw new NullPointerException("action is null or empty");
        if (context == null ) throw new NullPointerException("context is null ");

        this.receiveAction = action;
        this.context = context;
        this.receiveHandler = null;

        port = new SerialComPort(context, action);


    }

    /**
     * serialComPortStatus 方法简述
     * <p>获取串口的打开状态</p>
     * @return 串口的状态
     */
    public SerialComPortStatus serialComPortStatus()
    {
        return port.status();
    }

    /**
     * close 方法简述
     * <p>关闭串口函数<p/>
     * <p>关闭以及打开的串口<p/>
     */
    public void close()
    {
        Intent intent = new Intent(SerialComPortAction.SerialComPort_DISCONNECT);
        intent.putExtra(SerialComPortAction.APP_ACTION, receiveAction);
        context.sendBroadcast(intent);
        if (onCloseListener != null)
        {
            onCloseListener.dispatch(0);
        }
        unregisterReceiver();
        port.close();
    }
    /**
     * read 方法简述
     * <p>读取串口发送的数据</p>
     * @param datas     接收数组
     * @param length    接收数据大小
     * @param millis    读取超时时间
     * @return          实际读取到的数据字节大小
     * @exception ReadSerialDataException 未打开时端口使用抛出，必须捕获该异常
     */
    public int read( byte[] datas, int length, int millis) throws ReadSerialDataException
    {
        int len = port.read(datas, length);
        if (len > 0)
        {
            return len;
        }
        else
        {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (port.status().equals(SerialComPortStatus.CONNECTED)){
                return port.read(datas, length);
            }
            else
            {
                return len;
            }

        }
    }

    private SerialPortListener onCloseListener = null;

    private void setOnCloseListener( SerialPortListener onCloseListener)
    {
        if (onCloseListener == null) throw new NullPointerException(" onCloseListener is null");
        this.onCloseListener = onCloseListener;
    }

    private SerialPortListener onOpenListener = null;
    /**
     * 打开串口
     * @param com_id        com端口ID
     * @param bitRate       比特率
     * @param data_bits     数据位
     * @param stop_bits     停止位
     * @param parity        校验位
     * @param onOpenListener 异步，完成打开串口之后，会回调此对象的 dispatch(int value) 方法；
     * @exception NullPointerException com_id 为空时抛出
     */
    public void open( SerialComPort.COM_ID com_id, int bitRate, SerialComPort.DATA_BITS data_bits, SerialComPort.STOP_BITS stop_bits, SerialComPort.PARITY parity, SerialPortListener onOpenListener)
    {
        if (com_id == null) throw  new NullPointerException("com id is null");
        registerReceiver();
        this.onOpenListener = onOpenListener;

        port.setStatus(SerialComPortStatus.CONNECTING);

        port.open( com_id, bitRate, stop_bits, data_bits, parity);

        if( !usbDeviceStatus.equals(USBDeviceStatus.CONNECTED))
        {
            connectUsbDevice();
        }
    }
    /**
     * send 方法简述
     * <p>给串口发送数据</p>
     * @param message   要发送的字符串
     * @exception WriteSerialDataException 端口未打开时抛出，必须捕获该异常
     */
    public void send(String message) throws WriteSerialDataException
    {
        port.send(message);
    }

    /**
     * send 方法简述
     * <p>给串口发送数据</p>
     * @param data      要发送的字节数据
     * @param len       数据字节长度
     * @exception WriteSerialDataException 端口未打开时抛出，必须捕获该异常
     */
    public void send( byte[] data, int len) throws WriteSerialDataException
    {
        port.send(data, len);
    }
    private void connectUsbDevice()
    {
        Intent intent = new Intent(SerialComPortAction.SERVICE_INIT_MESSAGE);
        intent.putExtra(SerialComPortAction.APP_ACTION, receiveAction);
        context.sendBroadcast(intent);
    }



    private boolean unregisterReceiver()
    {
        boolean flag = true;
        if (broadCastReceiver != null)
        {

            try {
                context.unregisterReceiver(broadCastReceiver);
            }
            catch (Exception e)
            {
                Log.d("unregisterReceiver", e.getMessage());
                flag = false;
            }
            broadCastReceiver = null;
        }
        return flag;
    }

    private boolean registerReceiver()
    {
        boolean flag = true;
        if ( broadCastReceiver == null)
        {
            try {
                broadCastReceiver = new DataReceiver();
                IntentFilter filter = new IntentFilter(); //创建IntentFilter对象
                filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
                filter.addAction(receiveAction);
                context.registerReceiver(broadCastReceiver, filter);//注册Broadcast Receiver
            }
            catch (Exception e)
            {
                Log.d("registerReceiver",e.getMessage());
                flag = false;
            }
        }

        return flag;
    }


    private DataReceiver broadCastReceiver;
    private class DataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Tools.showShortToast(context, "context " + intent.getAction());
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction()))
            {
                close();
            }
            if(! usbDeviceStatus.equals(USBDeviceStatus.CONNECTED))
            {
                if (USBDeviceStatus.CONNECTED.getValue() == intent.getIntExtra(SerialComPortAction.EXTRA_INIT_STATUS,10 )) //连接成功
                {
                    usbDeviceStatus = USBDeviceStatus.CONNECTED;
                    if (port.isConnecting())
                    {
                        port.open();
                    }
                }
                else if (USBDeviceStatus.NOT_CONNECT.getValue() == intent.getIntExtra(SerialComPortAction.EXTRA_INIT_STATUS,10 ))
                {
                    usbDeviceStatus = USBDeviceStatus.NOT_CONNECT;
                    if (onOpenListener != null)
                    {
                        port.setStatus(SerialComPortStatus.DEVICE_NOT_CONNECT);
                        onOpenListener.dispatch(SerialComPortStatus.DEVICE_NOT_CONNECT.getValue());
                        onOpenListener = null;
                    }
                }
                else
                {
                    usbDeviceStatus = USBDeviceStatus.NO_PERMISSION;
                }
            }
            else if(!SerialComPortStatus.CONNECTED.equals(port.status()))
            {
                if( SerialComPortStatus.CONNECTED.getValue() == intent.getIntExtra(SerialComPortAction.EXTRA_COM_STATUS,10))
                {
                    port.setStatus(SerialComPortStatus.CONNECTED);
                }
                else if (  SerialComPortStatus.DEVICE_NOT_CONNECT.getValue()  == intent.getIntExtra(SerialComPortAction.EXTRA_COM_STATUS,10))
                {
                    port.setStatus( SerialComPortStatus.DEVICE_NOT_CONNECT);
                }
                else if (  SerialComPortStatus.BE_USAGE.getValue()  == intent.getIntExtra(SerialComPortAction.EXTRA_COM_STATUS,10))
                {
                    port.setStatus( SerialComPortStatus.BE_USAGE);
                }
                else
                {
                    port.setStatus( SerialComPortStatus.NOT_CONNECT);
                }
                if (onOpenListener != null)
                {
                    onOpenListener.dispatch( port.statusValue());
                    onOpenListener = null;
                }

            }
            else
            {
                int com = intent.getIntExtra( SerialComPortAction.EXTRA_MESSAGE_COM_ID,0);
                int length = intent.getIntExtra( SerialComPortAction.EXTRA_MESSAGE_LEN,0);
                if(com > 0)
                {
                    if(length > 0)
                    {
                        byte[] datas = intent.getByteArrayExtra(SerialComPortAction.EXTRA_MESSAGE_DATA);
                        port.putMessage(datas, length);
                        if ( receiveHandler != null)
                        {
                            Message message=new Message();
                            message.what=0;
                            message.arg1 = length;
                            message.obj= datas;
                            receiveHandler.sendMessage(message);
                        }
                    }

                }
            }
        }
    }

}
