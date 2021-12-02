version=2.0.6-SNAPSHOT
licensedir=../../../IDE/src/main/resources/Settings/LicenseIDE
mkdir app-package
cp $licensedir/LICENSE app-package/license.txt
cat $licensedir/THIRD-PARTY.txt >>app-package/license.txt
jardir=../../../IDE/target
mkdir app-package/jar
cp $jardir/*-mac.jar app-package/jar/sikulixide-$version.jar
