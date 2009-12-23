Description
-----------

Sonatype Maven Shell (`mvnsh`) - OSS Version

Building
--------

### Requirements

* [Maven](http://maven.apache.org) 2.x
* [Java](http://java.sun.com/) 5

Check-out and build:

    git clone git@github.com:sonatype/mvnsh.git
    cd mvnsh
    mvn install

After this completes, you can unzip the assembly and have a go with the shell:

    unzip mvnsh-assembly/target/mvnsh-*-bin.zip
    ./mvnsh-*/bin/mvnsh
