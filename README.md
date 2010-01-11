Description
-----------

Sonatype Maven Shell (`mvnsh`) - OSS Version


License
-------

[Eclipse Public License 1.0](http://www.eclipse.org/org/documents/epl-v10.html)

Features
--------

* Interactive shell
* Maven 3.x `mvn` command
* Colorized Maven output
* Integrated archetype support
* [Growl][1] build notifications (Using AppleScript or [JNA][2])
* Optional support for ANSI color on Windows (using [JNA][2])


Building
--------

### Requirements

* [Maven](http://maven.apache.org) 2+
* [Java](http://java.sun.com/) 5+

Check-out and build:

    git clone git@github.com:sonatype/mvnsh.git
    cd mvnsh
    mvn install

Extract the assembly and execute the shell:

    unzip mvnsh-assembly/target/mvnsh-*-bin.zip
    ./mvnsh-*/bin/mvnsh


[1]: http://growl.info/
[2]: https://jna.dev.java.net/
