
REM. start script for signalk on windows pc
REM. Assume we are starting in the signalk directory
REM.set SIGNALK_HOME=C:\signalk

set SIGNALK_HOME=%CD%

set JAR=signalk-server-0.0.1-SNAPSHOT-jar-with-dependencies.jar
REM.
cd %SIGNALK_HOME%
mkdir logs

REM.start server
set JAVA=java

REM. You may need to set the java version spcifically
REM. If so uncomment and edit the following to suit your install
REM. set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_07
REM. set JAVA=%JAVA_HOME%\bin\java

set EXT="-Djava.util.Arrays.useLegacyMergeSort=true"

REM. optionally limit memory here
REM. set MEM="-Xmx32m -XX:PermSize=32m -XX:MaxPermSize=48m"

set LOG4J=-Dlog4j.configuration=file:/%SIGNALK_HOME%/conf/log4j.properties

echo "Starting: %JAVA% %EXT% %LOG4J% %MEM% -jar target/%JAR%" >logs\start.log 2>&1 
"%JAVA%" %EXT% %LOG4J% %MEM% -jar target\%JAR% >>logs\start.log 2>&1 

pause 