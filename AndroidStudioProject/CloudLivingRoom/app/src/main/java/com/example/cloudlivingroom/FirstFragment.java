package com.example.cloudlivingroom;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
public class FirstFragment extends Fragment {
    static Handler handler;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView humidity=(TextView) view.findViewById(R.id.hum);
        TextView temperature=(TextView)view.findViewById(R.id.tem);
        ProgressBar CPU=(ProgressBar)view.findViewById(R.id.cpu_bar);
        ProgressBar MEM=(ProgressBar)view.findViewById(R.id.mem_bar);
        Button button=(Button)view.findViewById(R.id.button);
        Switch fans = (Switch)view.findViewById(R.id.fans_switch);
        Switch widow = (Switch)view.findViewById(R.id.window_switch);
        Switch default_defined = (Switch)view.findViewById(R.id.default_defined);
        EditText ip_edit = (EditText)view.findViewById(R.id.ip_setting);
        EditText port_edit = (EditText)view.findViewById(R.id.port_setting);
        handler=new Handler(){
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 1:{
                        String[] data=new String[4];
                        data=msg.obj.toString().split("-");
                        temperature.setText(data[0]);
                        humidity.setText(data[1]);
                        CPU.setProgress(Integer.parseInt(data[2]));
                        MEM.setProgress(Integer.parseInt(data[3]));
                    }
                    default:{
                        ;
                    }
                }
            }
        };
        default_defined.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    MainActivity.host_addr=ip_edit.getText().toString().replace("默认IP地址：","");
                    MainActivity.port=Integer.parseInt(port_edit.getText().toString().replace("默认端口：",""));
                    try {
                        if (ClientThread.inputStream!=null || ClientThread.outputStream!=null) {
                            ClientThread.socket.shutdownInput();
                            ClientThread.socket.shutdownOutput();
                            MainActivity.flag = 0;
                            Toast.makeText(getContext(), "应用成功，请重新连接", Toast.LENGTH_SHORT).show();
                        }
                        else Toast.makeText(getContext(), "应用成功，请重新连接", Toast.LENGTH_SHORT).show();
                    }catch (Exception e){Toast.makeText(getContext(),"更改失败",Toast.LENGTH_SHORT).show();}
                }
                else {
                    MainActivity.host_addr="192.168.3.34";
                    MainActivity.port=8080;
                    Toast.makeText(getContext(), "当前为默认端口", Toast.LENGTH_SHORT).show();
                }
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.flag==1) {
                    Thread data_in = new Thread(new DataInput(handler));
                    try {
                        data_in.start();
                        data_in.join(1000);
                        Toast.makeText(getContext(), "通信成功", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else Toast.makeText(getContext(),"服务器未连接",Toast.LENGTH_SHORT).show();
            }
        });
        fans.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (MainActivity.flag == 1){
                    if (isChecked) {
                        try {
                            Thread out = new Thread(new DataOutput("F1"));
                            out.start();
                            out.join(10);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            Thread out = new Thread(new DataOutput("F0"));
                            out.start();
                            out.join(10);
                            Toast.makeText(getContext(),"指令发送成功",Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(),"指令发送失败",Toast.LENGTH_SHORT).show();
                        }
                    }
            }
                else Toast.makeText(getContext(),"服务器无连接",Toast.LENGTH_SHORT).show();
            }
        });
        widow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (MainActivity.flag == 1){
                    if (isChecked) {
                        try {
                            Thread out = new Thread(new DataOutput("W1"));
                            out.start();
                            out.join(10);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            Thread out = new Thread(new DataOutput("W0"));
                            out.start();
                            out.join(10);
                            Toast.makeText(getContext(),"指令发送成功",Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(),"指令发送失败",Toast.LENGTH_SHORT).show();
                        }
                    }
            }
                else Toast.makeText(getContext(),"服务器无连接",Toast.LENGTH_SHORT).show();
            }
        });
    }
}