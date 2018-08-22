[![RaiMan's Stuff](https://raw.github.com/RaiMan/SikuliX-2014-Docs/master/src/main/resources/docs/source/RaiManStuff64.png)](http://www.sikuli.org) SikuliX 1.1.4
============

[![Build Status](https://travis-ci.org/RaiMan/SikuliX1.svg?branch=master)](https://travis-ci.org/RaiMan/SikuliX-2014)

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

Sorry, Linux people: not yet useable for you :-(

**You need at least Java 8, but it works on Java 9+ also**

**Developement is done on Java 11 now**

<hr>

**API SNAPSHOT on OSSRH** (currently only for Mac and Windows)<br>

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

