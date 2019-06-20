from __future__ import print_function
import os
import subprocess
import signal
import sys
import time
from time import sleep
import atexit
import threading
from queue import Queue
import random
import RPi.GPIO as GPIO
from Configuration import Configuration
from MessageBroker import MessageBroker
from AnalogConverter import AnalogConverter
from Accelerometer import Accelerometer
from VisualDetector import VisualDetector
from ControlInterface import ControlInterface

appIP = ""
appPORT = ""
queue = Queue()
calib = 0
rec = 0
#vd = VisualDetector()
#accelerometer = Accelerometer()

def btn1_callback(channel):
	print("falling edge detected on button 1")

def btn2_callback(channel):
	print("falling edge detected on button 2")

def pwr_callback(channel):  
	print("falling edge detected on power")
	os.system('shutdown -h now')

def setGPIO():
	GPIO.setmode(GPIO.BCM)
	GPIO.setup(4, GPIO.OUT)
	GPIO.setup(17, GPIO.OUT)
	GPIO.setup(18, GPIO.OUT)
	GPIO.setup(21, GPIO.IN)
	GPIO.setup(25, GPIO.IN)
	GPIO.setup(26, GPIO.IN)
	chan_list = [4,17,18]                             # also works with tuples
	GPIO.output(chan_list, GPIO.LOW)

	GPIO.add_event_detect(21, GPIO.FALLING, callback=btn1_callback, bouncetime=300)
	GPIO.add_event_detect(26, GPIO.FALLING, callback=btn2_callback, bouncetime=300)
	GPIO.add_event_detect(25, GPIO.FALLING, callback=pwr_callback, bouncetime=300)

	print("GPIO setup done")

def signal_handler(sig, frame):
	print('You pressed Ctrl+C!')
	GPIO.output(4,GPIO.LOW)
	GPIO.output(17,GPIO.LOW)
	GPIO.output(18,GPIO.LOW)
	sys.exit(0)

def calibrate(value,q):
	if (value is "1"):
		GPIO.output(4,GPIO.HIGH)
		q.put("c")
		q.put(1)
	else:
		GPIO.output(4,GPIO.LOW)
		q.put("c")
		q.put(0)

def record(value,q):
	global rec
	if (value is "1"):
		GPIO.output(4,GPIO.LOW)
		GPIO.output(18, GPIO.HIGH)
		q.put("r")
		q.put(1)
	else:
		GPIO.output(18, GPIO.LOW)
		q.put("r")
		q.put(0)

def setAppAddress(ip, port,q):
	global appPORT
	global appIP
	print("setAppAddress("+ip+","+port+")")
	appIP = ip
	appPORT = port
	q.put(appIP)
	q.put(appPORT)
	print("setAppAddress("+str(appIP)+","+str(appPORT)+")")
	printPort()

def printPort():
	print("ip ("+appIP+")")
	print("port ("+appPORT+")")

def main():
	global analog
	global accelerometer
	global vd
	global appPORT
	global appIP
	global calib
	global rec

	setGPIO()
	conf = Configuration()
	conf.readConfiguration()
	signal.signal(signal.SIGINT, signal_handler)
	analog = AnalogConverter()
	accelerometer = Accelerometer()
	vd = VisualDetector()

	count = 0
	message = ""
	ts = time.time()
	led1 = False
	ctrlInt = ControlInterface(6000)
	ctrlT = threading.Thread(target=ctrlInt.worker,args=(queue,))
	ctrlT.start()

	mBroker = MessageBroker(conf.getServerPort())
	print("Start waiting for the port")
	while(queue.empty()):
		print("data in queue: " + str(queue.qsize()))
		sleep(0.5)
	appIP = queue.get()
	appPORT = queue.get()
	printPort()

	GPIO.output(18, GPIO.HIGH)
	print("connecting")
	mBroker.connect(appIP,appPORT)
	print("main: going in the foreverloop")
	while (1):
		if (queue.empty()==False):
			cmd = queue.get()
			if(cmd == "c" and queue.empty()==False):
				value = queue.get()
				if (value == 1):
					calib = 1
				else:
					calib = 0
		if(calib == 1):
			vd.setCenter()
			accelerometer.setLevel()
		vd.worker()
		accel = accelerometer.getAccelerationVector()
		voltage = analog.getBatteryVoltage()
		message = str(accel['x'])+":"+str(accel['y'])+":"+str(accel['z'])+":"+str(vd.getX())+":"+str(vd.getY())+":"+str(voltage)+":"+str(calib)+":"+str(rec)+":"
		if (mBroker.transmitdata(message) != 0):
			mBroker.reset()
		message = ""
		if(led1):
			GPIO.output(17, GPIO.HIGH)
			led1 = False
		else:
			GPIO.output(17, GPIO.LOW)
			led1 = True

	print("main: exiting the foreverloop")
	return 0

if __name__ == "__main__":
	sys.exit(main())
