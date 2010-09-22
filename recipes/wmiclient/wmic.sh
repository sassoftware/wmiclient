#!/bin/bash
#
# Copyright (c) 2010 rPath, Inc.
#

#
# Wrapper around java wmiclient
#

. /etc/profile.d/sun-jre.sh

WMICDIR="%(wmicdir)s"

CLASSPATH=""
for jar in $WMICDIR/*.jar ; do
    CLASSPATH="$CLASSPATH:$jar"
done

java -classpath $CLASSPATH com.rpath.management.windows.WMIClientCmd $@
