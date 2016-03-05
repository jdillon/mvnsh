<!--

    Copyright (c) 2009-present Sonatype, Inc.
    All rights reserved. This program and the accompanying materials
    are made available under the terms of the Eclipse Public License v1.0
    and Apache License v2.0 which accompanies this distribution.
    The Eclipse Public License is available at
      http://www.eclipse.org/legal/epl-v10.html
    The Apache License v2.0 is available at
      http://www.apache.org/licenses/LICENSE-2.0.html
    You may elect to redistribute this code under either of these licenses.

-->
Description
-----------

Sonatype Maven Shell (`mvnsh`)

License
-------

[Eclipse Public License 1.0](http://www.eclipse.org/org/documents/epl-v10.html)
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)

Features
--------

* Interactive shell
* Integrated archetype support
* Optional support for ANSI color on Windows (using [JNA][1])

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

* [Maven](http://maven.apache.org) 3+
* [Java](http://java.sun.com/) 6+

Check-out and build:

    git clone git://github.com/jdillon/mvnsh.git
    cd mvnsh
    mvn install

Extract the assembly and execute the shell:

    unzip mvnsh-assembly/target/mvnsh-*-bin.zip
    ./mvnsh-*/bin/mvnsh

[1]: https://jna.dev.java.net/
