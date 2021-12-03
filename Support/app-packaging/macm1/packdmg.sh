version=2.0.6-SNAPSHOT
mkdir app-package/app
rm app-package/app/*.dmg
jpackage --input app-package/jar \
--type dmg \
--dest app-package/app \
--app-version 206.000 \
--name SikulixIDE \
--icon sikulix.icns \
--description "Visual automation and testing - SikuliX IDE - edit and run scripts" \
--vendor "SikuliX - RaiMan" \
--copyright "Copyright 2021, MIT License" \
--license-file app-package/license.txt \
--main-jar sikulixide-$version.jar \
--main-class org.sikuli.ide.Sikulix
