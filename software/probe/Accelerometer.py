import board
import busio
import MessageBroker
import adafruit_adxl34x


class Accelerometer:
	
	def __init__(self):
		#self.mbroker = broker
		i2c = busio.I2C(board.SCL, board.SDA)
		self.accelerometer = adafruit_adxl34x.ADXL345(i2c)


	def getAccelerationVector(self):
		print("%f %f %f"%self.accelerometer.acceleration)
		(x,y,z) = self.accelerometer.acceleration
		return {'x': x, 'y': y ,'z': z}
