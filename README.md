# Nexus 3

## About / Synopsis

Nuxeo Nexus Docker custom image.

Leverages Nexus API to upload and run Groovy scripts for configuration at boot.

## Table of contents

```bash
├── base                        Base layer
│   ├── Dockerfile
│   ├── Makefile
│   ├── skaffold.yaml
│   ├── supervisord.conf
│   ├── ...
├── builder                     Builder injecting CasC during image build
│   ├── Dockerfile
│   ├── Makefile
│   ├── scripts                 Groovy builder scripts
│   │   ├── build.gradle
│   │   ├── README.md           Developer instructions
│   │   ├── ...
│   └── skaffold.yaml
├── Dockerfile                  Nexus 3 image
├── docs
│   ├── backup_restore.md
│   ├── CasC.md
│   ├── jenkins-x-charts-nexus  Extracts from https://github.com/jenkins-x-charts/nexus/
│   ├── sonatype-nexus-charts   Extracts from https://github.com/helm/charts/tree/master/stable/sonatype-nexus
│   ├── UPGRADE_NOTES.md
│   └── values.yaml             Sample
├── Jenkinsfile
├── make.d
│   └── skaffold.mk
├── Makefile
├── parms                       Nexus 3 image customizations
│   ├── central                 https://packages.nuxeo.com/
│   ├── cluster                 https://packages.dev.nuxeo.com/
│   ├── jenkins-x               Jenkins X default Nexus 3
│   ├── maven-ncp               SUPINT-1574 NCP mirror
│   └── team                    Nuxeo default Nexus 3 for JX team https://packages-<team>.dev.nuxeo.com/
├── postStart.sh
├── README.md
├── skaffold.yaml
└── utils                       Utilitaries for basic usage
```

## Usage

### QA/CI

