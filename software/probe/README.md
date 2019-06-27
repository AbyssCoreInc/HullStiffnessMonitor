# Python code

## Dependencies

sudo pip3 install pytz
sudo pip3 install board
sudo pip3 install adafruit-circuitpython-ads1x15
sudo pip3 install adafruit-circuitpython-adxl34x
sudo pip3 install imutils
sudo pip3 install opencv-python
sudo apt-get install libtiff5
sudo apt-get install libilmbase-dev
sudo apt-get install libopenexr-dev
sudo apt-get install libgstreamer1.0-dev
sudo apt-get install libqtgui4
sudo apt-get install libqt4-test
sudo pip3 install zeroconf
sudo pip3 install netifaces

## Start the service on boot
There is an issue starting the service as daemon during boot time as V4L device has not been initialized
Adding this to the /lib/udev/rules.d/99-systemd.rules should help:
KERNEL=="video0", SYMLINK="video0", TAG+="systemd"


