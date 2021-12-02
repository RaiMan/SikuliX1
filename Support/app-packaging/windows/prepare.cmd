set version=2.0.6-SNAPSHOT
licensedir=..\..\..\IDE\src\main\resources\Settings\LicenseIDE
copy $licensedir\LICENSE + $licensedir\THIRD-PARTY.txt app-package\license.txt
jardir=..\..\..\IDE\target
copy $ardir\*-macm1.jar /B app-package\jar\sikulixide-%version%.jar /B
