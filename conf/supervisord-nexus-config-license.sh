#!/bin/sh -ex

grep -q nexus.licenseFile /nexus-data/etc/nexus.properties && exit 0

if [ -f /nexus-data/etc/license/.license.lic ]; then
  echo 'nexus.licenseFile=/nexus-data/etc/license/.license.lic' >> /nexus-data/etc/nexus.properties
elif [ -f /opt/sonatype/nexus/config/license.lic ]; then
  echo 'nexus.licenseFile=/opt/sonatype/nexus/config/license.lic' >> /nexus-data/etc/nexus.properties
fi

