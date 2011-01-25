Description
-----------

Sonatype [Maven Shell](http://mvnsh.sonatype.org) (`mvnsh`) - OSS Version

License
-------

[Eclipse Public License 1.0](http://www.eclipse.org/org/documents/epl-v10.html)

Features
--------

* Interactive shell
* Integrated archetype support
* [Growl][1] build notifications (Using AppleScript or [JNA][2])
* Optional support for ANSI color on Windows (using [JNA][2])

Maven Specific Features
--------

* Maven 3.x `mvn` command
* Colorized Maven output
* AHC Aether Connector

This Aether connector is backed by the Async HTTP Client (AHC) with the default HTTP provider being Netty. We actively work on AHC and will continually be improving AHC to make the transport 100% reliable. The features implemented so far are:

-  Resumable Downloads
-  Arbitrarily large file support (using AHC zero byte copy)
-  HTTP to HTTPS redirect support
-  NTLM support (v1 and v2)
-  WebDAV PUT support

Support
-------

To submit an issue, please use the [Sonatype Issue Tracker](https://issues.sonatype.org/browse/MVNSH).

Building
--------

### Requirements

* [Maven](http://maven.apache.org) 2+
* [Java](http://java.sun.com/) 5+

Check-out and build:

    git clone git://github.com/sonatype/mvnsh.git
    cd mvnsh
    mvn install

Extract the assembly and execute the shell:

    unzip mvnsh-assembly/target/mvnsh-*-bin.zip
    ./mvnsh-*/bin/mvnsh

[1]: http://growl.info/
[2]: https://jna.dev.java.net/
