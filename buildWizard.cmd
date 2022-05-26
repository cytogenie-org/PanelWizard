@echo off
echo 
echo 
echo Building wizard.jar
echo 
echo 

pushd src\org
echo y | rmdir /s  classes
mkdir  classes
javac -d classes -classpath ..\..\lib\beans2nbm.jar;..\..\lib\junit-4.8.1.jar netbeans\api\wizard\*.java netbeans\api\wizard\displayer\*.java netbeans\modules\wizard\*.java netbeans\spi\wizard\*.java
cd classes
copy ..\netbeans\api\wizard\*.properties org\netbeans\api\wizard
copy ..\netbeans\api\wizard\displayer\*.gif org\netbeans\api\wizard\displayer
copy ..\netbeans\modules\wizard\*.properties org\netbeans\modules\wizard
copy ..\netbeans\modules\wizard\displayer\*.png org\netbeans\modules\wizard\displayer

jar cf ..\..\..\wizard.jar org
cd ..
echo y | rmdir /s  classes
popd
