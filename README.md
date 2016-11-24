# CS5390-ServerChat

To run the client:
-host 192.168.1.5 -user Adrian -pass testpass2 -udpportserver 8080 -tcpport 8081

To run the server:
-file C:\Users\adisor\IdeaProjects\CS5390-ServerChat\src\main\resources\users -udpport 8080 -tcpport 8081

Command line commands are listed in ProtocolInputCommands class

Logging is enabled, but if you want to disable it, change to following line:
log4j.rootLogger=DEBUG, STDOUT
to
log4j.rootLogger=off, STDOUT