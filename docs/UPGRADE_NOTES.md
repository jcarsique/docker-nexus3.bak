# Upgrade Notes

This file gathers the upgrade notes from Jira tickets.
It also give pointers to the embedded components upgrade notes. Along with some extracts when relevant.

## DevTools Image

[NXBT-3159](https://jira.nuxeo.com/browse/NXBT-3159) - Upgrade Nexus servers to 3.20.1
* User and developer documentation
* License: `/nexus-data/etc/license/.license.lic` takes precedence over `/opt/sonatype/nexus/config/license.lic`
* fix .java path
* Enable/Disable HAZELCAST with environment variable
* enable volume lock
* Upgrade to 3.20.1
* fix default password
* Disable Wizard
* ufix SelectorConfiguration

## Nexus

Upgrade from 3.19.1

### 3.20.1

https://help.sonatype.com/repomanager3/release-notes/2019-release-notes#id-2019ReleaseNotes-RepositoryManager3.20.1
https://issues.sonatype.org/secure/ReleaseNote.jspa?projectId=10001&version=18778

* [NEXUS-22249](https://issues.sonatype.org/browse/NEXUS-22249) - NXRM pro 3.20.0 fails to start

### 3.20.0

https://help.sonatype.com/repomanager3/release-notes/2019-release-notes#id-2019ReleaseNotes-RepositoryManager3.20.0
https://issues.sonatype.org/secure/ReleaseNote.jspa?projectId=10001&version=18521

#### Bug

* [NEXUS-12488](https://issues.sonatype.org/browse/NEXUS-12488) - remote https repository with TLS client certificate loaded in NXRM JVM keystore not trusted
* [NEXUS-20140](https://issues.sonatype.org/browse/NEXUS-20140) - 500 Server Error shown in Chrome console when accessing Support Status page
* [NEXUS-21138](https://issues.sonatype.org/browse/NEXUS-21138) - Snapshot remover leaves maven-metadata.xml files deleted for a long time, breaking builds.
* [NEXUS-21306](https://issues.sonatype.org/browse/NEXUS-21306) - Cannot proxy Docker repos on Bintray
* [NEXUS-21315](https://issues.sonatype.org/browse/NEXUS-21315) - Extremely slow processing in "Docker - Delete unused manifests and images" task
* [NEXUS-21371](https://issues.sonatype.org/browse/NEXUS-21371) - npm package metadata conditional gets from NXRM to another NXRM always receive a response of 200 instead of 304 when content has not changed
* [NEXUS-21589](https://issues.sonatype.org/browse/NEXUS-21589) - Repository health check can fail if the same asset exists in more than one repository.
* [NEXUS-21672](https://issues.sonatype.org/browse/NEXUS-21672) - group repo with proxy repo member to remote group repo responds 404 when remote group repo responds 403 Requested item is quarantined
* [NEXUS-21762](https://issues.sonatype.org/browse/NEXUS-21762) - Connection pool leak when processing 403 (Requested item is quarantined) responses
* [NEXUS-22144](https://issues.sonatype.org/browse/NEXUS-22144) - Slow performance displaying content selectors in UI

#### Improvement

* [NEXUS-9837](https://issues.sonatype.org/browse/NEXUS-9837) - R CRAN format support
* [NEXUS-13433](https://issues.sonatype.org/browse/NEXUS-13433) - Support npm whoami
* [NEXUS-19424](https://issues.sonatype.org/browse/NEXUS-19424) - Add ability to cleanup by "Never downloaded"
* [NEXUS-19811](https://issues.sonatype.org/browse/NEXUS-19811) - Offline/Misconfigured blob store should be flagged in UI
* [NEXUS-20268](https://issues.sonatype.org/browse/NEXUS-20268) - enable HSTS for inbound HTTPS connectors by default
* [NEXUS-20269](https://issues.sonatype.org/browse/NEXUS-20269) - remove jetty-http-redirect-to-https.xml file from distribution
* [NEXUS-21560](https://issues.sonatype.org/browse/NEXUS-21560) - add X-Frame-Options header to some UI URLs to help avoid scanning software false positives for clickjacking
* [NEXUS-22089](https://issues.sonatype.org/browse/NEXUS-22089) - Collecting new metrics for NXRM Professional

#### Story

* [NEXUS-12456](https://issues.sonatype.org/browse/NEXUS-12456) - add support for "npm login" bearer token authentication to proxied upstream NPM private repositories
* [NEXUS-14233](https://issues.sonatype.org/browse/NEXUS-14233) - Support managing Realms via the REST API

## Docker



## JX Deployment

### Upgrade from JX version

If upgrading from JX version to Nuxeo version, you may have to fix the pod, setting ownership to the `nexus` user on its `nexus-data` folder.

### JenkinsX Chart

See [jenkins-x-charts-nexus/nexus/values.yaml](jenkins-x-charts-nexus/nexus/values.yaml)

```
name: nexus
version: 0.0.1-SNAPSHOT
```

Upgrade from 0.0.28

### 0.1.20

```bash
$ git logone v0.0.28... -- postStart.sh Dockerfile *.json nexus/
> ee811a7 2020-01-08 Cosmin Cojocar       fix: change correctly the admin password   (origin/master, origin/HEAD, master) N
> 5644331 2020-01-08 Cosmin Cojocar       Revert "fix: set up porperly the nexus admin password"   N
> 5fb9cbb 2020-01-07 Cosmin Cojocar       fix: set up porperly the nexus admin password   N
> ee66e79 2020-01-06 Cosmin Cojocar       chore: update the nexus docker image to version 3.20.1   N
> 545ce07 2019-08-16 AlienResidents       Modified arguments requirements for createOrUpdateAndRun to need just the path to the repository. Added -t to mapfile to remove newlines.   N
> 85ca107 2019-08-16 AlienResidents       Quoted variables. Added NEXUS_BASE_DIR, and NEXUS_REPO_DIR vars.  Implemented shellcheck recommendations for using find, mapfile, and a few others.   N
> 8f46d40 2019-02-22 Ilya Shaisultanov    feat: configure anon access via env vars   N
```

## Helm Chart

It's the original Sonatype chart forked by JenkinsX.
See [sonatype-nexus-charts/values.yaml](sonatype-nexus-charts/values.yaml)

```
name: sonatype-nexus
version: 1.21.3
appVersion: 3.20.1-01
```

```bash
$ git logone 1516468...master -- stable/sonatype-nexus/
> 2a224f63f 2019-12-30 Andrew Stoltz        [stable/sonatype-nexus] Use official sonatype nexus image. Keep default admin123 password. (#19791)   N
> c6c8806b7 2019-12-05 Varditn              commit all changes in one commit (#18921)   N
> 08202c438 2019-11-18 Javier Mart<C3><AD>nez      Added image pull secret option to download custom image from private repository (#18760)   N
> 7d0722e3a 2019-10-22 Daniel Whatmuff      [stable/sonatype-nexus] adding ACM cert annotations and LoadBalancer source IP whitelisting support (#16577)   N
> b5b1d5c6b 2019-07-19 Hec                  [stable/sonatype-nexus] Allow for additional ingress/service customization (#15662)   N
> ae36a617a 2019-06-04 Guillaume Perrin     [stable/sonatype-nexus] Fix ingress when proxy service is renamed (#14437)   N
> 4564bfa96 2019-04-29 hamza3202            [stable/sonatype-nexus] Add openshift routes (#13248)   N
> da27b8854 2019-04-24 hamza3202            [stable/sonatype-nexus]Nexus additional volume mounts (#13243)   N
> 5823251a8 2019-02-20 Sergei Ivanov        [stable/sonatype-nexus] Reorder host names in ingress (#11513)   N
> 282596242 2019-02-18 Sergei Ivanov        Reuse nexus.fullname template in nexus.proxy-ks.name (#11083)   N
> e42c96cf9 2019-01-07 Varditn              Updting secret (#10429)   N
> 8bceed589 2018-12-12 Varditn              updating images' tags and support org verification toggle (#9945)   N
> a2bd9bd73 2018-10-29 Diwakar              [stable/sonatype-nexus] create proxy service only if nexus-proxy is enabled (#8629)   N
> 49b4a376c 2018-10-26 guptaarvindk         nexus/clusterIP-headlessService (#8565)   N
> 74d0a348b 2018-10-19 Aleksandr Stepanov   Added Pod annotations for Nexus Helm Chart (#8569)   N
> 2bc37c9c1 2018-10-17 Faizan Ahmad          Add support to change service name (#8506)   N
> e67166dec 2018-10-15 Faizan Ahmad         Add support for additional secret, initContainers, postStart command and deployment annotations (#8338)   N
> 80a49ba26 2018-10-10 Faizan Ahmad         [Stable] Add support for configmap, additional volumes and volume mounts sonatype-nexus chart (#8226)   N
> 91c7c01b2 2018-10-02 Varditn              add ability to configure deployment strategy (#8073)   N
> e60a3ca69 2018-09-26 Kyle Williams        Add service label values for Nexus (#7973)   N
> 4c2c66d51 2018-09-26 Varditn              Add PVs manifests,Tolerations and updated docker images. (#7919)   N
> 057e05111 2018-08-30 Jenny Li             [stable/sonatype-nexus] add annotations for proxy-svc to support LoadBalancer (#7421)   N
> 000d2ac52 2018-08-17 Reuben James         [stable/sonatype-nexus] Enable use of hostAliases (#7228)   N
> 814ff9036 2018-07-05 jheyduk              add configurable path for liveness / readinessProbe (#6445)   N
> 30acbdd9e 2018-06-27 Maxime Guyot         Add resource requests and limits support for nexus (#6358)   N
> aa50b4d8c 2018-05-15 Bren Briggs          [stable/sonatype-nexus] Modify ingress rules to allow paths other than / (#5564)   N
> 54f00dfcd 2018-04-25 Miouge1              Add support for default certificate (#5223)   N
> 9954fc85f 2018-04-25 Adrien Chardon       [stable/sonatype-nexus] Fix resources not applied + allow env var (#5218)   N
> 7c41a0cf4 2018-04-20 jeff-knurek          [stable/sonatype-nexus] a significant refactor of the nexus chart that allows for easy backups and proxy (#3617)   N
> 71cafaded 2018-03-29 rodesousa            [stable/sonatype-nexus] fix reusability of existing claim (#4438)   N
> d88034c05 2018-03-12 rodesousa            [stable/sonatype-nexus] Add annotation configuration in pvc (#4057)   N
```
