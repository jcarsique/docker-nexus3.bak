#!/bin/sh -ex

grep -q nexus.licenseFile /nexus-data/etc/nexus.properties && exit 0

[ -f /nexus-data/etc/license/.license.lic ] && echo 'nexus.licenseFile=/nexus-data/etc/license/.license.lic' >> /nexus-data/etc/nexus.properties
[ -f /opt/sonatype/nexus/config/license.lic ] && echo 'nexus.licenseFile=/opt/sonatype/nexus/config/license.lic' >> /nexus-data/etc/nexus.properties
