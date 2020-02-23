#!/bin/sh -ex

grep -q "shiro.globalSessionTimeout" /nexus-data/etc/nexus.properties && exit 0

echo 'shiro.globalSessionTimeout=300000' >> /nexus-data/etc/nexus.properties
