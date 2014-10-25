Signal K java server
=================================================

An example Signal K server using java
Installation
------------

```shell
$ git clone https://github.com/rob42/Signalk-server.git
```

Then

```shell
$ cd Signalk-server
$ ./startpc.sh
```
NOTE: Windows users - DONT put any of this in directories with spaces or anything but simple ascii names. Use something like eg C:\dev\SignalK-server
Use the startpc.bat file to launch. 

You should now have a SignalK server running a webserver and websockets on `http://localhost:9292`. It wont do much until you feed it data, or add clients.
* Signal K data is output on TCP port 5555. On linux you can watch this with '$ ncat localhost 5555'
* The REST api is on localhost:9290/signalk/api/ (Browse to localhost:9290/signalk/api/vessels to see the json output, you need auth first)
* The Security api is on localhost:9290/signalk/auth (Browse to localhost:9290/signalk/auth/demoPass to get 'logged in')

Devices (aka GPS) attached on serial>USB adaptors will be automatically found and used. The input can be NMEA0183 compatible, or signalk, and expects 38400 baud by default. The configuration can be changed by editing conf/freeboard.cfg

Roadmap
-------
This is a first cut of the server and more functionality will be added:
* Better documentation (always)
* A better configuration method
* Completed security framework
* Completed REST API
* Support for client modules on demand
* and more


See http://www.42.co.nz/freeboard for more.