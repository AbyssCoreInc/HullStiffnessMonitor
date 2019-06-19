import json
from Configuration import *
import time
from time import sleep
import datetime
from array import array
from datetime import datetime, date, timedelta
from pytz import timezone
from zeroconf import ServiceInfo, Zeroconf
import netifaces as ni
import socket
#from socket import socket
#class MessageBroker(service.Service, object):
class MessageBroker:
	server_ip = ""
	server_port = 0
	ws_service_name = 'Hull Stiffness Monitor'
	wsPort = None
	wsInfo = None

	def __init__(self,port):
		print("DataTransmitter.Init")
		ni.ifaddresses('wlan0')
		ip_addr = ni.ifaddresses('wlan0')[ni.AF_INET][0]['addr']
		desc = {'port': str(port), 'addr':ip_addr}
		print(str(ip_addr))
		info = ServiceInfo(
			"_http._tcp.local.",
			self.ws_service_name+"._http._tcp.local.",
			addresses=[socket.inet_aton(ip_addr)],
			port=port,
			properties=desc,
			server="hsm-probe.local.",
		)
		print(info)
		self.zeroconf = Zeroconf()
		self.zeroconf.register_service(info)

		#with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
		#	s.bind((ip_addr, port))
			#self.zeroconf = Zeroconf()
			#self.zeroconf.register_service(info)
		#	print("start listening")
		#	s.listen()
		#	self.zeroconf = Zeroconf()
		#	self.zeroconf.register_service(info)
		#	print("hearing something")
		#	conn, addr = s.accept()
		#	print("accepted connection")
		#	with conn:
		#		print('Connected by', addr)
		#		while True:
		#			data = conn.recv(1024)
		#			print(data)
		#			if not data or len(data) > 10:
		#				break
		#		(self.app_ip,self.server_port) = str(data).split(":")
		#		self.server_ip = addr[0]
		#		conn.sendall(b'got it')
	#	if self.server_port[len(self.server_port)-1] == "'":
	#		self.server_port = self.server_port[:-1]
	#	print("app_ip: " + str(self.server_ip) + " app_port: "+str(self.server_port))
		print("DataTransmitter.Init ready")

	def getAppPort(self):
		return self.server_port

	def connect(self, ip, port):
		self.server_ip = ip
		self.server_port = port
		print("DataTransmitter.connect connecting to server ("+self.server_ip+")("+str(self.server_port)+")")
		#with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
		#	s.connect((self.server_ip, self.server_port))
		#	self.s = s
		connected = False
		while (not connected):
			try:
				self.s = socket.create_connection((self.server_ip,str(self.server_port)))
				#self.s = socket.connect(self.server_ip,str(self.server_port))
				self.s.settimeout(2.0)
				connected = True
			except:
				print("connection failed trying again")
				sleep(2.0)
			
		print("DataTransmitter.connect ready")
	def reset(self):
		print("DataTrasnmitter reseting connection")
		self.s.close()
		self.connect(self.server_ip,self.server_port)
		print("DataTrasnmitter reset ready")

	def getTimeStamp(self):
		ts = time.time()
		utcts = datetime.utcfromtimestamp(ts)
		zonets = timezone('UTC').localize(utcts)
		timestamp = zonets.strftime('%Y-%m-%dT%H:%M:%S')
		return timestamp

	def transmitdata(self,data):
		print("DataTransmitter.transmitdata msg: "+data)
		#b = bytearray()
		#b.extend(map(ord, data))
		b = array('b')
		data = data + "\n"
		b.frombytes(data.encode())
		try:
			#self.s.sendall(b)
			#self.s = socket.connect(self.server_ip,str(self.server_port))
			self.s.send(b)
			#self.s.flush()
			#self.s.close()
			#self.s.sendall(b'asdasdasd')
			print("DataTransmitter.transmitdata data away")
			receive = self.s.recv(1024)
			print("Received "+str(receive))
			if (str(receive) is "ok"):
				return 0
			else:
				return -1
		except:
			print("some error in transmitdata")
		print("DataTransmitter.transmitdata received:"+data)



