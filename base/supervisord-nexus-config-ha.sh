#!/bin/sh -ex

# Delete hazelcast db 
if test -d /nexus-data/db; then
    rm -rf /nexus-data/db/config
    rm -rf /nexus-data/db/component
    rm -rf /nexus-data/db/security
fi

grep -q 'nexus.clustered=true' /nexus-data/etc/nexus.properties && exit 0
echo 'nexus.clustered=true'  >> /nexus-data/etc/nexus.properties
