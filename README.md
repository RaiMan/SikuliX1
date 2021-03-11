

---

**What is SikuliX**<br>SikuliX automates anything you see on the screen of your desktop computer 
running Windows, Mac or some Linux/Unix. It uses image recognition powered by OpenCV to identify 
GUI components and can act on them with mouse and keyboard actions.
This is handy in cases when there is no easy access to a GUI's internals or 
the source code of the application or web page you want to act on. [More details](http://sikulix.com)

<hr>

**Java: recommended is Java 11 - but you need at least Java 8** (best place to get it: [AdoptOpenJDK](https://adoptopenjdk.net))
<br>non-LTS versions like Java 15 might create problems that should be reported

**Windows:** Works out of the box

**macOS/Linux** you have to make Tesseract OCR available ([for HowTo look here](https://github.com/RaiMan/SikuliX1/wiki/macOS-Linux:-Support-libraries-for-Tess4J-Tesseract-4-OCR)).

**Linux** OpenCV ibraries are bundled and should work on Ubuntu-like flavors. In case of problems you have to find a solutiuon.

<hr>

**Latest stable version is 2.0.5**

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

**Development environment**

 - Java 11 (current JDK LTS release)
 - Java 8 (Oracle) for comatibility test
 - Source and target level for Java is version 8 as long as supported by Oracle
 - Maven project
 - Windows 10 latest (Pro 64-Bit)
 - macOS 11.2 on Intel machines
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

 
