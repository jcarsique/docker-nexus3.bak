www.nuxeo.com# Nexus 3

## About / Synopsis

Nuxeo Nexus Docker custom image.

Leverages Nexus API to upload and run Groovy scripts for configuration at boot.

## Table of contents

```bash
├── base                  Base layer
│   ├── Dockerfile
│   ├── Makefile
├── builder               Builder injecting CasC during image build
│   ├── Dockerfile
│   ├── Makefile
│   └── scripts
├── Dockerfile            Nexus 3 image
├── Jenkinsfile
├── make.d
│   └── skaffold.mk
├── Makefile
└── parms                 Nexus 3 image customizations
    ├── central           https://packages.nuxeo.com/
    ├── cluster           https://packages.dev.nuxeo.com/
    ├── jenkins-x         Jenkins X default Nexus 3
    └── team              Nuxeo default Nexus 3 for JX team https://packages-<team>.dev.nuxeo.com/
```

## Usage

### QA/CI

[![master Build Status](http://jenkins.admin.34.74.59.50.nip.io/buildStatus/icon?job=nuxeo/docker-nexus3/master)](http://jenkins.admin.34.74.59.50.nip.io/job/nuxeo/job/docker-nexus3/job/master/)

### Requirements

* GNU Make
* Docker
* JX
  * jx-base builder
* Kubectl
* ksync

### Build

#### Code Driven

Build and push custom Nexus 3 Docker images:

* `nuxeo/nexus3/base`
* `nuxeo/nexus3/builder`
* `nuxeo/nexus3/jenkins-x`
* `nuxeo/nexus3/central`
* `nuxeo/nexus3/cluster`
* `nuxeo/nexus3/team`

On `PR-*|feature-*|fix-*` branch pattern, tag is `0.0.0-<branch-name>-<build-number>`

On `master` branch, tag is using `jx-release-version`

The [Jenkins pipeline](Jenkinsfile) is mainly invoking Make steps as described in the [DevPod](#devpod) section.

#### DevPod

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

#### Locally

Dockerfile build arguments and their default value:

* Builder
  * N/A
* Base
  * `NEXUS3_VERSION`=latest
  * `VERSION`=0.1-SNAPSHOT
  * `SCM_REF`=unknown
  * `SCM_REPOSITORY`=git@github.com:nuxeo/docker-nexus3.git
  * `DESCRIPTION`="Base Nexus 3 image layer for Nuxeo custom deployments"
* Nexus3
  * Base arguments are inherited
  * `VERSION`=0.1-SNAPSHOT
  * `DOCKER_REGISTRY`=jenkins-x-docker-registry  
  set `DOCKER_REGISTRY`=localhost:5000 for local build
  * `PARMS`=jenkins-x
  * `DESCRIPTION`="JX default Nexus 3"

See [https://hub.docker.com/r/sonatype/nexus3/tags]() for the latest version.

```bash
export NEXUS3_VERSION=3.19.1
export SCM_REF=`git id`
export VERSION=`git rev-parse --symbolic-full-name --abbrev-ref HEAD`
export DOCKER_REGISTRY=localhost:5000

docker build --build-arg NEXUS3_VERSION --build-arg SCM_REF \
             -t $DOCKER_REGISTRY/nuxeo/nexus3/base:$VERSION base

docker build -t $DOCKER_REGISTRY/nuxeo/nexus3/builder:$VERSION builder

docker build --build-arg DOCKER_REGISTRY --build-arg VERSION --build-arg SCM_REF \
             --build-arg PARMS=jenkins-x -t localhost:5000/nuxeo/nexus3/jenkins-x:$VERSION .
```

The custom configuration files are stored in the `<PARMS>` folders.

Usage:

```bash
export NEXUS3_VERSION=3.19.1
export SCM_REF=`git id`
export VERSION=`git rev-parse --symbolic-full-name --abbrev-ref HEAD`
export DOCKER_REGISTRY=localhost:5000
export PARMS=<PARMS>
export DESCRIPTION=<DESCRIPTION>
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
* `<team>`: Team customization (dedicated Git repository). <https://packages.<team>.dev.nuxeo.com>

### Run

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
       [-v $CONFIG_PATH/license.lic:/nexus-data/etc/licence.lic] \
       --name nexus-$PARMS -itd nuxeo/nexus3/$PARMS:$VERSION

# Alternate method with docker volume (TODO)
docker run --name nexus -p 8081:8081 --mount source=nexus-config,target=/opt/sonatype/nexus/config/ \
       --name nexus-$PARMS -itd nuxeo/nexus3/$PARMS:$VERSION

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

- `<blobstore name>-config.json`: a config file per blobstore (mandatory for S3)

      "config": [
        {
          "bucket": "<blobstore name>",
          "accessKeyId": "****",
          "secretAccessKey": "****",
          "prefix": "storage",
          "region": "<AWS region>",
          "expiration": "3"
        }
      ]
- `[blobstore|repositor|security].json`: optional configuration which overrides image parms

#### `<LICENSE>`: path to the Nexus license file (optional)

#### Data

Data provisioning is performed on start:

- under K8s, the Helm chart executes the [`postStart.sh`](postStart.sh)
- for development, see [builder/scripts/README.md](builder/scripts/README.md)

## Resources

<https://github.com/jenkins-x-charts/nexus>
<https://github.com/sonatype-nexus-community/nexus-scripting-examples>

[Nexus Repository Manager 3 > REST and Integration API > Script API > Examples](https://help.sonatype.com/repomanager3/rest-and-integration-api/script-api/examples)

<https://blog.sonatype.com/deploy-private-docker-registry-on-google-cloud-platform-with-nexus>

## Contributing / Reporting issues

Link to JIRA component (or project if there is no component for that project).
Sample: <https://jira.nuxeo.com/browse/NXP/component/14503/>
Sample: <https://jira.nuxeo.com/secure/CreateIssue!default.jspa?project=NXP>

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

## About Nuxeo

Nuxeo Content Platform is an open source Enterprise Content Management platform, written in Java. Data can be stored in both SQL & NoSQL databases.

The development of the Nuxeo Content Platform is mostly done by Nuxeo employees with an open development model.

The source code, documentation, roadmap, issue tracker, testing, benchmarks are all public.

Typically, Nuxeo users build different types of information management solutions for document management, case management, and digital asset management, use cases. It uses schema-flexible metadata & content models that allows content to be repurposed to fulfill future use cases.

Learn more at [www.nuxeo.com](https://www.nuxeo.com/).
