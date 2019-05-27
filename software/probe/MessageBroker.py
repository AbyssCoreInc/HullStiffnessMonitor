import json
from Configuration import *
import time
import datetime
from datetime import datetime, date, timedelta
from pytz import timezone
import socket

class MessageBroker:
	server_ip = ""
	server_port = ""
	
	def __init__(self, conf):
		# connec to server REST interface
		self.conf = conf
		self.server_port = conf.getServerPort()
		self.server_ip = conf.getServerIP()
		print("DataTransmitter.Init ready")
		#def __del__(self):
		#self.client.loop_stop()

	# Handle incoming MQTT messages. Parses control messages. Message format is "[commang]-[value]"
	# at this point it is verstile enough. If more sophistication like timestamps, etc are needed
	# better messaging has be implemented. To other direction message tries to be NGSI compliatn JSON so
	# similar approach could be used.
	def on_message(self,client, userdata, message):
		print("on_message")
			
	def connect(self):
		print("DataTransmitter.connect connecting to mqtt broker ", self.mqtturl)
		with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
			s.connect((self.server_ip, self.server_port))
		print('Received', repr(data))
		print("DataTransmitter.connect ready")
	
	def getTimeStamp(self):
		ts = time.time()
		utcts = datetime.utcfromtimestamp(ts)
		zonets = timezone('UTC').localize(utcts)
		timestamp = zonets.strftime('%Y-%m-%dT%H:%M:%S')
		return timestamp
	

	def transmitdata(self,data):
		print("DataTransmitter.transmitdata msg: "+data)
		s.sendall(data)
		receive = s.recv(1024)
		print("DataTransmitter.transmitdata received:"+data)


	def worker(self):
		self.client.subscribe("CameraDolly/ControlMessage")
		self.client.loop_start()
		try:
			while True:
				time.sleep(1)
				print("Wait messages")
		except KeyboardInterrupt:
			print("exiting")
		self.client.disconnect()
		self.client.loop_stop()
#print("DataTransmitter.trasnmitdata ready")

