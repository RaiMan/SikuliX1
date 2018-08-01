# this worked on Ubuntu 18.04 on August 1st, 2018
# thanks to https://lucacerone.net/2017/install-tesseract-3-0-5-in-ubuntu-16-04/

# remove tesseract binaries and languages
sudo apt-get remove tesseract-ocr*

# remove leptonica
sudo apt-get remove libleptonica-dev

# make sure other dependencies are removed too
sudo apt-get autoclean
sudo apt-get autoremove --purge

# general preparations
sudo apt-get install autoconf automake libtool
sudo apt-get install autoconf-archive
sudo apt-get install pkg-config
#sudo apt-get install libpng12-dev # did not work
sudo apt-get install libpng-dev # installs 1.6
sudo apt-get install libjpeg8-dev
sudo apt-get install libtiff5-dev
sudo apt-get install zlib1g-dev
sudo apt-get install libicu-dev
sudo apt-get install libpango1.0-dev
sudo apt-get install libcairo2-dev

######################### install leptonica 1.74.4
# http://www.leptonica.com/source/leptonica-1.74.4.tar.gz

# extract to folder and go into folder
######################################
./configure

# compile/link: this takes some time
sudo make

sudo make install

####################### install tesseract 3.05.02
# https://github.com/tesseract-ocr/tesseract/archive/3.05.02.tar.gz

# extract to folder and go into folder
######################################

./autogen.sh
./configure --enable-debug

# compile/link: this takes some time
LDFLAGS="-L/usr/local/lib" CFLAGS="-I/usr/local/include" make

sudo make install
sudo make install-langs # is a no-op with no additional languages
sudo ldconfig

################## test
tesseract -v

# should show
#tesseract 3.05.02
# leptonica-1.74.4
#  libjpeg 8d (libjpeg-turbo 1.5.2) : libpng 1.6.34 : libtiff 4.0.9 : zlib 1.2.11

