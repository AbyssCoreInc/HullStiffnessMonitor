import netifaces as ni
import socket
from time import sleep

class ControlInterface:
	def __init__(self,port):
		ni.ifaddresses('wlan0')
		ip_addr = ni.ifaddresses('wlan0')[ni.AF_INET][0]['addr']
		#with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as self.s:
		#	self.s.bind((ip_addr, port))
		#	print("start listening")
		#	self.s.listen()
		self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		self.sock.bind((ip_addr, port))
		print("start listening")
		self.sock.listen()
		print("ControlInterface.Init ready")

	def worker(self,q):
		#self.mailbox = q
		while True:
			conn, addr = self.sock.accept()
			print("accepted connection")
			with conn:
				print('Connected by', addr)
				while True:
					#print("ctrl")
					try:
						data = conn.recv(1024)
					except(socket.error, e):
						err = e.args[0]
						if(err == errno.EAGAIN or err == errno.EWOULDBLOCK):
							sleep(1)
							print("No data available")
							continue
						else:
							# a "real" error occurred
							print(e)
							print("accepting new connection")
							conn, addr = self.sock.accept()
					if (len(data) > 2):
						self.interpretMessage(data.decode("ascii")+":"+str(addr[0]),q)
						#conn.sendall(b'got it')
						break


	def interpretMessage(self, data, que):
		import Probe
		print("interpretMessage "+data)
		array = data.split(":")
		if(len(array) > 1):
			if (array[0] == "calibrate"):
				Probe.calibrate(array[1], que)
			elif (array[0] == "record"):
				Probe.record(array[1], que)
			else:
				Probe.setAppAddress(array[2],array[1], que)
