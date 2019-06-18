import time
import board
import busio
import adafruit_ads1x15.ads1115 as ADS
from adafruit_ads1x15.analog_in import AnalogIn

class AnalogConverter:
	divider = 1.5151

	def __init__(self):
		# Create the I2C bus
		i2c = busio.I2C(board.SCL, board.SDA)

		# Create the ADC object using the I2C bus
		ads = ADS.ADS1115(i2c)

		# Create single-ended input on channel 0
		self.chan = AnalogIn(ads, ADS.P0)

	def getBatteryVoltage(self):
		
		return self.chan.voltage*self.divider

