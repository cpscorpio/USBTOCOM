package com.uboxol.usbtocom;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import com.uboxol.serialcomport.SerialComPort;
import com.uboxol.serialcomport.SerialComPortControl;
import com.uboxol.serialcomport.SerialPortListener;
import com.uboxol.serialcomport.WriteSerialDataException;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    int bitRate;    /* 110 / 300 / 9600/ 115200 ...  */
    SerialComPort.STOP_BITS stopBits;   /* 0=1 stop bit, 1=1.5 stop bit, 2=2 stop bit;  */
    SerialComPort.DATA_BITS dataType;   /* 8:8bit, 7: 7bit */
    SerialComPort.PARITY parityType; /* 0: none, 1: odd, 2: even */
    SerialComPort.COM_ID com ;       /* 串口编号:1~5 代表串口1到串口5)*/;

    int writeSize = 0;
    int readSize = 0;

    Spinner bitRateSpinner;;
    Spinner stopBitsSpinner;
    Spinner dataTypeSpinner;
    Spinner parityTypeSpinner;
    Spinner comSpinner;

    Button writeButton;
    Button configButton;
    TextView readTextView,WriteBytes,ReadBytes;
    EditText writeEditText;

    private SerialComPortControl serialComPortControl;
    private ReceiveThread receiveThread = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        writeSize = 0;
        readSize = 0;

        try {
             /* setup the baud rate list */
            bitRateSpinner = (Spinner) findViewById(R.id.baudRateValue);
            ArrayAdapter<CharSequence> baudAdapter = ArrayAdapter.createFromResource(this, R.array.baud_rate,
                    R.layout.my_spinner_textview);
            baudAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
            bitRateSpinner.setAdapter(baudAdapter);
            bitRateSpinner.setGravity(0x10);
            bitRateSpinner.setSelection(3);
            bitRateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    Log.i("bitRateSpinner", parent.getItemAtPosition(pos).toString());
                    bitRate = Integer.parseInt(parent.getItemAtPosition(pos).toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
		/* by default it is 9600 */
            bitRate = 9600;

		/* stop bits */
            stopBitsSpinner = (Spinner) findViewById(R.id.stopBitValue);
            ArrayAdapter<CharSequence> stopAdapter = ArrayAdapter.createFromResource(this, R.array.stop_bits,
                    R.layout.my_spinner_textview);
            stopAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
            stopBitsSpinner.setAdapter(stopAdapter);
            stopBitsSpinner.setGravity(0x01);
            stopBitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    Log.i("stopBitsSpinner", parent.getItemAtPosition(pos).toString());
                    String s_stopBit = parent.getItemAtPosition(pos).toString();
                    if(s_stopBit.equals("1"))
                    {
                        stopBits = SerialComPort.STOP_BITS.BIT_1;
                    }
                    else if(s_stopBit.equals("1.5"))
                    {
                        stopBits = SerialComPort.STOP_BITS.BIT_1_5;
                    }
                    else if(s_stopBit.equals("2"))
                    {
                        stopBits = SerialComPort.STOP_BITS.BIT_2;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
		/* default is stop bit 1 */
            stopBits = SerialComPort.STOP_BITS.BIT_1;

           /* daat bits */
            dataTypeSpinner = (Spinner) findViewById(R.id.dataBitValue);
            ArrayAdapter<CharSequence> dataAdapter = ArrayAdapter.createFromResource(this, R.array.data_bits,
                    R.layout.my_spinner_textview);
            dataAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
            dataTypeSpinner.setAdapter(dataAdapter);
            dataTypeSpinner.setSelection(1);
            /* default data bit is 8 bit */
            dataType = SerialComPort.DATA_BITS.BIT_8;
            dataTypeSpinner.setGravity(0x11);
            dataTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    Log.i("dataTypeSpinner", parent.getItemAtPosition(pos).toString());

                    String s_dataType = parent.getItemAtPosition(pos).toString();

                    if("7".equals(s_dataType))
                    {
                        dataType = SerialComPort.DATA_BITS.BIT_7;
                    }
                    else if("8".equals(s_dataType))
                    {
                        dataType = SerialComPort.DATA_BITS.BIT_8;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });


		    /* parity */
            parityTypeSpinner = (Spinner) findViewById(R.id.parityValue);
            ArrayAdapter<CharSequence> parityAdapter = ArrayAdapter.createFromResource(this, R.array.parity,
                    R.layout.my_spinner_textview);
            parityAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
            parityTypeSpinner.setAdapter(parityAdapter);
            parityTypeSpinner.setGravity(0x11);
            parityTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                    Log.i("parityTypeSpinner", parent.getItemAtPosition(pos).toString());
                    String parityString = new String(parent.getItemAtPosition(pos).toString());
                    if (parityString.compareTo("None") == 0) {
                        parityType = SerialComPort.PARITY.NONE;
                    }

                    if (parityString.compareTo("Odd") == 0) {
                        parityType = SerialComPort.PARITY.ODD;
                    }

                    if (parityString.compareTo("Even") == 0) {
                        parityType = SerialComPort.PARITY.EVEN;
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
		/* default is none */
            parityType = SerialComPort.PARITY.NONE;

		/* flow control */
            comSpinner = (Spinner) findViewById(R.id.flowControlValue);
            ArrayAdapter<CharSequence> flowAdapter = ArrayAdapter.createFromResource(this, R.array.flow_control,
                    R.layout.my_spinner_textview);
            flowAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
            comSpinner.setAdapter(flowAdapter);
            comSpinner.setGravity(0x11);
            comSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    Log.i("comSpinner", parent.getItemAtPosition(pos).toString());
                    String comString = new String(parent.getItemAtPosition(pos).toString());
                    if (comString.compareTo("COM1") == 0) {
                        com = SerialComPort.COM_ID.COM_1;
                    }

                    if (comString.compareTo("COM2") == 0) {
                        com = SerialComPort.COM_ID.COM_2;
                    }

                    if (comString.compareTo("COM3") == 0) {
                        com = SerialComPort.COM_ID.COM_3;
                    }

                    if (comString.compareTo("COM4") == 0) {
                        com = SerialComPort.COM_ID.COM_4;
                    }

                    if (comString.compareTo("COM5") == 0) {
                        com = SerialComPort.COM_ID.COM_5;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
		/* default flow control is is none */
            com = SerialComPort.COM_ID.COM_1;

            configButton = (Button)findViewById(R.id.configButton);
            writeButton = (Button)findViewById(R.id.WriteButton);
            readTextView = (TextView)findViewById(R.id.readTextView);
            readTextView.setText("");

            WriteBytes = (TextView)findViewById(R.id.WriteBytes);
            ReadBytes = (TextView)findViewById(R.id.ReadBytes);
            writeEditText = (EditText)findViewById(R.id.WriteValues);

            configButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.i("OnClickListener","setOnClickListener " + serialComPortControl.serialComPortStatus().getValue());
                    stopReadData();
                    if ( serialComPortControl.serialComPortStatus().equals(SerialComPort.SerialComPortStatus.CONNECTED))
                    {
                        serialComPortControl.close();
                        configButton.setText("打开");
                        readTextView.setText(readTextView.getText() + "串口已关闭\n");
                        return;
                    }
                    serialComPortControl.open(com, bitRate,dataType,stopBits,parityType, new SerialPortListener(){
                        @Override
                        public void dispatch(int flag) {
//                            Log.i("serialComPortControl.open " ,"" + flag);
//                            readTextView.setText(readTextView.getText() +"serialComPortControl.open " + flag + "\n");
//                            switch (flag)
//                            {
//                                case 0:{
//                                    startReadData();
//                                    configButton.setText("关闭");
//                                    readTextView.setText(readTextView.getText() + "串口打开成功\n");
//                                    break;
//                                }
//                                case 1:{
//                                    readTextView.setText(readTextView.getText() + "USB设备未连接\n");
//                                    break;
//                                }
//                                case 2:{
//                                    readTextView.setText(readTextView.getText() + "USB设备没有权限\n");
//                                    break;
//                                }
//                                case 3:{
//                                    readTextView.setText(readTextView.getText() + "端口被占用\n");
//                                    break;
//                                }
//                                default:{
//                                    readTextView.setText(readTextView.getText() + "串口未打开\n");
//                                    break;
//                                }
//                            }

                            Log.i("port status", serialComPortControl.serialComPortStatus().toString());
                            switch (serialComPortControl.serialComPortStatus())
                            {
                                case CONNECTED:{
                                    startReadData();
                                    configButton.setText("关闭");
                                    readTextView.setText(readTextView.getText() + "串口打开成功\n");
                                    break;
                                }
                                case NOT_CONNECT:{
                                    readTextView.setText(readTextView.getText().toString() + "未打开串口\n");
                                    break;
                                }
                                case BE_USAGE:{
                                    readTextView.setText(readTextView.getText().toString() + "串口被占用\n");
                                    break;
                                }
                                case DEVICE_NO_PERMISSION:{
                                    readTextView.setText(readTextView.getText().toString() + "设备没有获取权限\n");
                                    break;
                                }
                                case DEVICE_NOT_CONNECT:{
                                    readTextView.setText(readTextView.getText().toString() + "设备未连接\n");
                                    break;
                                }
                            }

                        }
                    });
                }
            });
            writeEditText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                    Tools.info("keyCode " + keyCode);
                    Tools.info("keyEvent " + keyEvent);
                    if(keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN)
                    {
                        sendMessage();
                    }
                    return false;
                }
            });
            writeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    sendMessage();
                }
            });


            serialComPortControl = new SerialComPortControl("uboxol.usbhost.USBTOCOM", this);
