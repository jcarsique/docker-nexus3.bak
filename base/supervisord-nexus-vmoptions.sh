#!/bin/sh -ex

grep -q '-Djava.util.prefs.userRoot=/opt/sonatype/nexus/.java' /opt/sonatype/nexus/bin/nexus.vmoptions && exit 0
echo '-Djava.util.prefs.userRoot=/opt/sonatype/nexus/.java' >> /opt/sonatype/nexus/bin/nexus.vmoptions
