@echo off
REM Script to set JAVA_HOME and PATH for Java 25
set JAVA_HOME=%USERPROFILE%\Downloads\jdk-25_windows-x64_bin\jdk-25
set PATH=%JAVA_HOME%\bin;%PATH%

echo JAVA_HOME set to: %JAVA_HOME%
echo Java version:
java -version
echo.
echo Maven wrapper should now work. Run:
echo .\mvnw.cmd spring-boot:run