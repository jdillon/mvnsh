#!/bin/bash
#
# Copyright (c) 2009-present the original author or authors.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# and Apache License v2.0 which accompanies this distribution.
#
# The Eclipse Public License is available at
#   http://www.eclipse.org/legal/epl-v10.html
#
# The Apache License v2.0 is available at
#   http://www.apache.org/licenses/LICENSE-2.0.html
#
# You may elect to redistribute this code under either of these licenses.
#

dirname=`dirname $0`
dirname=`cd "$dirname" && pwd`
cd "$dirname"

# This will update all pom.xml files to use version specified as first parameter of this script

newVersion=$1
if [ -z "$newVersion" ]; then
    echo "usage: `basename $0` <new-version>"
    exit 1
fi

exec mvn org.eclipse.tycho:tycho-versions-plugin:0.23.1:set-version \
    -Dtycho.mode=maven \
    -Dartifacts=mvnsh \
    -DnewVersion="$newVersion"
