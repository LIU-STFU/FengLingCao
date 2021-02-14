package com.example.cloudlivingroom;

import java.io.OutputStream;

public class DataOutput extends Thread{
    private OutputStream outputStream=null;
    private byte[] data_byte;
    public DataOutput(String data){
        this.outputStream=ClientThread.outputStream;
        data_byte=data.getBytes();
    }
    @Override
    public void run() {
        try {
            outputStream.write(data_byte,0,2);
        }catch (Exception e){e.printStackTrace();}
    }
}
