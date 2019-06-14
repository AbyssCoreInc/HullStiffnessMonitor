import json
from Configuration import *
import time
import datetime
from datetime import datetime, date, timedelta
from pytz import timezone
import socket

class MessageBroker:
	server_ip = ""
	server_port = 0

	def __init__(self, ip, port):
		print("DataTransmitter.Init")
		self.server_port = port
		self.server_ip = ip
		print("DataTransmitter.Init ready")

	def connect(self):
		print("DataTransmitter.connect connecting to server ")
		#with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
		#	s.connect((self.server_ip, self.server_port))
		#	self.s = s
		self.s = socket.create_connection((self.server_ip,str(self.server_port)))
		print("DataTransmitter.connect ready")

	def getTimeStamp(self):
		ts = time.time()
		utcts = datetime.utcfromtimestamp(ts)
		zonets = timezone('UTC').localize(utcts)
		timestamp = zonets.strftime('%Y-%m-%dT%H:%M:%S')
		return timestamp

	def transmitdata(self,data):
		print("DataTransmitter.transmitdata msg: "+data)
		b = bytearray()
		b.extend(map(ord, data))
		#self.s.sendall(b)
		self.s.sendall(b'asdasdasd')
		print("DataTransmitter.transmitdata data away")
		receive = self.s.recv(1024)
		print("DataTransmitter.transmitdata received:"+data)



