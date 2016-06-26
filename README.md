Signal K java server
=================================================

An example Signal K server using java. If you are after a quick install and trial of the java signalk server then you should use the signalk-java project. Its easier to install and has the front end that was previously in this project.

Provided under an Apache 2 licence

Current capabilities:
* Accepts as input:
	* NMEA0813
	* AIS 
	* NMEA2000 in Canboat format
	* Signalk json
* Reads input from:
	* TCP/IP sockets
	* Websockets
	* HTTP via REST api
	* MQTT
	* STOMP
	* Serial
	* USB
	* local files
* Outputs to:
	* TCP/IP sockets
	* UDP sockets
	* Websockets
	* HTTP via REST api
	* MQTT
	* STOMP
	* Serial
	* USB
* Supports:
	* *NEW: provides discovery over Bonjour, Zeroconf and DNS-SD*
	* AIS targets displayed
	* TCP and MQTT clients (listen to remote servers)
    * On-demand user apps, sort of works, anyway :-)  Bit early for some of the apps yet.
	* Delta and Full signalk formats, and translations between them.
	* Subscriptions, with * and ? wildcards. Configurable format, period and delivery policy
	* LIST - get a list of available signalk keys with * and ? wildcard support
	* GET - get data matching keys on demand
	* PUT - send data on demand 
	* UPDATES - periodic messages.
	* has http://hawt.io management console on localhost:8000/hawtio 
	* Anchor watch
	* metadata based alarms and gui configs.
	
* Todo:
	* _attr based security

Using
=====
The project is not in any public maven repository yet but is available from jitpack.io. 
To use that version add the following to your pom.xml:
```
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
	<dependency>
		<groupId>com.github.SignalK</groupId>
		<artifactId>signalk-server-java</artifactId>
		<!--choose appropriate version-->
		<version>JIT-Deploy-07</version>
	</dependency>
```

Development
===========

The project is developed and built using maven and eclipse. 

You will need to clone the signalk-core-java project and build it with maven , then the signalk-server-java project. 

The default signalk-server-java build will use the most recent jitpack.io signalk-core build, so for dev you need to set the system property in maven as follows. This will cause the builds to use the dev dependencies, from your local repository.

```
mvn -Dsignalk.build=dev install
```

The signalk-core-java project is usable separately and contains the core model, and useful helpers.

