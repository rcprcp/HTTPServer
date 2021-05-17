# HttpServer
Mock HTTP server for troublehooring, testing and debugging HTTP Client issues.

Trying to only use standard Java libraries.  This program is using  com.sun.net.httpserver and java.net  - currently the only external libraries are for JSON parsing the to support log4j output.

##Usage:
Run the program like this - note the placement of the program's options.
```
java -jar HttpServer-1.0-SNAPSHOT-jar-with-dependencies.jar --debug --null
```

there are a few command line options.

--debug enable debug logging

--null   inject a null payload on the 5th record.

