#include <Chrono.h>
#include <LightChrono.h>
#include <String.h>
#include <Servo.h>
#include <dht11.h>
#include "Nokia_5110.h"
#define RST 31
#define CE 33
#define DC 35
#define DIN 37
#define CLK 39
Nokia_5110 lcd = Nokia_5110(RST, CE, DC, DIN, CLK);
Chrono chrono;
//COMMAND
#define SEND_DATA "S"//请给上位机汇报传感器数据
#define RECV_COMMAND "R"//请接收上位机指令并完成相应动作
//POSITION
#define KITCHEN "K"
#define LIVINGROOM "L"
//ACTION
#define SHUT_DOWN "SD"
#define OPEN "OP"
//DEVICE PIN
#define DHT11_PIN 10
#define MQ_PIN 11
#define D_PIN 12
#define LIVINGROOM_SERVO_PIN 13
#define KITCHEN_SERVO_PIN 22
Servo servo1;//实例化一个舵机客厅
Servo servo2;//餐厅
dht11 DHT11;//实例化一个dht11传感器
static String command,CHK,TEM,HUM,MQS,APP,Data;
static int chk,tem,hum,MQ_state,approach,current_deg1,change1,min1,current_deg2,change2,min2,State;
static char temp[12];
struct DEVICES{
  String ROOM;
  String Device_ID;
  int Device_PIN;
  }DEVICE_INFO[8]{{LIVINGROOM,"W0",0},{LIVINGROOM,"F0",4},{LIVINGROOM,"A0",5},{LIVINGROOM,"E0",6},
                  {KITCHEN,"W0",0},   {KITCHEN,"F0",7},   {KITCHEN,"A0",8},   {KITCHEN,"E0",9}
                  };
static struct DEVICES *p;
void(* resetFunc) (void) = 0;
void go_servo1(int degree){
  current_deg1=servo1.read();
  change1=((degree-current_deg1)/abs(degree-current_deg1));
  lcd.clear();
  lcd.print("servo1 working..");
  for(min1=current_deg1;min1!=degree;){
  servo1.write(min1);
  min1+=change1;
  delay(5);
  }
  }
void go_servo2(int degree){
  current_deg2=servo2.read();
  change2=((degree-current_deg2)/abs(degree-current_deg2));
  lcd.clear();
  lcd.print("servo1 working..");
  for(min2=current_deg2;min2!=degree;){
  servo2.write(min2);
  min2+=change2;
  delay(5);
  }
  }
void COMMAND(String RS,String Room,String device_id,String action,String rem){
  if (RS.equals(SEND_DATA)){
    ;
    }
    else if (RS.equals(RECV_COMMAND)){
      if(action.equals(SHUT_DOWN)){State=0;}
      else if(action.equals(OPEN)){State=1;}
      else State=0;
      for(p=DEVICE_INFO;p<DEVICE_INFO+8;p++)
      {
        if(p->ROOM==Room && p->Device_ID==device_id && device_id!="W0"){
          digitalWrite(p->Device_PIN,State);
          break;
          }
          else if(p->ROOM==Room && p->Device_ID==device_id && device_id=="W0")
          {
            if(Room.equals(LIVINGROOM)){
              go_servo1((int)(rem.toInt()*180/100));
              }
              else go_servo2((int)(rem.toInt()*180/100));
            }
        }
      }
      else {
      lcd.clear();
      lcd.println("Invalid Data");
      lcd.println("Restarting...");
      delay(3000);
      resetFunc();
      }
  }
  String get_data(){
  CHK="";
  TEM="";
  HUM="";
  MQS="";
  APP="";
  chk = DHT11.read(DHT11_PIN); //dht11校验码            
  tem = (int)DHT11.temperature;      //dht11温度数据      
  hum = (int)DHT11.humidity;   //dht11湿度数据             
  MQ_state = digitalRead(MQ_PIN);
  approach = digitalRead(D_PIN);
  delay(5);//等待传感器数据缓冲
  CHK+=chk;
  CHK+=":";
  TEM+=tem;
  TEM+=":";
  HUM+=hum;
  HUM+=":";
  MQS+=MQ_state;
  MQS+=":";
  APP+=approach;
  Data=CHK+TEM+HUM+MQS+APP;
  return Data;
  }
  void setup(){
  pinMode(DHT11_PIN,OUTPUT);
  pinMode(MQ_PIN,INPUT);
  pinMode(D_PIN,INPUT);
  servo1.attach(LIVINGROOM_SERVO_PIN);
  servo2.attach(KITCHEN_SERVO_PIN);
  lcd.setContrast(50);
  Serial.begin(9600);
    }
  void loop(){
  if(chrono.hasPassed(2000)){
  chrono.restart();
  lcd.clear();
  lcd.print("Temperature:");
  lcd.println(tem);
  lcd.print("Humidity:");
  lcd.print(hum);
  lcd.println("%");
  }
  if(Serial.available()>=12){
        Serial.readBytes(temp,12);
        command=String(temp);
        if (command.substring(0,1).equals(SEND_DATA)){
          Serial.println(get_data());
          }
        COMMAND(command.substring(0,1),command.substring(2,3),command.substring(4,6),command.substring(7,9),command.substring(10,12)); 
        }
    }
