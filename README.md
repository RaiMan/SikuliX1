[![RaiMan's Stuff](https://raw.github.com/RaiMan/SikuliX-2014-Docs/master/src/main/resources/docs/source/RaiManStuff64.png)](http://www.sikuli.org) SikuliX 1.1.3
============

[![Build Status](https://travis-ci.org/RaiMan/SikuliX1.svg?branch=master)](https://travis-ci.org/RaiMan/SikuliX-2014) [![Join the chat at https://gitter.im/RaiMan/SikuliX-2014](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/RaiMan/SikuliX-2014?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) 

-----------------
1.1.3 is a bug-fix release for problems with versions up to 1.1.2 ([get nightly builds](https://raiman.github.io/SikuliX1/nightly.html))

[looking for 1.1.2 final (available 2018-03-10)](https://launchpad.net/sikuli/sikulix/1.1.2)

<hr>

**The followup is SikuliX2 [SikuliX 2.0.0](https://github.com/RaiMan/SikuliX2)** 
<hr>

**SNAPSHOTS on OSSRH**<br>

The repository URL:<br>
`<url>http://oss.sonatype.org/content/groups/public</url>`<br>

The coordinates:
```
<groupId>com.sikulix</groupId>
<artifactId>sikulixapi</artifactId>
<version>1.1.3-SNAPSHOT</version>
```

**You might also visit the WIKI**

**The Java language and bytecode versions are at 1.8**, so the nightly artifacts at least need Java 8.

**BE AWARE: When using with Java 9+ - Please report any problems**

In doubt it is a good idea to run things from command line using

    java -Dsikuli.Debug=3 -jar ((sikulix.jar or whatever app using sikulixapi.jar))
    
... and have a deeper look at the debug output in case.

<hr>

**If you want to test the head of developement without the need to build from sources:** <br>
[look here at the page with the nightly builds]()<br>
**At your own risk ;-) Take care for your existing work - be prepared to restore your stuff!!**

<hr />

**SikuliX is completely free Open Source in all aspects** - [see details](http://sikulix.com/disclaimer)

... based on Sikuli Script that was discontinued mid 2012 - [see the sources](https://github.com/sikuli/sikuli)
<hr/>

This Maven multi-module setup contains everything ...
-------------
**... to build the ready-to-use SikuliX packages as they are [available from Launchpad](https://launchpad.net/sikuli)**

`It is highly recommended to first look through this README, before clicking around`

**Forking and/or downloading this repo only makes sense:**
 - if you want to get a knowledge about the internals of Sikuli
 - if you want to create your own packages containing Sikuli features
 - if you want to contribute.

<hr />

**To get the ready-to-use packages (IDE, Script, Java/Jython support) <br />it is still recommended [to start here](http://www.sikulix.com).**

<hr />

**Usage docs now on [ReadTheDocs](http://sikulix-2014.readthedocs.org/en/latest/#) (work in progress)**

**Tools I use for developement:** <br />
IDE with Maven and GitHub support: [IntelliJ IDEA CE](https://www.jetbrains.com/idea/) (using Java 7 and 8)<br />
Doc Service: [Read the Docs](https://readthedocs.org)<br />
Build support: [Travis CI](https://travis-ci.org)
WebSite Services: [Host Europe](https://www.hosteurope.de)

The structure of this repo
------------------------

Each folder (module) in this repo is a Maven project by itself with its own POM, but it needs to be in this folder and POM structure, since there is a super POM on the first level, that is the parent POM for all the other child POMs and installs all modules into your local Maven repo. For detailed usage information look further below.

<hr/>

**--- The top level modules (representing the Sikuli features) ---**

Module sikulixapi (folder API) (sikulixapi.jar)
---

**package org.sikuli.script** 

The Java implementation comprising the API to access the top elements (Screen, Region, Pattern, Match, Image, ...) and their methods allowing to search for images and to act on points and matches simulating mouse and keyboard.

The ready-to-use package `sikulixapi.jar` provides this API for Java programming and any Java aware scripting languages.

**package org.sikuli.basics** 

Implements basic utility and helper features used in the top level packages (basic file and folder handling, download features, jar access and handling, export of native libraries, parameter and preferences handling, update and extension handling, ...) and hence it is contained in all packages.

**package org.sikuli.natives** 

Contains the Java sources interface classes (JNI based, mainly SWIG generated) providing the implementation of the OpenCV and Tesseract usage and the implementation of some system specific features (HotKeyHandling, App class support,...).

**package org.opencv. ...** 

Sikuli's image search is based on features of [OpenCV](http://opencv.org). Starting with version 2.4.6 OpenCV provides a self-contained JNI interface to the OpenCV native libraries, allowing to use OpenCV features directly in Java (and hence making C++ programming obsolete for this).

This module contains a specially configured Java/JNI OpenCV package (built using the standard OpenCV configure/make workflow) for use with the OpenCV features currently needed by Sikuli (core, imgproc, feature2d and highgui). The corresponding native library pack (currently Mac only) is contained in the module Libs.

For Windows and Mac the native packages will again be pre-built and ready-to-use. For Linux there will be advices and scripts available to get the needed libraries ready.

Module sikulixide (folder IDE) (sikulix.jar)
---
Implements a GUI using Java, that allows to edit and run Sikuli scripts (currently Jython and JRuby are supported). It is an easy to use IDE focusing on the handling of the screenshots and images used in the typical Sikuli workflows.

The package `sikulix.jar` is the top level package containing all other options (hence the follow up of `sikuli-ide.jar` known from former releases).

After setup this package `sikulix.jar` contains the selected scripting interpreter(s) (Jython and/or JRuby), thus allowing to run Sikuli scripts out of the box from the commandline and providing interactive Sikuli aware scripting shells (hence it includes the functionality known from the `sikuli-script.jar` of former Sikuli(X) releases and is used the same way).

In all cases the Jython and JRuby jar packages are loaded from MavenCentral if needed.

If you want to experiment with the special JRuby support (rSpec, cucumber, ...) you have to look into the modules JRubyAddOns and JRubyGem. Both have to be built manually if needed (not contained in the local developement build).

**The SikuliX-IDE will not get any fixes or improvements anymore past version 1.1.2 (as of March 2018).** 

Version 2 will use a different approach based on an available editor package (probably JEdit).
<hr/>

**--- The helper/utility modules (intended for internal and/or developement use only) ---**

**Module Setup**

It produces the fat jar `sikulixsetup.jar` being the root downloadable artefact. It is needed to setup the SikuliX packages to be used on the local systems. Though the preferred setup is to let setup download the needed stuff on the fly, there is the possibility to run setup completely local/offline after having downloaded the needed stuff manually ([look here ...](http://www.sikulix.com/quickstart.html#qs2)) 

<hr/>

Usage - basic information
-------------------------

If you intend to compile and build the modules after having downloaded this repo, you should have a valid Maven 3 installation and for editing, testing and integration some IDE, that is enabled for working with Maven projects and has support for Git repositories (I myself use NetBeans 8, which supports both out of the box).

**Take care** Even if you only want to work on one of the modules (e.g. API), the modules should not be moved around, but stay in the structure of the downloaded repo. Each module depends on the parent POM in the root as well as the ready-to-use-jar-production POMs, that additionally depend on the assembly descriptors.

**--- Mandatory first step**

In the root directory of the repo run <br />
`mvn clean install`<br />
which builds all modules and installs the artifacts into your local Maven repository.

**Be aware** This mandatory first step will add "tons" of additional stuff from Maven Central repository to your local Maven repository, especially when you are a first time Maven user.

More details for Maven aspects you can find [here ...](https://github.com/RaiMan/SikuliX-2014/wiki/More-information-related-to-the-Maven-aspects-in-this-project)

**--- How to produce the ready to use jars ---**

... `sikulix.jar` and `sikulixapi.jar`

[please look here](https://github.com/RaiMan/SikuliX-2014/wiki/How-to-produce-the-ready_to_use-jar-packages)
