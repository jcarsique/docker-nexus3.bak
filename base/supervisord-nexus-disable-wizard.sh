#!/bin/sh -ex

grep -q 'nexus.onboarding.enabled=false' /nexus-data/etc/nexus.properties && exit 0
echo 'nexus.onboarding.enabled=false'  >> /nexus-data/etc/nexus.properties
