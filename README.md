<!--

    Copyright (c) 2009-present the original author or authors.

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

Maven Shell (`mvnsh`)

License
-------

[Eclipse Public License 1.0](http://www.eclipse.org/org/documents/epl-v10.html)
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)

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
