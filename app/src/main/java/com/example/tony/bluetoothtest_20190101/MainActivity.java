package com.example.tony.bluetoothtest_20190101;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_ENABLE_BT = 1;
    private EditText ed_Msg ;
    private Button btn_Send;
    private Button btn_BT_ON;
    private Button btn_BT_OFF;
    private Button btn_Shw_Paired;
    private Button btn_Search;
    private BluetoothAdapter BTAd;
    private ArrayAdapter<String> BTAd_Array;

    ProgressDialog progressdialog;
    Handler  hd2;


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

        //Bluetooth
        BTAd = BluetoothAdapter.getDefaultAdapter();
        BTAd_Array = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);


        //set event
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
                /*final String[] dinner = {"腿庫","雞蛋糕","沙威瑪","澳美客","麵線","麵疙瘩"};
                AlertDialog.Builder dialog_list = new AlertDialog.Builder(MainActivity.this);
                dialog_list.setTitle("利用List呈現");
                dialog_list.setCancelable(false);
                dialog_list.setItems(dinner, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog_list.show();*/




                hd2 =new Handler(){
                    @Override
                    public void handleMessage(Message msg){

                        if(msg.what == 1 ){
                            progressdialog.dismiss();
                        }else if(msg.what == 0) {
                            progressdialog =ProgressDialog.show(MainActivity.this,"","Loading...",true,false);

                            hd2.sendEmptyMessageDelayed(1,10000);
                        }
                    }
                };
                hd2.sendEmptyMessage(0);
            }
        });
        btn_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BTAd == null){
                    CommonTool.ToastAlert(MainActivity.this,"此裝置不支援藍芽");
                }
                else {
                    if(BTAd.isEnabled()){
                        if(BTAd.isDiscovering()){

                        }
                        else {
                            BTAd_Array.clear();
                            BTAd.startDiscovery();
                            CommonTool.ToastAlert(MainActivity.this,"開始尋找");
                            IntentFilter filter;
                            filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                            registerReceiver(BTReceiver,filter);
                        }
                    }
                    else {
                        CommonTool.ToastAlert(MainActivity.this,"藍芽尚未開啟");
                    }

                }

            }
        });
    }

    final BroadcastReceiver BTReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                BTAd_Array.add(device.getName() +"\n"+device.getAddress());
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent result){
            super.onActivityResult(requestCode,resultCode,result);

            switch (resultCode){
                case RESULT_OK:
                    CommonTool.ToastAlert(MainActivity.this,"BT Open Success");
                    break;
                case RESULT_CANCELED:
                    CommonTool.ToastAlert(MainActivity.this,"BT Open failed");
                    break;
            }

    }
}
