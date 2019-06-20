from collections import deque
from imutils.video import VideoStream
import numpy as np
import argparse
import cv2
import imutils
import numpy
import time
import subprocess

class VisualDetector:
	def __init__(self):
		# define the lower and upper boundaries of the "red dot"
		# ball in the HSV color space (though RGB values..), then initialize the
		# list of tracked points
		self.redLower = (3,0,190)
		self.redUpper = (100, 10, 255)
		self.pts = deque(maxlen=64)
		self.cam_width = 1280
		self.cam_height = 1024
		# if a video path was not supplied, grab the reference
		# to the webcam
		#self.vs = VideoStream(src=0, usePiCamera=True, resolution=(1280,720)).start()
		self.vs = cv2.VideoCapture(0)
		self.vs.set(3,self.cam_width)
		self.vs.set(4,self.cam_height)
		#self.vs.set(15, -8.0)
		command = "v4l2-ctl -d 0 -c auto_exposure=1 -c exposure_time_absolute=200 -c brightness=25 -c red_balance=6000 -c contrast=90"
		output = subprocess.call(command, shell=True)
		self.channels = {
			'hue': None,
			'saturation': None,
			'value': None,
			'laser': None,
		}
		self.hue_min = 20
		self.hue_max = 160
		self.sat_min = 100
		self.sat_max = 255
		self.val_min = 200
		self.val_max = 256
		self.d_x = 0
		self.d_y = 0
		self.trail = numpy.zeros((self.cam_height, self.cam_width, 3), numpy.uint8)
		self.previous_position = None
		self.center_x = self.cam_width/2.0
		self.center_y = self.cam_height/2.0
		# allow the camera or video file to warm up
		time.sleep(2.0)

	def track(self, frame, mask):
		"""
		Track the position of the laser pointer.
		Code taken from
		http://www.pyimagesearch.com/2015/09/14/ball-tracking-with-opencv/
		"""
		center = None

		countours = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)[-2]
		#countours = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
		#print("countours: "+str(len(countours)))
		# only proceed if at least one contour was found
		if len(countours) > 0:
			# find the largest contour in the mask, then use
			# it to compute the minimum enclosing circle and
			# centroid
			c = max(countours, key=cv2.contourArea)
			((x, y), radius) = cv2.minEnclosingCircle(c)
			moments = cv2.moments(c)
			if moments["m00"] > 0:
				center = int(moments["m10"] / moments["m00"]), int(moments["m01"] / moments["m00"])
			else:
				center = int(x), int(y)
			#print("dot center "+str(x)+","+str(y))
			self.d_x = x
			self.d_y = y
			# only proceed if the radius meets a minimum size
			if radius > 1:
				# draw the circle and centroid on the frame,
				cv2.circle(frame, (int(x), int(y)), int(radius),(0, 255, 255), 2)
				cv2.circle(frame, center, 5, (0, 0, 255), -1)
				# then update the ponter trail
				if self.previous_position:
					cv2.line(self.trail, self.previous_position, center, (255, 255, 255), 2)
		cv2.add(self.trail, frame, frame)
		self.previous_position = center

	def threshold_image(self, channel):
		if channel == "hue":
			minimum = self.hue_min
			maximum = self.hue_max
		elif channel == "saturation":
			minimum = self.sat_min
			maximum = self.sat_max
		elif channel == "value":
			minimum = self.val_min
			maximum = self.val_max

		(t, tmp) = cv2.threshold(
			self.channels[channel],  # src
			maximum,  # threshold value
			0,  # we dont care because of the selected type
			cv2.THRESH_TOZERO_INV  # t type
		)

		(t, self.channels[channel]) = cv2.threshold(
			tmp,  # src
			minimum,  # threshold value
			255,  # maxvalue
			cv2.THRESH_BINARY  # type
		)

		if channel == 'hue':
			# only works for filtering red color because the range for the hue
			# is split
			self.channels['hue'] = cv2.bitwise_not(self.channels['hue'])


	def worker(self):
		count = 0
		#while True:
			# grab the current frame
		ret, frame = self.vs.read()
#		name = "frame%d.jpg"%count
#		cv2.imwrite(name, frame)
		count = count + 1
			# handle the frame from VideoCapture or VideoStream
			#frame = frame[1] if args.get("video", False) else frame

			# if we are viewing a video and we did not grab a frame,
			# then we have reached the end of the video
		if frame is not None:
			#print("got frame")
				#frame = imutils.resize(frame, width=600)
				#print("blur")
			blurred = cv2.GaussianBlur(frame, (3, 3), 0)
			#print("HSV")
			hsv = cv2.cvtColor(blurred, cv2.COLOR_BGR2HSV)
			h, s, v = cv2.split(hsv)
			self.channels['hue'] = h
			self.channels['saturation'] = s
			self.channels['value'] = v

				# Threshold ranges of HSV components; storing the results in place
			self.threshold_image("hue")
			self.threshold_image("saturation")
			self.threshold_image("value")

				# Perform an AND on HSV components to identify the laser!
			self.channels['laser'] = cv2.bitwise_and(
				self.channels['hue'],
				self.channels['value']
			)
			self.channels['laser'] = cv2.bitwise_and(
				self.channels['saturation'],
				self.channels['laser']
			)

				# Merge the HSV components back together.
			hsv = cv2.merge([
				self.channels['hue'],
				self.channels['saturation'],
				self.channels['value'],
			])
			self.track(frame, self.channels['laser'])

	def getY(self):
		return self.d_x-(self.center_x)
	def getX(self):
		return self.d_y-(self.center_y)
	def setCenter(self):
		self.center_x = self.d_x
		self.center_y = self.d_y
