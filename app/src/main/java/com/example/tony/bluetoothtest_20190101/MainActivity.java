package com.example.tony.bluetoothtest_20190101;

import android.os.Looper;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.support.v4.app.ActivityCompat;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.support.v4.content.ContextCompat;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_ENABLE_BT = 1;
    private interface BTSearch{
        public static final int START = 0;
        public static final int FINISHEND=1;
        public static final int END = 2;
    };
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private EditText ed_Msg ;
    private RadioGroup rgMode;
    private Button btn_Send;
    private Button btn_BT_ON;
    private Button btn_BT_OFF;
    private Button btn_Shw_Paired;
    private Button btn_Search;
    private BluetoothAdapter BTAd;
    private ArrayAdapter<String> BTAd_Array;
    private ArrayList<String> BTMAC_Array;
    ProgressDialog progressdialog;
    Handler  BT_Handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){
        //view
        ed_Msg = (EditText)findViewById(R.id.ed_Msg);
        btn_Send = (Button)findViewById(R.id.btn_Send);
        btn_BT_ON = (Button)findViewById(R.id.btn_BT_ON);
        btn_BT_OFF = (Button)findViewById(R.id.btn_BT_OFF);
        btn_Shw_Paired = (Button)findViewById(R.id.btn_Shw_Paired);
        btn_Search = (Button)findViewById(R.id.btn_Search);
        rgMode = (RadioGroup)findViewById(R.id.rgBTMode);

        //Bluetooth
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ENABLE_BT);
        }
        BTAd = BluetoothAdapter.getDefaultAdapter();
        BTAd_Array = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        BTMAC_Array = new ArrayList<String>();

        //set event
        BT_Handler =new Handler(){
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case BTSearch.START:
                        progressdialog =ProgressDialog.show(MainActivity.this,"","Searching...",true,false);

                        BT_Handler.sendEmptyMessageDelayed(BTSearch.FINISHEND,20000);
                        break;
                    case BTSearch.FINISHEND:
                        BTAd.cancelDiscovery();
                        progressdialog.dismiss();
                        CommonTool.ToastAlert(MainActivity.this,"結束搜尋...");
                        BT_Handler.sendEmptyMessageDelayed(BTSearch.END,100);
                        break;
                    case BTSearch.END:
                        createAlertListView();
                        break;
                }

            }
        };
        btn_BT_ON.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BTAd == null){
                    CommonTool.ToastAlert(MainActivity.this,"此裝置不支援藍芽");
                }
                else {
                    if(!BTAd.isEnabled()){
                        Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBT,REQUEST_ENABLE_BT);
                    }
                }
            }
        });
        btn_Shw_Paired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPairedDevices();
                createAlertListView();
            }
        });

        //按下搜尋鈕
        btn_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //支不支援藍芽
                if(BTAd == null){
                    CommonTool.ToastAlert(MainActivity.this,"此裝置不支援藍芽");
                }
                else {
                    //再看有沒有打開藍芽
                    if(BTAd.isEnabled()){
                        //避免重複開啟搜尋，判斷是否已經開始搜尋
                        if(BTAd.isDiscovering()){

                        }
                        else {
                            /*尚未搜尋，
                                1.清空裝置List
                                2.搜索藍芽
                                3.開始計時
                                4.註冊BroadcastReceiver
                             */

                            CommonTool.ToastAlert(MainActivity.this,"開始尋找");
                            BTAd_Array.clear();
                            BTMAC_Array.clear();
                            BTAd.startDiscovery();



                            IntentFilter filter;
                            filter = new IntentFilter();
                            filter.addAction(BluetoothDevice.ACTION_FOUND);
                            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                            registerReceiver(BTReceiver,filter);
                            BT_Handler.sendEmptyMessage(BTSearch.START);
                        }
                    }
                    else {
                        CommonTool.ToastAlert(MainActivity.this,"藍芽尚未開啟");
                    }

                }

            }
        });
    }
    //接收藍芽回傳廣播
    final BroadcastReceiver BTReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //判斷是否為藍芽回傳的廣播
            String action = intent.getAction();

            switch (action) {
                case BluetoothDevice.ACTION_FOUND://找到藍芽
                    //取得搜尋到的裝置訊息
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    CommonTool.ToastAlert(MainActivity.this,"Find BT:"+device.getName());
                    BTArray_Setting(device);
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED://
                    CommonTool.ToastAlert(MainActivity.this,action);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED://開始探索
                    CommonTool.ToastAlert(MainActivity.this,action);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED://探索結束
                    CommonTool.ToastAlert(MainActivity.this,action);
                    break;
                default:
                    CommonTool.ToastAlert(MainActivity.this,action);
                    break;
            }

        }
    };
    public void callThreadToast(final String Msg){
        new Handler(Looper.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(MainActivity.this,Msg,Toast.LENGTH_SHORT).show();
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent result){
            super.onActivityResult(requestCode,resultCode,result);

            switch (resultCode){
                case RESULT_OK:
                    CommonTool.ToastAlert(MainActivity.this,"BT Open Success");
                    if(rgMode.getCheckedRadioButtonId() == R.id.rdoServer){
                        OpenDiscoverable();
                        StartServerSide();
                    }
                    break;
                case RESULT_CANCELED:
                    CommonTool.ToastAlert(MainActivity.this,"BT Open failed");
                    break;
            }

    }
    private void getPairedDevices(){
        java.util.Set<BluetoothDevice> prDevices = BTAd.getBondedDevices();
        if(prDevices.size() > 0 ){
            for(BluetoothDevice BTDv : prDevices){
                BTArray_Setting(BTDv);
            }
        }
    }
    private void createAlertListView(){
        int List_Count = 0;
        List_Count = BTAd_Array.getCount();

        if(List_Count >0){
            AlertDialog.Builder dialog_list = new AlertDialog.Builder(MainActivity.this);
            dialog_list.setTitle("已搜索到的裝置");
            dialog_list.setCancelable(true);
            dialog_list.setAdapter(BTAd_Array, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String MAC = BTMAC_Array.get(which);
                    if(MAC != null &&  MAC !="" && MAC.length() !=0){
                        StartBTConnect(MAC);
                    }

                }
            });
            dialog_list.show();
        }
        else{
          CommonTool.ToastAlert(MainActivity.this,"沒找到裝置");
        }

    }
    private void BTArray_Setting(BluetoothDevice BD){
        BTAd_Array.add(BD.getName() +"\n"+BD.getAddress());
        BTMAC_Array.add(BD.getAddress());
    }
    //打開可被探索模式，300秒
    private void OpenDiscoverable(){
        Intent discoverableI = new Intent(BTAd.ACTION_REQUEST_DISCOVERABLE);

        discoverableI.putExtra(BTAd.EXTRA_DISCOVERABLE_DURATION,300);
        startActivity(discoverableI);
    }
    private void StartBTConnect(String sltMAC){
        final String MAC  =sltMAC;
        BluetoothDevice BD = BTAd.getRemoteDevice(MAC);
        //BTAd.cancelDiscovery();
        CommonTool.ToastAlert(MainActivity.this,"In StartBTConnect");
        //匿名繼承 Thread
        new BTClientThread(BD).start();
    }

    private void StartServerSide(){
        final BluetoothServerSocket BTSS ;
        BluetoothServerSocket tmp = null;
        try{

            tmp = BTAd.listenUsingRfcommWithServiceRecord("TestServer",BT_UUID);
            callThreadToast("Start Waiting");

        }catch (IOException ioe){
            callThreadToast("Server listen Err");
        }
        BTSS =tmp;
        new Thread(){
            @Override
            public void run() {
                super.run();
                BluetoothSocket BS=null;
                BluetoothDevice BD;
                while(true){
                    try{
                        BS = BTSS.accept();
                        callThreadToast("Waiting.....");
                    }
                    catch (IOException ioe){
                        callThreadToast("accept Err");

                    }
                    if(BS != null){
                        BD = BS.getRemoteDevice();
                        callThreadToast("get device:"+BD.getName());

                        break;
                    }
                }

            }
        }.start();


    }

    private class BTClientThread extends Thread{
        private BluetoothDevice BD;
        BluetoothSocket BTS;
        public BTClientThread(BluetoothDevice BD){
            this.BD = BD;
        }
        @Override
        public void run(){

            //Create Socket
            if(BD != null){
                try{
                    BTS =BD.createRfcommSocketToServiceRecord(BT_UUID);
                    callThreadToast("Start Connect");
                }catch (IOException ioe){
                    callThreadToast("IO Err for get Socket");
                }
            }
            //Start Connect
            if(BTS != null){
                try{
                    BTS.connect();
                    callThreadToast("Connecting....");
                }catch (IOException ioe){
                    callThreadToast("IO Err for  Connect");

                    try{
                        BTS.close();
                    }
                    catch (IOException ioe_c){
                        callThreadToast("IO Err for  connect fail to Close");

                    }
                }
            }

        }
    }
}
