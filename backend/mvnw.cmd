@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2
@REM ----------------------------------------------------------------------------
@IF "%DEBUG%"=="" @ECHO OFF
@SETLOCAL LEAVE_UNSET=
@SETLOCAL ENABLEEXTENSIONS
@SETLOCAL ENABLEDELAYEDEXPANSION

SET "BASE_DIR=%~dp0"
SET "WROPT="
IF EXIST "%BASE_DIR%\.mvn\wrapper\maven-wrapper.properties" (
    FOR /F "tokens=1* delims==" %%A IN ('TYPE "%BASE_DIR%\.mvn\wrapper\maven-wrapper.properties" ^| FINDSTR /R "^distributionUrl"') DO SET "MVN_DIST=%%B"
)
IF "%MVN_DIST%"=="" (
    SET "MVN_DIST=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip"
)

SET "MAVEN_USER_HOME=%USERPROFILE%\.m2"
SET "WRAPPER_DIR=%MAVEN_USER_HOME%\wrapper\dists"

@REM Run maven command
IF EXIST "%BASE_DIR%\.mvn\wrapper\maven-wrapper.jar" (
    "%JAVA_HOME%\bin\java" -jar "%BASE_DIR%\.mvn\wrapper\maven-wrapper.jar" %*
) ELSE (
    mvn %*
)
