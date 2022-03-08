version=2.0.6-SNAPSHOT
licensedir=../../../IDE/src/main/resources/Settings/LicenseIDE
mkdir -p app-package
cp $licensedir/LICENSE app-package/license.txt
cat $licensedir/THIRD-PARTY.txt >>app-package/license.txt
jardir=../../../IDE/target
mkdir -p app-package/jar
cp $jardir/*-macm1.jar app-package/jar/sikulixide-$version.jar
rm -f -R app-package/app
