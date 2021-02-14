package com.example.cloudlivingroom;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class ClientThread extends Thread{
    public static Socket socket=null;
    public static InputStream inputStream=null;
    public static OutputStream outputStream=null;
    private String host_addr="";
    private int port;
    public ClientThread(String host_addr,int port){
        this.host_addr=host_addr;
        this.port=port;
    }
    @Override
    public void run() {
        try {
            socket=new Socket(host_addr,port);
            inputStream=socket.getInputStream();
            outputStream=socket.getOutputStream();
        }catch (Exception e){e.printStackTrace();}
    }
}
