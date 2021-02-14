import numpy as np
import time
import cv2
import os
import re
import serial
import psutil
import socket
from queue import PriorityQueue, Queue
import threading
##"R-L-W0-00-90"
##"R-L-A0-OP-00"
##"R-K-W0-00-90"
##"R-K-A0-OP-00"
command=PriorityQueue(maxsize=10)
information=Queue(maxsize=10)
home_info=Queue(maxsize=10)
rpi=socket.socket()
yolopath = os.getcwd()
labelsPath = os.path.join(yolopath, 'rpi.names')
weightsPath = os.path.join(yolopath, 'rpi_69000.weights')
configPath = os.path.join(yolopath, 'rpi.cfg')
Camera = 0  #摄像头设备
CONFIDENCE = 0.7  # 过滤弱检测的最小概率
THRESHOLD = 0.4  # 非最大值抑制阈值
LABELS = open(labelsPath).read().strip().split("\n")
net = cv2.dnn.readNetFromDarknet(configPath, weightsPath)
net.setPreferableBackend(cv2.dnn.DNN_BACKEND_OPENCV)
net.setPreferableTarget(cv2.dnn.DNN_TARGET_CPU)
ln = net.getLayerNames()
ln = [ln[i[0] - 1] for i in net.getUnconnectedOutLayers()]
vs = cv2.VideoCapture(Camera)
vs.set(cv2.CAP_PROP_BUFFERSIZE,1) #缓存区大小
vs.set(cv2.CAP_PROP_FOURCC,cv2.VideoWriter_fourcc(*"MJPG"))#图片格式 
def YOLO_TEST():
    while True:
        (W, H) = (None, None)
        (grabbed, frame) = vs.read()
        if not grabbed:
            break
        if W is None or H is None:
            (H, W) = frame.shape[:2]
        blob = cv2.dnn.blobFromImage(frame, 1 / 255.0, (320, 320),swapRB=True, crop=False)
        net.setInput(blob)
        layerOutputs = net.forward(ln)
        boxes = []
        confidences = []
        classIDs = []
        for output in layerOutputs:
            for detection in output:
                scores = detection[5:]
                classID = np.argmax(scores)
                confidence = scores[classID]
                if confidence > CONFIDENCE:
                    box = detection[0:4] * np.array([W, H, W, H])
                    (centerX, centerY, width, height) = box.astype("int")
                    x = int(centerX - (width / 2))
                    y = int(centerY - (height / 2))
                    boxes.append([x, y, int(width), int(height)])
                    confidences.append(float(confidence))
                    classIDs.append(classID)
        idxs = cv2.dnn.NMSBoxes(boxes, confidences, CONFIDENCE,THRESHOLD)
        judgement=0
        if len(idxs) > 0 :
            for i in idxs.flatten():
                (x, y) = (boxes[i][0], boxes[i][1])
                text = "{}: {:.4f}".format(LABELS[classIDs[i]],confidences[i])
                if float(text.strip("fire: "))>0.8:
                    judgement = float(text.strip("fire: "))
                    break
        if judgement>0.8:
            command.put((1,"R-L-W0-00-90"),block=True)
            command.put((1,"R-L-A0-OP-00"),block=True)
            command.put((1,"R-K-W0-00-90"),block=True)
            command.put((1,"R-K-A0-OP-00"),block=True)
            time.sleep(3)
        else :
            time.sleep(1)
def SERIAL_COMMUNICATE():
    ser = serial.Serial('/dev/ttyUSB0', 9600, timeout=1)
    while True:
        out=command.get(block=True)[1]
        if out.split("-")[0]=="S":
            ser.write(out.encode())
            time.sleep(0.5)
            information.put(bytes.decode(ser.readline()).strip("\r\n"),block=False)
        else:
            ser.write(out.encode())
            time.sleep(0.5)
def ANALYZE():
    while True:
        time.sleep(1)
        command.put((3,"S-0-00-00-00"),block=True)
        info=information.get(block=True)
        CHK,TEM,HUM,MQS,APP=info.split(":")
        temperature=int(TEM)
        humidity=int(HUM)
        if home_info.full():
            pass
        else :
            home_info.put((temperature,humidity))
        if not CHK=="-2":
            if temperature>29 or humidity<90:
                command.put((3,"R-L-F0-OP-00"),block=True)
                command.put((3,"R-L-W0-00-60"),block=True)
                command.put((3,"R-K-W0-00-60"),block=True)
                command.put((3,"R-K-F0-OP-00"),block=True)
                time.sleep(0.05)
        if MQS=="0":
            command.put((1,"R-L-W0-00-90"),block=True)
            command.put((1,"R-L-A0-OP-00"),block=True)
            time.sleep(0.05)
        if APP=="0":
            command.put((3,"R-L-E0-OP-00"),block=True)
            time.sleep(0.05)
def ANDROID_SOCKET():
    try:
        rpi.bind(("192.168.3.34",8080))
    except:
        rpi.bind(("192.168.3.34",8081))
    rpi.listen(1)
    client,address=rpi.accept()
    while True:
        time.sleep(1)
        cpuinfo = os.popen("free -m | grep Mem")
        strMem = cpuinfo.readline()
        numMem = re.findall("\d+", strMem)
        gpu_percent=str(int(int(numMem[1])/int(numMem[0])*100)).zfill(3)
        cpu_usage=str(int(psutil.cpu_percent(0))).zfill(3)
        tem_hum=home_info.get(block=True)
        sent_info=str(tem_hum[0]).zfill(3)+"-"+str(tem_hum[1]).zfill(3)+"-"+cpu_usage+"-"+gpu_percent
        try:
            client.send(sent_info.encode())
            time.sleep(2)##此处对应安卓客户端接收数据的周期
        except:
            client,address=rpi.accept()
            continue
        try:
            buffer=client.recv(2,0x40)
        except BlockingIOError as error:
            buffer=None
        if buffer:
            signal=buffer.decode()
            if signal=="W0":
                command.put((2,"R-L-W0-00-10"),block=True)
            elif signal=="W1":
                command.put((2,"R-L-W0-00-90"),block=True)
            elif signal=="F0":
                command.put((2,"R-K-F0-OP-00"),block=True)
            elif signal=="F1":
                command.put((2,"R-K-F0-SD-00"),block=True)
            else:
                pass
        else:
            pass
yolo=threading.Thread(target=YOLO_TEST)##debug done
arduino=threading.Thread(target=SERIAL_COMMUNICATE)
control_center=threading.Thread(target=ANALYZE)
android=threading.Thread(target=ANDROID_SOCKET)
yolo.start()
arduino.start()
control_center.start()
android.start()
yolo.join()
arduino.join()
control_center.join()
android.join()
