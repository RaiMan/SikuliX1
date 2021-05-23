SikuliX IDE
===

Implements a GUI using Java, that allows to edit and run Sikuli scripts 
(currently Jython and JRuby are supported). 
It is an easy to use IDE focusing on the handling of the screenshots 
and images used in the typical Sikuli workflows.

It can also be used to run scripts from commandline.

---
**2.1.0 - Current development focus**

---
The **IDE GUI features** are being completely revised including some changes in the Look&Feel:
- Buttons and popup-menus
- thumbnail-images and their behavior on click (features and options)
- image preview feature (adjust pattern options and image optimization)
- global preferences
- per script preferences
- auto-complete including auto-capture
- recorder feature
- generic support for right-click (context menus)

... work in progress

---
**Important information for the use of the 2.1.0 snapshot jars**

---
**Java 11+ and 64-Bit support required**

Make sure you are using Java 11 or later when running SikuliX IDE 2.1.0 (either from commandline with java command or by 
double-clicking the jar-file).

Recommendation: Use the packages from [AdoptOpenJDK](https://adoptopenjdk.net).

**The jars are system-specific**

After download you will have jars, that only run on the system, they are built for:
 - Windows: sikulixwin-2.1.0-SNAPSHOT-*date*-*time*-*N*.jar
 - macOS: sikulixmac-2.1.0-SNAPSHOT-*date*-*time*-*N*.jar
 - Linux: sikulixlux-2.1.0-SNAPSHOT-*date*-*time*-*N*.jar

... where `*date*-*time*-*N*` is a timestamp of the time, the jar was created on OSSRH.

Feel free, to rename the jar to whatever you need/want.

**Jython is included**

The content of `jython-2.7.2-slim.jar` available on Maven Central is included in the SikuliX IDE, 
so Jython scripting is available out of the box.

**JRuby supported, but not included**

If you want to use Ruby scripting via JRuby support, you have to take care, 
that a JRuby jar (version 9.2.11+) is on the Java classpath when running the SikuliX IDE.

**OpenCV support is included**

The OpenCV libraries and the Java interface are included in the SikuliX IDE. It is based on the contents of 
[OpenPnP::OpenCV](https://github.com/openpnp/opencv) and currently on `OpenCV version 4.5`.

`Note on Linux`: The included libraries are built and tested on recent Ubuntu versions. In case of problems 
or for not compatible Linux flavours you have to find a solution. Feel free, to post an issue 
with complete information about your environment and your trials.

**OCR support via Tesseract**

OCR support is included via the package [Tess4J](https://github.com/nguyenq/tess4j) and currently
on `version 4.5.4` based on the `Tesseract` libraries `version 4.1.1` and `Leptonica` libraries `version 1.79`.
- Windows: the Tesseract pre-built libraries are included and automatically made available at runtime.
- macOS: preferably use HomeBrew to `brew install tesseract`, which should install `version 4.1.1` 
  and take care for any dependencies
- Linux: use a suitable way, to get the Tesseract and Leptonica libraries available on the system library path.

**VNC and Android support suspended**

The features are currently not available and might come back later.
