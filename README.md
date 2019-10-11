[![RaiMan's Stuff](https://raw.github.com/RaiMan/SikuliX-2014-Docs/master/src/main/resources/docs/source/RaiManStuff64.png)](http://sikulix.com) SikuliX 1.1.4
============

**What is SikuliX** SikuliX automates anything you see on the screen of your desktop computer 
running Windows, Mac or some Linux/Unix. It uses image recognition powered by OpenCV to identify 
GUI components and can act on them with mouse and keyboard actions.
This is handy in cases when there is no easy access to a GUI's internals or 
the source code of the application or web page you want to act on. [More details](http://sikulix.com)

[![Build Status](https://travis-ci.org/RaiMan/SikuliX1.svg?branch=master)](https://travis-ci.org/RaiMan/SikuliX1)

**Major changes and enhancements**
 - latest OpenCV 3.x and everything at the Java level
 - support for transparency
 - revision of the text/OCR feature now based on Tess4J (wrapper around latest Tesseract 4.x)
 - packaging reduced to downloadable, ready-to-use API and IDE jars 
 - revision of the IDE and its feature implementations
 - complete re-implementation of the script running support
 
 **Be aware**
 This version is a developement version and currently only available as nightly build or Maven snapshot.
 
[Here you can get the stuff](https://raiman.github.io/SikuliX1/downloads.html) 

[Here you can read more about the changes/enhancements](https://sikulix-2014.readthedocs.io/en/latest/news.html)

**You need at least Java 8, but it works on Java 9 up to 12 also**

**Windows:** Works out of the box.

**Mac:** you have to make the prerequisites for Tesseract ready ([for HowTo look here](https://github.com/RaiMan/SikuliX1/wiki/macOS-Linux:-Support-libraries-for-Tess4J-Tesseract-4-OCR)).

**Linux:** you have to make the prerequisites OpenCV and Tesseract ready ([for HowTo look here](https://sikulix-2014.readthedocs.io/en/latest/newslinux.html#version-1-1-4-special-for-linux-people)).<br>There might be oddities, since testing is mainly done on Windows 10 and Mac 10.14. Feel free to report.

<hr>

**API SNAPSHOT on OSSRH**<br>

The repository URL:<br>
```
<https://oss.sonatype.org/content/repositories/snapshots>
```

The coordinates are:
```
<groupId>com.sikulix</groupId>
<artifactId>sikulixapi</artifactId>
<version>1.1.4-SNAPSHOT</version>
```

<hr>

**Developement environment**

 - Java 12 (OpenJDK release)
 - Windows 10 latest
 - Mac 10.14 latest
 - Ubuntu 18.04 in WSL on Windows 10

<hr>

#### Contributions are welcome and appreciated
 - for `bugreports and requests for features or enhancements` use the issue tracker here
 - for `smaller bugfixes and/or feature enhancements` you should create a pull request against the target branch (master in most cases)
 - for `more complex revisions and/or enhancements` you should ask for a developement branch together with a short description of your ideas
 
 **Please respect the following rules and guidelines when contributing**
  - Start with smaller fixes. E.g. choose an issue from the issue tracker and try to fix it. Or fix issues you encounter while using SikuliX.
  - Only fix cosmetic stuff if it's related to an issue you want to fix.
  - Before you change stuff like dependencies / overall code style and so on, talk with the maintainer beforehand.<br>Sometimes there is a a reason that things are as they are (... and sometimes not :-)).
  - Try to accept the individual coding styles of the acting contributors, even if some of the stuff might be sub-optimal in your eyes.<br>But feel free to talk about your ideas and the reasons behind.

 
