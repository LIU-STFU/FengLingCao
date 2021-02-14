package com.example.cloudlivingroom;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    public static String host_addr="192.168.3.34";
    public static int port=8080;
    public static int flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        flag=0;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag==0){
                Snackbar.make(view, "正连接至树莓派", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Thread connect= new Thread(new ClientThread(host_addr,port));
                try {
                    connect.start();
                    connect.join(2000);

                }catch (Exception e){e.printStackTrace();}
                    if (ClientThread.inputStream!=null) {
                        Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                        flag=1;
                    }
                    else Toast.makeText(MainActivity.this,"服务器无响应",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this,"已有连接，正在断开",Toast.LENGTH_SHORT).show();
                    try {
                        ClientThread.socket.shutdownInput();
                        ClientThread.socket.shutdownOutput();
                        flag=0;
                        Snackbar.make(view, "已断开", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                }catch (Exception e){Toast.makeText(MainActivity.this,"更改失败",Toast.LENGTH_SHORT).show();}
                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}