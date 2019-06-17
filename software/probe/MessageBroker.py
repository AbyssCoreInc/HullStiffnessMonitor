import json
from Configuration import *
import time
import datetime
from datetime import datetime, date, timedelta
from pytz import timezone
from zeroconf import ServiceInfo, Zeroconf
import netifaces as ni
import socket

#class MessageBroker(service.Service, object):
class MessageBroker:
	server_ip = ""
	server_port = 0
	ws_service_name = 'Hull Stiffness Monitor'
	wsPort = None
	wsInfo = None

	#def __init__(self, factory, portCallback):
	#def __init__(self, ip, port):
	def __init__(self,port):
		print("DataTransmitter.Init")
	#	self.server_port = port
	#	self.server_ip = ip
	#	factory.protocol = BroadcastServerProtocol
	#        self.factory = factory
	#        self.portCallback = portCallback
		

		ni.ifaddresses('wlan0')
		ip_addr = ni.ifaddresses('wlan0')[ni.AF_INET][0]['addr']
		#fqdn = socket.gethostname()
		#ip_addr = socket.gethostbyname(fqdn)
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

		with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
			s.bind((ip_addr, port))
			s.listen()
			conn, addr = s.accept()
			with conn:
				print('Connected by', addr)
				while True:
					data = conn.recv(1024)
					print(data)
					if not data or len(data) > 10:
						break
				(self.app_ip,self.server_port) = str(data).split(":")
				self.server_ip = addr[0]
				conn.sendall(b'got it')
		if self.server_port[len(self.server_port)-1] == "'":
			self.server_port = self.server_port[:-1]
		print("app_ip: " + str(self.server_ip) + " app_port: "+str(self.server_port))
		print("DataTransmitter.Init ready")

	
	#def privilegedStartService(self):
	#	self.wsPort = reactor.listenTCP(0, self.factory)
	#	port = self.wsPort.getHost().port

	#	fqdn = socket.gethostname()
	#	ip_addr = socket.gethostbyname(fqdn)
	#	hostname = fqdn.split('.')[0]

	#	wsDesc = {'service': 'Verasonics Frame', 'version': '1.0.0'}
	#	self.wsInfo = ServiceInfo('_hsm-probe._tcp.local.',
	#			hostname + ' ' + self.ws_service_name + '._hsm-probe._tcp.local.',
	#			socket.inet_aton(ip_addr), port, 0, 0, wsDesc, hostname + '.local.')
	#	self.zeroconf.register_service(self.wsInfo)
	#	self.portCallback(port)

	#	return super(WebSocketManager, self).privilegedStartService()

	#def stopService(self):
	#	self.zeroconf.unregister_service(self.wsInfo)
	#	self.wsPort.stopListening()
	#	return super(WebSocketManager , self).stopService()
	def getAppPort(self):
		return self.server_port

	def connect(self):
		print("DataTransmitter.connect connecting to server ("+self.server_ip+")("+self.server_port+")")
		#with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
		#	s.connect((self.server_ip, self.server_port))
		#	self.s = s
		self.s = socket.create_connection((self.server_ip,str(self.server_port)))
		self.s.settimeout(2.0)
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
		try:
			self.s.sendall(b)
			#self.s.sendall(b'asdasdasd')
			print("DataTransmitter.transmitdata data away")
			receive = self.s.recv(1024)
		except:
			print("some error in transmitdata")
		print("DataTransmitter.transmitdata received:"+data)



