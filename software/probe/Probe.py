from __future__ import print_function
import os
import subprocess
import signal
import sys
import time
import atexit
import threading
import random
import RPi.GPIO as GPIO
from Configuration import Configuration
from MessageBroker import MessageBroker
from AnalogConverter import AnalogConverter
from Accelerometer import Accelerometer
from VisualDetector import VisualDetector

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

def main():
	setGPIO()
	conf = Configuration()
	conf.readConfiguration()
	signal.signal(signal.SIGINT, signal_handler)
	mBroker = MessageBroker(conf.getServerIP(), conf.getServerPort())
	mBroker.connect()

	#analog = AnalogConverter()
	#accelerometer = Accelerometer(mBroker)
	vd = VisualDetector()

	count = 0
	message = ""
	ts = time.time()
	led1 = False
	GPIO.output(4,GPIO.HIGH)
	print("main: going in the foreverloop")
	while (1):
		#accel = accelerometer.getAccelerationVector()
		#message = str(accel['x'])+":"+str(accel['y'])+":"+str(accel['z'])+"0.0:0.0"
		#mBroker.transmitdata(message)
		message = ""
		vd.worker()
		if (count == 1000000):
			#message = "bat_v:"+str(analog.getBatteryVoltage())
			#mBroker.transmitdata(message)
			if(led1):
				GPIO.output(17, GPIO.HIGH)
				GPIO.output(18, GPIO.LOW)
				led1 = False
			else:
				GPIO.output(18, GPIO.HIGH)
				GPIO.output(17, GPIO.LOW)
				led1 = True
			message = ""
			count = 0
		count = count + 1

	print("main: exiting the foreverloop")
	return 0

if __name__ == "__main__":
	sys.exit(main())
