package com.example.tony.bluetoothtest_20190101;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

 public  class CommonTool {
    //測試用，彈跳視窗
     static public void TestAlertMsg(Context context ,String s){
        AlertDialog.Builder al = new AlertDialog.Builder(context);
        al.setTitle("Test");
        al.setMessage(s);
        /*al.setNegativeButton("Cancel", DialogCl);

        al.setNeutralButton("Later", DialogCl);
        al.setPositiveButton("OK", DialogCl);*/
        al.show();


    }

    //閃現訊息
     static public  void ToastAlert(Context context ,String s){
        Toast.makeText(context,s,Toast.LENGTH_LONG).show();
    }
}
