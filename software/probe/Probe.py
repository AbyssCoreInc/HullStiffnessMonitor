from __future__ import print_function
import os
import subprocess
import sys
import time
import atexit
import threading
import random
from Configuration import *
from MessageBroker import *


threads = []
stepcount = 0
numsteps = 0
counter = 0

# create empty threads (these will hold the stepper 1 and 2 threads)
st1 = threading.Thread()

#atexit.register(turnOffMotors)

def initiateThreads(datatrans,lensheater,configuration):
	t1 = threading.Thread(target=datatrans.worker)
	threads.append(t1)
	t1.start()
	
	print("started threads")


def main():
	global counter
	conf = Configuration()
	conf.readConfiguration()


	mBroker = MessageBroker(conf)
	mBroker.connect()
	cam.setMessageBroker(mBroker)
	cam.setImageNumber(images)
	initiateThreads(mBroker,lensHeater,conf)
	ts = time.time()
	print("main: going in the foreverloop (images="+str(images)+")")
	while (1):
		sendData()

	print("main: exiting the foreverloop")
	return 0

if __name__ == "__main__":
	sys.exit(main())
