#!/bin/sh -ex

grep -q '-Djava.util.prefs.userRoot=/nexus-data/.java' /opt/sonatype/nexus/bin/nexus.vmoptions && exit 0
echo '-Djava.util.prefs.userRoot=/nexus-data/.java' >> /opt/sonatype/nexus/bin/nexus.vmoptions
