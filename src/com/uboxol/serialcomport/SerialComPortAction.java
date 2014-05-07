package com.uboxol.serialcomport;


public class SerialComPortAction {

    public final static String SERVICE_INIT_MESSAGE = "ubox.service.init";
    public final static String EXTRA_INIT_STATUS = "device_status";

    //打开COM
    public final static String SERVICE_OPEN_COM = "ubox.service.open";
    public final static String EXTRA_COM_STATUS = "usbconfig_status";

    //关闭COM
    public final static String SerialComPort_DISCONNECT = "ubox.service.disconnect";

    public final static String SerialComPort1_MESSAGE = "ubox.message.com1";
    public final static String SerialComPort2_MESSAGE = "ubox.message.com2";
    public final static String SerialComPort3_MESSAGE = "ubox.message.com3";
    public final static String SerialComPort4_MESSAGE = "ubox.message.com4";
    public final static String SerialComPort5_MESSAGE = "ubox.message.com5";



    public final static String[] SerialComPorts = {"",
            SerialComPort1_MESSAGE,
            SerialComPort2_MESSAGE,
            SerialComPort3_MESSAGE,
            SerialComPort4_MESSAGE,
            SerialComPort5_MESSAGE
    };


    public final static String CONFIG_BIT_RATE = "bitrate";
    public final static String CONFIG_STOP_BITS = "stop_bits";
    public final static String CONFIG_PARITY_TYPE = "paritytype";
    public final static String CONFIG_DATA_TYPE = "datatype";
    public final static String CONFIG_COM = "com";

    public final static String APP_ACTION = "action";


    /**
     * EXTRA for message
     */
    public final static String EXTRA_MESSAGE_COM_ID = "com";
    public final static String EXTRA_MESSAGE_LEN = "len";
    public final static String EXTRA_MESSAGE_DATA = "datas";

}