[![master Build Status](https://jenkins.admin.dev.nuxeo.com/buildStatus/icon?job=nuxeo/docker-nexus3/master)](https://jenkins.admin.dev.nuxeo.com/job/nuxeo/job/docker-nexus3/job/master/)

### Development

#### Requirements

* GNU Make
* Docker
* JX
  * jx-base builder
* Kubectl
* ksync

#### Environment Setup

```bash
aws-okta exec devtools -- aws sts get-caller-identity
```
#### Build

##### Code Driven

Build and push custom Nexus 3 Docker images:

* `nuxeo/nexus3/base`
* `nuxeo/nexus3/builder`
* `nuxeo/nexus3/jenkins-x`
* `nuxeo/nexus3/central`
* `nuxeo/nexus3/cluster`
* `nuxeo/nexus3/maven-ncp`
* `nuxeo/nexus3/team`

On `PR-*|feature-*|fix-*` branch pattern, tag is `0.0.0-<branch-name>-<build-number>`

On `master` branch, tag is using `jx-release-version`

The [Jenkins pipeline](Jenkinsfile) is mainly invoking Make steps as described in the [DevPod](#devpod) section.

##### DevPod

The DevPod allows to execute Make with the same environment as the CI.

Setup `ksync` In a separate terminal:

```bash
# Select JX context and team
jx context
jx team

ksync init
# Ignore rpc error

ksync watch
# watch is unstable: restart it in case of crash
```

```bash
git clone git@github.com:nuxeo/docker-nexus3.git
cd docker-nexus3
jx create devpod --sync=true --label jx-base --auto-expose=false --import=false

# create the base image
make base
# create the builder image
make builder
# create the central image
make central
```

##### Locally

Dockerfile build arguments and their default value:

* Builder
  * N/A
* Base
  * `NEXUS3_VERSION`
    * [Makefile](Makefile) = 3.20.1
    * [builder/scripts/build.gradle](builder/scripts/build.gradle) = 3.20.1-01
    * [base/Dockerfile](base/Dockerfile) = unknown
    * [utils/build.sh](utils/build.sh) = latest
  * `VERSION`= 0.1-SNAPSHOT
  * `SCM_REF`= unknown
  * `SCM_REPOSITORY`= git@github.com:nuxeo/docker-nexus3.git
  * `DESCRIPTION`= "Base Nexus 3 image layer for Nuxeo custom deployments"
* Nexus3
  * Base arguments are inherited
  * `VERSION` = 0.1-SNAPSHOT
  * `DOCKER_REGISTRY` = jenkins-x-docker-registry
    * [make.d/skaffold.mk](make.d/skaffold.mk) = jenkins-x-docker-registry
    * [Dockerfile](Dockerfile) = jenkins-x-docker-registry
    * [utils/build.sh](utils/build.sh) = localhost:5000
  * `PARMS` = jenkins-x
  * `DESCRIPTION` = "JX default Nexus 3"

See [https://hub.docker.com/r/sonatype/nexus3/tags]() for the latest version.

```bash
export NEXUS3_VERSION=3.20.1
export SCM_REF=`git id`
export VERSION=`git rev-parse --symbolic-full-name --abbrev-ref HEAD`
export DOCKER_REGISTRY=localhost:5000

echo $NEXUS3_VERSION $(git rev-parse --short HEAD) $VERSION $DOCKER_REGISTRY
docker build --build-arg NEXUS3_VERSION --build-arg SCM_REF \
             -t $DOCKER_REGISTRY/nuxeo/nexus3/base:$VERSION base

docker build -t $DOCKER_REGISTRY/nuxeo/nexus3/builder:$VERSION builder

export PARMS=jenkins-x
docker build --build-arg DOCKER_REGISTRY --build-arg VERSION --build-arg SCM_REF \
             --build-arg PARMS -t localhost:5000/nuxeo/nexus3/$PARMS:$VERSION .
```

The custom configuration files are stored in the `<PARMS>` folders.

Usage:

```bash
export SCM_REF=`git id`
export VERSION=`git rev-parse --symbolic-full-name --abbrev-ref HEAD`
export DOCKER_REGISTRY=localhost:5000
export PARMS=<PARMS>
export DESCRIPTION=<DESCRIPTION>

echo $(git rev-parse --short HEAD) $VERSION $DOCKER_REGISTRY
docker build --build-arg VERSION --build-arg SCM_REF [--build-arg DOCKER_REGISTRY] \
             [--build-arg PARMS] [--build-arg DESCRIPTION] \
             [-t nuxeo/nexus3/$PARMS[:$VERSION]] .

# Sample with 'central'
export PARMS=central
export DESCRIPTION="README sample with $PARMS parameter to build packages.nuxeo.com"
docker build --build-arg VERSION --build-arg SCM_REF --build-arg DOCKER_REGISTRY \
             --build-arg PARMS --build-arg DESCRIPTION \
             -t nuxeo/nexus3/$PARMS:$VERSION .
```

The custom parameters folder name, `<PARMS>`:

* [`central`](parms/central): Nuxeo Central <https://packages.nuxeo.com/>
* [`cluster`](parms/cluster): jx-prod cluster <https://packages.dev.nuxeo.com/>
* [`jenkins-x`](parms/jenkins-x): Jenkins X default. Not used.
* [`team`](parms/team): Team sample. Not used.
* [`maven-ncp`](parms/maven-ncp): SUPINT-1574 NCP mirror
* `<team>`: Team customization (dedicated Git repository). <https://packages.<team>.dev.nuxeo.com>

#### Upgrade Nexus Version

The Nuxeo image build inherits from some resources. Some others have been forked.
Here are the related resources to look at when upgrading the Nexus version (`NEXUS3_VERSION`):
* Nexus archive
  * https://help.sonatype.com/repomanager3/download
  * https://help.sonatype.com/repomanager3/release-notes/
* Nexus Docker image `sonatype/nexus3`
  * https://hub.docker.com/r/sonatype/nexus3
  * https://github.com/sonatype/docker-nexus3
* Nuxeo Docker image
  * [Makefile](Makefile)
  * [builder/scripts/build.gradle](builder/scripts/build.gradle)
  * [docs/UPGRADE_NOTES.md](docs/UPGRADE_NOTES.md)
* Forked resources
  * JX Docker build
    * see [docs/jenkins-x-charts-nexus](docs/jenkins-x-charts-nexus)
    * forked from https://github.com/jenkins-x-charts/nexus/
  * JX chart deploying `gcr.io/jenkinsxio/nexus`
    * see [docs/jenkins-x-charts-nexus/nexus/values.yaml](docs/jenkins-x-charts-nexus/nexus/values.yaml)
    * forked from https://github.com/jenkins-x-charts/nexus/tree/master/nexus  
  * Sonatype Helm chart deploying `sonatype/nexus3`
    * see [docs/sonatype-nexus-charts/values.yaml](docs/sonatype-nexus-charts/values.yaml)
    * forked from https://github.com/helm/charts/tree/master/stable/sonatype-nexus
* JX Deployment
  * [Team admin values.yaml](https://github.com/nuxeo/jx-admin-env/blob/master/values.yaml)
  * [Team DevTools values.yaml](https://github.com/nuxeo/jx-devtools-env/blob/master/values.yaml)
  * ...

##### Sample Case

Complex sample case with the upgrade from 3.19.0 to 3.20.1.

* Jira: https://jira.nuxeo.com/browse/NXBT-3162
* GitHub PR: https://github.com/nuxeo/docker-nexus3/pull/25
* CI: https://jenkins.admin.dev.nuxeo.com/job/nuxeo/job/docker-nexus3/job/PR-25
* Docker images to test: `docker-registry.admin.dev.nuxeo.com/nuxeo/nexus3/<team>:0.0.0-PR-25-<build>`

A lot to read following the above order of resources: release notes, commits...
* https://help.sonatype.com/repomanager3/release-notes/2019-release-notes#id-2019ReleaseNotes-RepositoryManager3.20.1
* https://issues.sonatype.org/secure/ReleaseNote.jspa?projectId=10001&version=18778
* https://issues.sonatype.org/secure/ReleaseNote.jspa?projectId=10001&version=18521
* https://github.com/sonatype/docker-nexus3/compare/3.19.0...3.20.1
* https://github.com/jenkins-x-charts/nexus/compare/8f46d405f81e0f8da03f51204cac3f6d512f779c...v0.1.20
* `[github.com/helm/charts]$ git logone 1516468...master -- stable/sonatype-nexus/`

#### Release Delivery

* deploy on PPEPROD and STAGING environments
* first validation by the DevTools team
* orchestrate validation by the various teams
* schedule deployments to PROD, see [nuxeowiki/DVT/Maintenance on Production](https://nuxeowiki.atlassian.net/wiki/spaces/DVT/pages/949780624/Maintenance+on+Production)

### Configuration

#### Static

Most configuration data is embedded in the image at build time, coming from the [parms](parms).

#### Secrets

Secrets are injected at run time.

#### Runtime Parameters

* `HAZELCAST`: activate clustering if `true`
* `INSTALL4J_ADD_VM_PARAMS`: JVM parameters
* `ENABLE_ANONYMOUS_ACCESS`: **parameter is ignored** in favor to the JSON security definition.
* `NEXUS_SECURITY_RANDOMPASSWORD`: generate a random admin password if `true`

See also:
* [Sonatype Chart parameters](https://github.com/helm/charts/blob/master/stable/sonatype-nexus/README.md#configuration)

### Run

#### Jenkins X

Patch the `nexus` secret in the target namespace:

```bash
VAULT="nuxeo/vaultbolt-devtools/code/ci-casc/tf/nexus/prod"
NAMESPACE=<TEAM>
export PASSWORD=$(pass show $VAULT/password |base64)
kubectl patch secret nexus -p "{\"data\":{\"password\": \"$PASSWORD\"}}" -n $NAMESPACE
export PASSWORDS=$(pass show $VAULT/passwords.json |base64)
kubectl patch secret nexus -p "{\"data\":{\"passwords.json\": \"$PASSWORDS\"}}" -n $NAMESPACE
export LICENSE=$(pass show $VAULT/license_nexus.lic |base64)
kubectl patch secret nexus -p "{\"data\":{\"license.lic\": \"$LICENSE\"}}" -n $NAMESPACE
```

Then update the chart template: see [docs/values.yaml](docs/values.yaml)

#### Custom Image Run

The configuration Data is provided at instance start.

Usage:

```bash
export CONFIG_PATH=<CONFIG>
export PARMS=<PARMS>
export VERSION=`git rev-parse --symbolic-full-name --abbrev-ref HEAD`

pass show code/ci-casc/tf/nexus/prod/password > $CONFIG_PATH/password
pass show code/ci-casc/tf/nexus/prod/passwords.json > $CONFIG_PATH/passwords.json
pass show code/ci-casc/tf/nexus/prod/license_nexus.lic > $CONFIG_PATH/license.lic

docker run -p 8081:8081 -v $CONFIG_PATH:/opt/sonatype/nexus/config/ \
       [-v nexus-store:/nexus-store] \
       [-v nexus-data:/nexus-data] \
       [-v $CONFIG_PATH/license.lic:/nexus-data/etc/license/.license.lic] \
       --name nexus-$PARMS -itd nuxeo/nexus3/$PARMS:$VERSION

# Alternate method with docker volume (TODO)
docker run --name nexus -p 8081:8081 --mount source=nexus-config,target=/opt/sonatype/nexus/config/ \
       --name nexus-$PARMS -itd nuxeo/nexus3/$PARMS:$VERSION

docker exec nexus-$PARMS cat /nexus-data/admin.password
docker exec -u nexus nexus-$PARMS /opt/sonatype/nexus/postStart.sh
```

TODO: docker volume is preferred.

#### `<CONFIG>`: configuration folder containing

- `password`: the admin credentials file (mandatory)
- `passwords.json`: the users credentials file (optional)

      {
          "user1" : "password1",
          "user2" : "password2"
      }
- `[blobstore|repository|security].json`: optional configuration which overrides image parms

#### `<LICENSE>`: path to the Nexus license file (optional)

#### Data

Data provisioning is performed on start:

- under K8s, the Helm chart executes the [`postStart.sh`](postStart.sh)
- for development, see [builder/scripts/README.md](builder/scripts/README.md)

## Resources

<https://github.com/helm/charts>

<https://github.com/jenkins-x-charts/nexus>

<https://github.com/sonatype-nexus-community/nexus-scripting-examples>

[Nexus Repository Manager 3 > REST and Integration API > Script API > Examples](https://help.sonatype.com/repomanager3/rest-and-integration-api/script-api/examples)

<https://blog.sonatype.com/deploy-private-docker-registry-on-google-cloud-platform-with-nexus>

## Contributing / Reporting issues

See [NXBT Packages Repositories tickets](https://jira.nuxeo.com/issues/?jql=project%20%3D%20NXBT%20AND%20component%20%3D%20%22Package%20Repositories%22)

Create a ticket [NXBT](https://jira.nuxeo.com/secure/CreateIssue!default.jspa?project=NXBT)
with component "Package Repositories"

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

## About Nuxeo

Nuxeo Content Platform is an open source Enterprise Content Management platform, written in Java. Data can be stored in both SQL & NoSQL databases.

The development of the Nuxeo Content Platform is mostly done by Nuxeo employees with an open development model.

The source code, documentation, roadmap, issue tracker, testing, benchmarks are all public.

Typically, Nuxeo users build different types of information management solutions for document management, case management, and digital asset management, use cases. It uses schema-flexible metadata & content models that allows content to be repurposed to fulfill future use cases.

Learn more at [www.nuxeo.com](https://www.nuxeo.com/).
