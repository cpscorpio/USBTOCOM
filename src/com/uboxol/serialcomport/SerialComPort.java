package com.uboxol.serialcomport;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by chenpeng on 14-5-6.
 */
public class SerialComPort {

    /**
     * <p>串口ID</p>
     * @author  chenpeng
     * @version Version 0.1
     */
    public static enum COM_ID{
        /**
         * 串口1
         */
        COM_1(1),
        /**
         * 串口2
         */
        COM_2(2),
        /**
         * 串口3
         */
        COM_3(3),
        /**
         * 串口4
         */
        COM_4(4),
        /**
         * 串口5
         */
        COM_5(5);
        private int value;
        COM_ID(int v)
        {
            value = v;
        }
        int getValue()
        {
            return this.value;
        }
    }
    /**
     * <p>停止位</p>
     * @author  chenpeng
     * @version Version 0.1
     */
    public static enum STOP_BITS{
        /**
         * 1bit 停止位
         */
        BIT_1(0),
        /**
         * 1.5 bit 停止位
         */
        BIT_1_5(1),
        /**
         * 2 bit 停止位
         */
        BIT_2(2);
        private int value;
        STOP_BITS(int v)
        {
            this.value = v;
        }
        int getValue()
        {
            return this.value;
        }
    }
    /**
     * <p>校验位</p>
     * @author  chenpeng
     * @version Version 0.1
     */
    public static enum PARITY{
        /**
         * none 校验位
         */
        NONE(0),
        /**
         * Odd 校验位
         */
        ODD(1),
        /**
         * Even 校验位
         */
        EVEN(2);
        private int value;
        PARITY(int v)
        {
            value = v;
        }
        int getValue()
        {
            return this.value;
        }
    }
    /**
     * <p>数据位</p>
     * @author  chenpeng
     * @version Version 0.1
     */
    public static enum DATA_BITS{
        /**
         * 8bit 数据位
         */
        BIT_8(8),
        /**
         * 7bit 数据位
         */
        BIT_7(7);
        private int value;
        DATA_BITS(int v)
        {
            value = v;
        }
        int getValue()
        {
            return this.value;
        }
    }

    /**
     * <p>串口打开状态</p>
     * @author  chenpeng
     * @version Version 0.1
     */
    public static enum SerialComPortStatus {
        /**
         * 串口没有连接状态
         */
        NOT_CONNECT(-1),
        /**
         * 串口已经连接状态
         */
        CONNECTED(0),
        /**
         * USB设备没有打开
         */
        DEVICE_NOT_CONNECT(1),
        /**
         * 没有获取到USB访问权限
         */
        DEVICE_NO_PERMISSION(2),
        /**
         * 串口被占用
         */
        BE_USAGE(3),
        /**
         * 无效
         */
        CONNECTING(4);

        private int value;

        SerialComPortStatus(int v)
        {
            this.value = v;
        }

        public  int getValue()
        {
            return this.value;
        }
    }

    private int baudRate = 9600;   // 110 / 300 / 600 / 9600/ 115200 /256000

    private STOP_BITS stopBits = STOP_BITS.BIT_1;   //0=1stop bit, 1=1.5 stop bit, 2=2 stop bit;

    private PARITY parity = PARITY.NONE;     //0=none, 1=Odd(奇校验), 2=Even(偶校验)

    private COM_ID portId = COM_ID.COM_1;

    private DATA_BITS dataBits = DATA_BITS.BIT_8;   //7 or 8

    private SerialComPortStatus serialComPortStatus = SerialComPortStatus.NOT_CONNECT;

    private MessageBuffer messageBuffer = null;  //2M

    private Context context;

    private String receiveAction;

    public SerialComPort(Context context, String receiveAction)
    {
        this.context = context;
        this.receiveAction = receiveAction;
    }


    public void open(COM_ID com_id,int baudRate, STOP_BITS stopBits, DATA_BITS dataBits, PARITY parity)
    {

        this.portId = com_id;
        this.stopBits = stopBits;
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.parity = parity;
        this.messageBuffer = new MessageBuffer(5 * this.baudRate);
        this.open();
    }

    public void open()
    {
        Intent intent = new Intent(SerialComPortAction.SERVICE_OPEN_COM);
        intent.putExtra(SerialComPortAction.APP_ACTION, this.receiveAction);
        intent.putExtra(SerialComPortAction.CONFIG_COM, this.portId.getValue());
        intent.putExtra(SerialComPortAction.CONFIG_BIT_RATE, this.baudRate);
        intent.putExtra(SerialComPortAction.CONFIG_DATA_TYPE, this.dataBits.getValue());
        intent.putExtra(SerialComPortAction.CONFIG_PARITY_TYPE, this.parity.getValue());
        intent.putExtra(SerialComPortAction.CONFIG_STOP_BITS, stopBits.getValue());
        context.sendBroadcast(intent);
    }

    public void send(String message) throws WriteSerialDataException {

        this.send(message.getBytes(), message.getBytes().length);
    }
    public void send( byte[] data, int len) throws WriteSerialDataException {
        if (isConnected())
        {
            Intent intent = new Intent(SerialComPortAction.SerialComPorts[ portId.getValue()]);
            intent.putExtra(SerialComPortAction.APP_ACTION, receiveAction);
            intent.putExtra(SerialComPortAction.EXTRA_MESSAGE_COM_ID, portId.getValue());
            intent.putExtra(SerialComPortAction.EXTRA_MESSAGE_DATA,data);
            intent.putExtra(SerialComPortAction.EXTRA_MESSAGE_LEN,len);
            context.sendBroadcast(intent);
        }
        else
        {
            throw new WriteSerialDataException("Can't send data to not open SerialComPort !");
        }
    }

    public int read( byte[] datas, int length) throws ReadSerialDataException {

        if (this.messageBuffer != null && isConnected()){
            return messageBuffer.read(datas, 0, length);
        }
        else
        {
            throw new ReadSerialDataException("Can't read data from not open SerialComPort !" + (isConnected() ? "connected!":" not connected"));
        }
    }

    public boolean isConnected()
    {
        return serialComPortStatus == SerialComPortStatus.CONNECTED;
    }
    public void putMessage( byte[] datas, int length)
    {
        if (messageBuffer != null)
        {
            messageBuffer.put( datas, 0, length);
        }

    }
    public int statusValue()
    {
        return serialComPortStatus.getValue();
    }

    public SerialComPortStatus status()
    {
        return serialComPortStatus;
    }

    public void setStatus(SerialComPortStatus status)
    {
        Log.d("serialComPortStatus change",this.serialComPortStatus.toString() + " to " + status.toString());
        this.serialComPortStatus = status;
    }

    public boolean isConnecting()
    {
        return serialComPortStatus == SerialComPortStatus.CONNECTING;
    }
    public void close()
    {
        this.serialComPortStatus = SerialComPortStatus.NOT_CONNECT;
        Log.d(" port close ", serialComPortStatus.toString() + " " + serialComPortStatus.getValue());
        this.messageBuffer = null;
    }
}
