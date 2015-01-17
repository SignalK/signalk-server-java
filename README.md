Signal K java server
=================================================

An example Signal K server using java
Now under Apache licence

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

You should now have a SignalK server running a webserver and websockets on `http://localhost:9292`. It will be streaming a demo file and dumping logging to screen. Control logging by editing conf/log4j.properties.

It currently streams out a demo file taken from a boat sailing in a race in San Francisco. The output includes AIS data. 
If you edit the conf/signalk.cfg file and make "signalk.demo=flase" (default=true), then it will stop doing that.
Normally it only sends output in signalk delta format to subscribed clients, so clients MUST subscribe using the REST API, but we are still testing that so it also pings out changes every 1000ms. 

* Signal K data is also streamed on on TCP port 5555. On linux you can watch this with '$ ncat localhost 5555'
* The REST api is on localhost:9290/signalk/api/ (Browse to localhost:9290/signalk/api/vessels to see the json output, you need auth first)
* The Security api is on localhost:9290/signalk/auth (Browse to localhost:9290/signalk/auth/demoPass to get 'logged in')

Devices (aka GPS) attached on serial>USB adaptors will be automatically found and used. The input can be NMEA0183 compatible, or signalk, and expects 38400 baud by default. The configuration can be changed by editing conf/signalk.cfg

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