//            serialComPortControl = new SerialComPortControl("uboxol.usbhost.USBTOCOM", myHandler, this);   //传入myHandler可以直接接受消息，无需使用read方法读取

            serialComPortControl.setOnCloseListener(new SerialPortListener() {
                @Override
                public void dispatch(int flag) {
                    stopReadData();
                }
            });

        }catch (Exception e)
        {
            e.printStackTrace();
            Log.e("ERROR",e.toString());
        }


    }

    private void startReadData()
    {
        if (receiveThread != null && receiveThread.isRunning())
        {
            //do nothing
        }
        else
        {
            receiveThread = new ReceiveThread();
            receiveThread.start();
        }
    }
    private void stopReadData()
    {
        if (receiveThread != null)
        {
            receiveThread.cancel();
            receiveThread = null;
        }
    }


    private void sendMessage()
    {
        switch (serialComPortControl.serialComPortStatus())
        {
            case CONNECTED:{
                String message = writeEditText.getText().toString();
                if (message.length() > 0)
                {
                    try {
                        serialComPortControl.send(message);
                        writeSize += message.getBytes().length;
                        WriteBytes.setText("Write\n(" + writeSize + ")");
                    } catch (WriteSerialDataException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            case NOT_CONNECT:{
                readTextView.setText(readTextView.getText().toString() + "未打开串口\n");
                break;
            }
            case BE_USAGE:{
                readTextView.setText(readTextView.getText().toString() + "串口被占用\n");
                break;
            }
            case DEVICE_NO_PERMISSION:{
                readTextView.setText(readTextView.getText().toString() + "设备没有获取权限\n");
                break;
            }
            case DEVICE_NOT_CONNECT:{
                readTextView.setText(readTextView.getText().toString() + "设备未连接\n");
                break;
            }
        }

    }


    class ReceiveThread extends Thread{
        private boolean flag;

        public ReceiveThread()
        {
            flag = true;
        }
        @Override
        public void run() {
            while (serialComPortControl.serialComPortStatus().equals(SerialComPort.SerialComPortStatus.CONNECTED) && flag)
            {
                try
                {
                    byte[] b = new byte[100];
                    int length = serialComPortControl.read( b,100,100);
                    if (length > 0)
                    {
                        Log.i("read",length + " bit");
                        Message msg = new Message();
                        msg.what= 0;
                        msg.arg1 = length;
                        msg.obj = b;
                        myHandler.sendMessage(msg);
                    }
                }
                catch (Exception e)
                {
                    Log.d("error",e.getMessage());
                }
            }

            flag = false;
        }

        public void cancel()
        {
            flag = false;
        }

        public boolean isRunning()
        {
            return flag;
        }
    }
    public Handler myHandler = new Handler() {

        @Override
        public void dispatchMessage(Message msg) {

            switch (msg.what) {
                case 0:
                    byte[] arr = (byte[]) msg.obj;  // 这里是可以收到值的
                    readTextView.setText(readTextView.getText().toString() + "receive : " + new String(arr) + "\n");
                    readSize += msg.arg1;           //data length
                    ReadBytes.setText("Write\\n(" + readSize + ")");
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    protected void onDestroy() {
        stopReadData();
        serialComPortControl.close();
        super.onDestroy();
    }

    @Override
    public void onStart()
    {
       super.onStart();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }


}
