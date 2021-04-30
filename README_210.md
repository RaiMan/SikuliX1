**Current development version is 2.1.0** (branch `master` nightly builds / snapshots):<br>
[![Build Status](https://travis-ci.org/RaiMan/SikuliX1.svg?branch=master)](https://travis-ci.org/RaiMan/SikuliX1)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FRaiMan%2FSikuliX1.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2FRaiMan%2FSikuliX1?ref=badge_shield)

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
