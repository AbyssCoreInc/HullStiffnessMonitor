from collections import deque
from imutils.video import VideoStream
import numpy as np
import argparse
import cv2
import imutils
import time

class VisualDetector:
	def __init__(self):
		# define the lower and upper boundaries of the "green"
		# ball in the HSV color space, then initialize the
		# list of tracked points
		self.greenLower = (29, 86, 6)
		self.greenUpper = (64, 255, 255)
		#pts = deque(maxlen=args["buffer"])

		# if a video path was not supplied, grab the reference
		# to the webcam
		self.vs = VideoStream(src=0).start()

		# otherwise, grab a reference to the video file

		# allow the camera or video file to warm up
		time.sleep(2.0)

	def worker(self):
		while True:
			# grab the current frame
			frame = vs.read()

			# handle the frame from VideoCapture or VideoStream
			frame = frame[1] if args.get("video", False) else frame

			# if we are viewing a video and we did not grab a frame,
			# then we have reached the end of the video
			#if frame is None:
			#	break

			# resize the frame, blur it, and convert it to the HSV
			# color space
			frame = imutils.resize(frame, width=600)
			blurred = cv2.GaussianBlur(frame, (11, 11), 0)
			hsv = cv2.cvtColor(blurred, cv2.COLOR_BGR2HSV)

			# construct a mask for the color "green", then perform
			# a series of dilations and erosions to remove any small
			# blobs left in the mask
			mask = cv2.inRange(hsv, greenLower, greenUpper)
			mask = cv2.erode(mask, None, iterations=2)
			mask = cv2.dilate(mask, None, iterations=2)
