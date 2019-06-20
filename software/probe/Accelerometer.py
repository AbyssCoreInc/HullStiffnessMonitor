import board
import busio
import MessageBroker
import adafruit_adxl34x


class Accelerometer:
	
	def __init__(self):
		#self.mbroker = broker
		i2c = busio.I2C(board.SCL, board.SDA)
		self.accelerometer = adafruit_adxl34x.ADXL345(i2c)
		self.level_x = 0
		self.level_y = 0
		self.level_z = 0


	def getAccelerationVector(self):
		#print("%f %f %f"%self.accelerometer.acceleration)
		(x,y,z) = self.accelerometer.acceleration
		return {'x': x-self.level_x, 'y': y-self.level_y ,'z': z-self.level_z}

	def setLevel(self):
		(x,y,z) = self.accelerometer.acceleration
		self.level_x = x
		self.level_y = y
		self.level_z = z


