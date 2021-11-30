del *.exe
java -version
jpackage --input src ^
--app-version 2.0.6 ^
--name SikulixIDE ^
--icon sikulix.ico ^
--main-jar sikulixide-2.0.6.jar ^
--main-class org.sikuli.ide.Sikulix ^
--win-shortcut ^
--win-dir-chooser ^
--add-launcher Sikulix=sxconsole.properties

