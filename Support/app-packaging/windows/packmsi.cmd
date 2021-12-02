del app-package\app\*.exe
del app-package\app\*.msi
set version=2.0.6-SNAPSHOT
jpackage --type msi ^
--input app-package\jar ^
--dest app-package\app ^
--app-version 206.000 ^
--name SikulixIDE ^
--icon sikulix.ico ^
--description "Visual automation and testing - SikuliX IDE - edit and run scripts" ^
--vendor "SikuliX - RaiMan" ^
--copyright "Copyright 2021, MIT License" ^
--license-file app-package\license.txt ^
--main-jar sikulixide-%version%.jar ^
--main-class org.sikuli.ide.Sikulix ^
--win-shortcut ^
--win-dir-chooser ^
--add-launcher Sikulix=sxconsole.properties

