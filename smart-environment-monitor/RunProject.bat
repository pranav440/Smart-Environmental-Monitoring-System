@echo off
pushd "%~dp0smart-environment-monitor"
echo Starting Smart Environmental Monitor...
".maven\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run
pause
popd
