[![RaiMan's Stuff](https://raw.github.com/RaiMan/SikuliX-2014-Docs/master/src/main/resources/docs/source/RaiManStuff64.png)](http://sikulix.com) SikuliX
============

**What is SikuliX**<br>SikuliX automates anything you see on the screen of your desktop computer 
running Windows, Mac or some Linux/Unix. It uses image recognition powered by OpenCV to identify 
GUI components and can act on them with mouse and keyboard actions.
This is handy in cases when there is no easy access to a GUI's internals or 
the source code of the application or web page you want to act on. [More details](http://sikulix.com)

<hr>

**You need at least Java 8, but it works on Java 9 up to latest (currently 14)**

**Windows:** Works out of the box ([for exceptions look here](https://github.com/RaiMan/SikuliX1/wiki/Windows:-Problems-with-libraries-OpenCV-or-Tesseract))

**Mac:** you have to make Tesseract OCR available ([for HowTo look here](https://github.com/RaiMan/SikuliX1/wiki/macOS-Linux:-Support-libraries-for-Tess4J-Tesseract-4-OCR)). **Java 14: open problems with Tesseract**

**Linux:** you have to make OpenCV and Tesseract OCR available ([for HowTo look here](https://sikulix-2014.readthedocs.io/en/latest/newslinux.html#version-1-1-4-special-for-linux-people)).

<hr>

**Latest stable version is 2.0.4** (branch `release_2.0.x` - [see what is fixed](https://github.com/RaiMan/SikuliX1/wiki/ZZZ-Bug-Fixes))

**Development version 2.1.0 currently not useable until further notice**<br>
New features will only be available in new major versions (currently 2.1.0, branches master and/or dev_...). 
<br>Until release of a stable 2.1.0, there will be nightly builds and snapshots available (see below).

[Here you can read about the changes/enhancements](https://sikulix-2014.readthedocs.io/en/latest/news.html)

**Get SikuliX ready to use**
- [SikuliX IDE for editing and running scripts](https://launchpad.net/sikuli/sikulix/2.0.4/+download/sikulixide-2.0.4.jar)
  - [Jython support for the IDE](https://repo1.maven.org/maven2/org/python/jython-standalone/2.7.1/jython-standalone-2.7.1.jar)
  - [JRuby support for the IDE](https://repo1.maven.org/maven2/org/jruby/jruby-complete/9.2.0.0/jruby-complete-9.2.0.0.jar)
  - download all needed to one folder and run sikulix-2.0.x.jar
  <br><br>
- [SikuliX Java API for programming in Java or Java aware languages](https://launchpad.net/sikuli/sikulix/2.0.4/+download/sikulixapi-2.0.4.jar)
  - for use in non-Maven projects
 
For use in **Java Maven projects** the dependency coordinates are:
```
<dependency>
  <groupId>com.sikulix</groupId>
  <artifactId>sikulixapi</artifactId>
  <version>2.0.4</version>
</dependency>
```
<hr>

**Current development version is 2.1.0** (branch `master` nightly builds / snapshots):<br>
[![Build Status](https://travis-ci.org/RaiMan/SikuliX1.svg?branch=master)](https://travis-ci.org/RaiMan/SikuliX1)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FRaiMan%2FSikuliX1.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2FRaiMan%2FSikuliX1?ref=badge_shield)

[Read about fixes, enhancements and new features](https://github.com/RaiMan/SikuliX1/wiki/About-fixes-and-enhancements-in-2.1.0)

**Get the nightly builds ready to use** 
- [SikuliX IDE for editing and running scripts]()
  - [Jython support for the IDE]()
  - [JRuby support for the IDE]()
  - download all needed to one folder and run sikulix-2.1.0.jar
  <br><br>
- [SikuliX Java API for programming in Java or Java aware languages]()
  - for use in non-Maven projects

For use in **Java Maven projects** use the SNAPSHOT dependency information:<br><br>
The repository URL:
```
<repositories>
  <repository>
    <id>sonatype-ossrh</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
  </repository>
</repositories>
```
The dependency coordinates are:
```
<dependency>
  <groupId>com.sikulix</groupId>
  <artifactId>sikulixapi</artifactId>
  <version>2.1.0-SNAPSHOT</version>
</dependency>
```
<hr>

**Development environment**

 - Java 11 (current JDK LTS release)
 - Java 8 (Oracle) for comatibility test
 - Source and target level for Java is version 8 as long as supported by Oracle
 - Maven project
 - Windows 10 latest (Pro 64-Bit)
 - Mac 10.15 latest
 - Ubuntu 18.04 in WSL on Windows 10 (basic tests only, headless)
 - Ubuntu 18.04 running in Oracle VM VirtualBox 6.1 on Windows 10
 - Using IntelliJ IDEA CE in all environments

<hr>

#### Contributions are welcome and appreciated
 - for `bugreports and requests for features or enhancements` use the issue tracker here
 - for `bugfixes` related to the latest release version you should create a pull request against the release branch (currently `release_2.0.x`), so your fix will be in the next bug-fix release (see milestones).
- for `smaller bugfixes and/or feature enhancements` related to the running development (currently branch master as version 2.1.0-SNAPSHOT and dev_... branches) you should create a pull request against the target branch
- a pull request should target only one branch. It is the resposibility and job of the maintainer to apply the changes to other branches in case 
- for `more complex revisions and/or enhancements` you should ask for a development branch together with a short description of your ideas
 
 **Please respect the following rules and guidelines when contributing**
  - Start with smaller fixes. E.g. choose an issue from the issue tracker and try to fix it. Or fix issues you encounter while using SikuliX.
  - Only fix cosmetic stuff if it's related to an issue you want to fix.
  - Before you change stuff like dependencies / overall code style and so on, talk with the maintainer beforehand.<br>Sometimes there is a a reason that things are as they are (... and sometimes not :-)).
  - Try to accept the individual coding styles of the acting contributors, even if some of the stuff might be sub-optimal in your eyes.<br>But feel free to talk about your ideas and the reasons behind.

 
