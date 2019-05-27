import json
from pprint import pprint

class Configuration:
	json_conf = []
	def __init__(self):
		print("Init")
	
	def readConfiguration(self):
		print("Reading configuration file")
		with open('/etc/hullmonitor.json', 'r') as handle:
			self.json_conf = json.load(handle)
	def getServerPort(self):
		return self.json_conf["configuration"]["server_port"]
	def getServerIP(self):
		return self.json_conf["configuration"]["server_ip"]
	
