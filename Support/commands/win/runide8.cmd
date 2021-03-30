@echo off
SETLOCAL ENABLEEXTENSIONS
set ide=C:\Users\rmhde\IdeaProjects\SikuliX1\IDE\target\sikulixide-2.0.6-SNAPSHOT-complete-win.jar
set java="C:\Program Files\AdoptOpenJDK\jdk8\bin\java.exe"
echo running %ide%
%java% -jar %ide% %*
ENDLOCAL
