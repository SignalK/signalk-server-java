Signal K java server
=================================================

An example Signal K server using java
Now under Apache 2 licence

Installation
------------

You will need Java 1.7+ installed. You need to be able to type 'java' on the command line and get java responding, or you need to edit the JAVA_HOME variable in start.sh or start.bat.

```shell
$ git clone https://github.com/SignalK/signalk-java-server.git
```

Then on Linux:

```shell
$ cd Signalk-server
$ ./startpc.sh
```
NOTE: Windows users - DONT put any of this in directories with spaces or anything but simple ascii names. Use something like eg C:\dev\SignalK-server
Use the startpc.bat file to launch. 

You should now have a SignalK server running:

* webserver on `http://localhost:9290` 
	* REST api on http://localhost:9290/signalk/api
	* Authentication on http://localhost:9290/signalk/auth - but its a pass all for now so you dont need to login
* websockets server on `http://localhost:9292`. 
* signalk output streamed as TCP over port 5555. On linux you can watch this with `$ ncat localhost 5555` **see below for subscriptions
* nmea output will be streamed as TCP over port 5556. On linux you can watch this with `$ ncat localhost 5556`, or use telnet to connect.

It will be streaming a demo file and dumping logging to screen. Control logging by editing conf/log4j.properties.

Try `http://localhost:9290/signalk/api/vessels` to see some output. 

You can drill down by adding json fields, eg `http://localhost:9290/signalk/api/vessels/367153070` or `http://localhost:9290/signalk/api/vessels/motu`

The content mime type is application/json, your browser may not display it directly. On firefox just install https://addons.mozilla.org/en-us/firefox/addon/jsonovich/?src=search

It currently streams out a demo file taken from a boat sailing in a race in San Francisco. The output includes AIS data. 
If you edit the `conf/signalk.cfg` file and make `signalk.demo=false` (default=true), then it will stop doing that.
Normally it only sends output in signalk delta format to subscribed clients, so clients MUST subscribe or you see only the heartbeat message every 1000ms.
You can subscribe by sending the following json. It supports * and ? wildcards In linux you can paste it into the screen you opened earlier and press [Enter]. :
```
{"context":"vessels.self","subscribe":[{"path":"environment.depth.belowTransducer"},{"path":"navigation.position"}]}
``` 
Then you will see those values every 1000ms.

Try:
```
{"context":"vessels.366982320","subscribe":[{"path":"navigation.position"}]}

{"context":"vessels.*","subscribe":[{"path":"navigation.position"}]}

{"context":"vessels.366982320","subscribe":[{"path":"navigation.position"}]}
{"context":"vessels.366982320","unsubscribe":[{"path":"navigation.position"}]}

{"context":"vessels.*","subscribe":[{"path":"navigation.position.l*"}]}
{"context":"vessels.*","unsubscribe":[{"path":"navigation.position.l*"}]}

{"context":"vessels.*","subscribe":[{"path":"navigation.course*"}]}
{"context":"vessels.*","unsubscribe":[{"path":"navigation.course*"}]}

``` 

Devices (aka GPS) attached on serial>USB adaptors will be automatically found and used. The input can be NMEA0183 compatible, or signalk, and expects 38400 baud by default. The configuration can be changed by editing conf/signalk.cfg

Development
-----------
The project is developed and built using maven and eclipse. You will need to clone the signalk-core-java project and build it with maven , then the signalk-server-java project.
The signalk-core-java project is usuable separately and contains the core model, and useful helpers.

Roadmap
-------
This is a first cut of the server and more functionality will be added:
* Better documentation (always)
* A better configuration method
* Completed security framework
* Completed REST API
* Support for client modules on demand
* and more


See http://www.42.co.nz/freeboard and http://http://signalk.github.io/ for more.