version=2.0.6-SNAPSHOT
mkdir app-package/app
rm -f app-package/app/*.app
jpackage --input app-package/jar \
--type app-image \
--dest app-package/app \
--app-version 206.000 \
--name SikulixIDE \
--icon sikulix.icns \
--description "SikuliX IDE" \
--file-associations ../windows/sxfastartup.properties \
--file-associations ../windows/sxfapython.properties \
--vendor "SikuliX - RaiMan" \
--copyright "Copyright 2021, MIT License" \
--main-jar sikulixide-$version.jar \
--main-class org.sikuli.ide.Sikulix
