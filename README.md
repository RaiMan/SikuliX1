[![RaiMan's Stuff](https://raw.github.com/RaiMan/SikuliX-2014-Docs/master/src/main/resources/docs/source/RaiManStuff64.png)](http://sikulix.com) SikuliX 1.1.4
============

**What is SikuliX** SikuliX automates anything you see on the screen of your desktop computer 
running Windows, Mac or some Linux/Unix. It uses image recognition powered by OpenCV to identify 
GUI components and can act on them with mouse and keyboard actions.
This is handy in cases when there is no easy access to a GUI's internals or 
the source code of the application or web page you want to act on. [More details](http://sikulix.com)

[![Build Status](https://travis-ci.org/RaiMan/SikuliX1.svg?branch=master)](https://travis-ci.org/RaiMan/SikuliX1)

Since version 2 is still in a very experimental stage and needs a complete revision, I thought, that it might be the
time, to implement some of the version-2-ideas into the current version 1.

**Major changes and enhancements**
 - latest OpenCV 3.x and everything at the Java level
 - support for transparency
 - revision of the text/OCR feature now based on Tess4J (wrapper around latest Tesseract 3.x)
 - packaging reduced to downloadable, ready-to-use API and IDE jars (bye, bye Setup ;-) 
 - revision of the IDE and its feature implementations
 
 **Be aware**
 This version is a developement version and currently only available as nightly build.
 
[Here you can get the stuff](https://raiman.github.io/SikuliX1/downloads.html) 

[Here you can read more about the changes/enhancements](https://sikulix-2014.readthedocs.io/en/latest/news.html)

**You need at least Java 8, but it works on Java 9 up to 12 also**

**Developement is done on**

 - Java 12 (OpenJDK release)
 - Windows 10 latest
 - Mac 10.14 latest
 - Ubuntu 18.04 in VirtualBox on Windows 10

<hr>

**API SNAPSHOT on OSSRH**<br>

The repository URL:<br>
```
<url>http://oss.sonatype.org/content/groups/public</url>
```

The coordinates are:
```
<groupId>com.sikulix</groupId>
<artifactId>sikulixapi</artifactId>
<version>1.1.4-SNAPSHOT</version>
```

**Works out of the box for Mac and Windows**

**For Linux** 

 - you have to make the prerequisites OpenCV and Tesseract ready ([for HowTo look here](https://sikulix-2014.readthedocs.io/en/latest/newslinux.html#version-1-1-4-special-for-linux-people))

 - there might be other oddities, since testing is mainly done on Windows 10 and Mac 10.14