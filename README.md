******** Paused (not available) till October 2023 ********
---

---

[![SikuliX](https://raw.githubusercontent.com/RaiMan/SikuliX1/master/Support/sikulix-red.png)](https://sikulix.github.io)

---
**What is SikuliX**<br>SikuliX automates anything you see on the screen of your desktop computer 
running Windows, Mac or some Linux/Unix. It uses image recognition powered by OpenCV to identify 
GUI components and can act on them with mouse and keyboard actions.
This is handy in cases when there is no easy access to a GUI's internals or 
the source code of the application or web page you want to act on. [More details](http://sikulix.com)

Great thanks for the new logo and all the help with the new webpage to [@Waleed Sadek](https://github.com/waleedsadek-panx)

---
**2.0.6 (branch release_2.0.x) preparing for release - snapshots available**

**Latest Upload: April 17th, 2023**

**Direct IDE downloads** &nbsp;&nbsp;&nbsp;&nbsp;
[> for Windows <](https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=com.sikulix&a=sikulixidewin&v=2.0.6-SNAPSHOT&e=jar)&nbsp;&nbsp;&nbsp;&nbsp;[> for macOS Intel <](https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=com.sikulix&a=sikulixidemac&v=2.0.6-SNAPSHOT&e=jar)&nbsp;&nbsp;&nbsp;&nbsp;[> for macOS Silicon Mx <](https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=com.sikulix&a=sikulixidemacm&v=2.0.6-SNAPSHOT&e=jar)&nbsp;&nbsp;&nbsp;&nbsp;[> for Linux <](https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=com.sikulix&a=sikulixidelux&v=2.0.6-SNAPSHOT&e=jar) 

You get files like `sikulixidemac-2.0.6-20210708.194940-1.jar`, which you can place wherever you want and rename them to whatever you want. 

**JAVA: must be Java 11 or later** (best places to get it: [Eclipse Temurin](https://adoptium.net) or [Azul](https://www.azul.com/downloads/?package=jdk#download-openjdk))

**OCR (macOS/Linux):** now using Tess4J/Tesseract 5 - have a Tesseract 5.x ready (tesseract runs on commandline)

**OpenCV Support:** Windows/macOS have it bundled - for Linux you have to make it ready yourself 

[more information coming sooner or later ;-)]()

---
**2.1.0 (branch master) currently not useable - development suspended**
<hr>

**Latest stable version is 2.0.5** (still runs with Java 8)

[Important: Read about changes/issues/enhancements](https://github.com/RaiMan/SikuliX1/wiki/About-actual-release-version)

[List of fixes](https://github.com/RaiMan/SikuliX1/wiki/ZZZ-Bug-Fixes)

[Get SikuliX ready to use](https://raiman.github.io/SikuliX1/downloads.html)
 
For use in **Java Maven projects** the dependency coordinates are:
```
<dependency>
  <groupId>com.sikulix</groupId>
  <artifactId>sikulixapi</artifactId>
  <version>2.0.5</version>
</dependency>
```
<hr>

**My Development environment**

 - Java 17 (current JDK LTS release)
 - Source and target level for Java is version 11
 - Maven project
 - Windows 11 latest (Pro 64-Bit)
 - latest macOS 12 (Monterey) on Intel and M1 machines
 - Ubuntu latest LTS version running in Oracle VM VirtualBox on Windows 10
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

 
