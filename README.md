Signal K java server
=================================================

An example Signal K server using java

Currently this is GPL until I get a better understanding of its direction. It uses technology from, and will become the core of the freeboard project ( http://www.42.co.nz/freeboard ) 
If you need something more liberal pls raise an issue.

Installation
------------

You will need Java 1.7+ installed.

```shell
$ git clone https://github.com/SignalK/signalk-java-server.git
```

Then

```shell
$ cd Signalk-server
$ ./startpc.sh
```
NOTE: Windows users - DONT put any of this in directories with spaces or anything but simple ascii names. Use something like eg C:\dev\SignalK-server
Use the startpc.bat file to launch. 

You should now have a SignalK server running a webserver and websockets on `http://localhost:9292`. It wont do much until you feed it data, or add clients.
If you edit the conf/signalk.cfg file and make "signalk.demo=true" (default=false), then it will stream out a demo file taken from a boat sailing in a race in San Francisco. The output includes AIS data. 

* Signal K data is output on TCP port 5555. On linux you can watch this with '$ ncat localhost 5555'
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