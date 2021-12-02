set version=2.0.6-SNAPSHOT
set licensedir=..\..\..\IDE\src\main\resources\Settings\LicenseIDE
copy $licensedir\LICENSE + $licensedir\THIRD-PARTY.txt app-package\license.txt
set jardir=..\..\..\IDE\target
copy $ardir\*-win.jar /B app-package\jar\sikulixide-%version%.jar /B
