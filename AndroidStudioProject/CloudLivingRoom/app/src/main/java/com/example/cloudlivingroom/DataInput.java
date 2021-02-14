package com.example.cloudlivingroom;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

public class DataInput extends Thread{
    private InputStream inputStream=null;
    private Handler handler;
    public DataInput(Handler handler){
        this.inputStream=ClientThread.inputStream;
        this.handler=handler;
    }
    @Override
    public void run() {
        while (true){
            try {
                byte[] info=new byte[15];
                SystemClock.sleep(2000);
                if (inputStream.available()!=0) {
                    int count = inputStream.read(info,0,15);
                    String infos = new String(info);
                    Message msginfo = new Message();
                    msginfo.what = 1;
                    msginfo.obj = infos;
                    msginfo.setTarget(handler);
                    msginfo.sendToTarget();
                }
                }
            catch (IOException e){e.getStackTrace();}
        }
    }
}
