package com.example.tony.bluetoothtest_20190101;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

interface BTIO{
    public static final int MESSAGE_READ = 3;
    public static final int MESSAGE_WRITE=4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_START_COMM = 6;
};

interface OnBTListener{
    void ListenMsg(int MsgMode,Object o);
}
public class BluetoothTool {
    Context c;
    BTClientThread BC;
    BluetoothDevice BD;
    OnBTListener OL;
    Boolean SetCB = false;
    public BluetoothTool(Context c, BluetoothDevice BD){
        this.c = c;
        this.BD = BD;
    }
    public void SetMsgListener(OnBTListener OL){
        this.OL = OL;
        BC = new BTClientThread(BD,OL);
        SetCB = true;
    }
    public void StartConnect(){
        if(SetCB)
            BC.start();
        else
            Toast.makeText(c,"Not Set Listener",Toast.LENGTH_LONG).show();
    }
}


class BTClientThread extends Thread{
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothDevice BD;
    BTCommunication BT_Comm;
    OnBTListener OL;
    BluetoothSocket BTS;
    Boolean connFail = false;
    public BTClientThread(BluetoothDevice BD,OnBTListener OL){
        this.BD = BD;
        this.OL = OL;
    }
    @Override
    public void run(){

        //Create Socket
        if(BD != null){
            try{
                BTS =BD.createRfcommSocketToServiceRecord(BT_UUID);
                OL.ListenMsg(BTIO.MESSAGE_TOAST,"Start Connect");
            }catch (IOException ioe){
                OL.ListenMsg(BTIO.MESSAGE_TOAST,"IO Err for get Socket");
                connFail = true;
            }
        }
        //Start Connect
        if(BTS != null){
            try{
                BTS.connect();
                OL.ListenMsg(BTIO.MESSAGE_TOAST,"Connecting....");
            }catch (IOException ioe){
                //callThreadToast("IO Err for  Connect");
                OL.ListenMsg(BTIO.MESSAGE_TOAST,"IO Err for get Socket");
                connFail = true;
                try{
                    BTS.close();
                }
                catch (IOException ioe_c){
                    OL.ListenMsg(BTIO.MESSAGE_TOAST,"IO Err for  connect fail to Close");

                    connFail = true;
                }
            }
        }
        //開啟溝通管道
        if(!connFail){
            BT_Comm = new BTCommunication(BTS,OL);
            BT_Comm.start();
            OL.ListenMsg(BTIO.MESSAGE_START_COMM,BT_Comm);
        }
    }


}

class BTCommunication extends Thread{
    OnBTListener OL;
    private final BluetoothSocket BTS;
    private final InputStream InS;
    private final OutputStream OutS;
    public BTCommunication(BluetoothSocket BTS,OnBTListener OL){
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        this.BTS = BTS;
        this.OL = OL;
        try{
            tmpIn = BTS.getInputStream();
            tmpOut = BTS.getOutputStream();
        }catch (IOException ioe){
            OL.ListenMsg(BTIO.MESSAGE_TOAST,"IO Err for  IOStream");
        }

        InS = tmpIn;
        OutS = tmpOut;

    }

    @Override
    public void run() {
        super.run();
        int MaxLen = 1024;
        int Len;
        byte[] buffer = new byte[MaxLen];
        SendBTMessage sMsg = new SendBTMessage();

        while(true){
            try{
                //判斷已有資料進來
                Len = InS.available();

                if(Len != 0){
                    //等候資料讀取
                    SystemClock.sleep(100);
                    //讀取目前資料長度
                    Len = InS.available();

                    if(Len > MaxLen){
                        Len = MaxLen;
                    }

                    Len = InS.read(buffer,0,Len);
                    sMsg.MsgLen = Len;
                    sMsg.buffer = buffer;
                    OL.ListenMsg(BTIO.MESSAGE_READ,sMsg);
                }

            }catch (IOException ioe){
                OL.ListenMsg(BTIO.MESSAGE_TOAST,"IO Err for Read Data");
            }

        }
    }

    public void write(String input){
        byte[] bytes = input.getBytes();
        try{
            OutS.write(bytes);
        }catch (IOException ioe){
            OL.ListenMsg(BTIO.MESSAGE_TOAST,"IO Err for Write Data");
        }

    }
}
class SendBTMessage{
    public int MsgLen;
    public byte[] buffer;
    public SendBTMessage(){

    }

}


