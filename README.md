# Nexus 3

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
    └── team              Jenkinx X default for team
```

## Usage

### QA

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
* `nuxeo/nexus3/jenkins`
* `nuxeo/nexus3/central`

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

Dockerfile build arguments and default values:

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
  * `PARMS`=jenkins
  * `DESCRIPTION`="JX default Nexus 3"

```bash
# See https://hub.docker.com/r/sonatype/nexus3/tags
docker build --build-arg NEXUS3_VERSION=3.19.1 \
             --build-arg SCM_REF="$(git id)" \
             -t localhost:5000/nuxeo/nexus3/base base

docker build -t localhost:5000/nuxeo/nexus3/builder builder

docker build --build-arg DOCKER_REGISTRY=localhost:5000 \
             --build-arg VERSION=latest \
             --build-arg SCM_REF="$(git id)" \
             -t localhost:5000/nuxeo/nexus3/jenkins .
```

##### Custom Image Build

A custom image must be built for each deployment. The configuration files (CasC) are stored in the `<PARMS>` folders.

Usage:

```bash
docker build [--build-arg DOCKER_REGISTRY=localhost:5000] \
             --build-arg VERSION=<VERSION> \
             [--build-arg PARMS=<PARMS>] \
             [--build-arg SCM_REF="$(git id)"] \
             [--build-arg DESCRIPTION="<DESCRIPTION>"] \
             [-t localhost:5000/nexus3/<PARMS>[:<VERSION>]] .

# Sample with 'central'
docker build --build-arg DOCKER_REGISTRY=localhost:5000 \
             --build-arg VERSION=latest \
             --build-arg PARMS=central \
             --build-arg SCM_REF="$(git id)" \
             --build-arg DESCRIPTION="README sample with 'central' parameter to build packages.nuxeo.com" \
             -t nuxeo/nexus3/central .
```

###### `<PARMS>`

The custom parameters folder name:

* [`central`](parms/central): Nuxeo Central Repository <https://packages.nuxeo.com/>
* [`cluster`](parms/cluster): Cluster Repository <https://packages.dev.nuxeo.com/>
* `team`: Team Repositories [https://packages-\<team\>.dev.nuxeo.com](https://packages-\<team\>.dev.nuxeo.com)
* [`jenkins`](parms/jenkins): Jenkins X default Nexus 3. Not used.

###### Custom Image Run

Configuration Data is provided at instance start.

Usage:

```bash
docker run -p 8081:8081 -v <CONFIG>:/opt/sonatype/nexus/config/ \
       [-v nexus-store:/nexus-store] \
       [-v nexus-data:/nexus-data] \
       [-v <LICENSE>:/nexus-data/etc/licence.lic] \
       -itd localhost:5000/nexus3/<PARMS>
```

###### `<CONFIG>`: configuration folder containing

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
- `[blobstore|repositor|security].json`: optional configuration which override image parms

###### `<LICENSE>`: path to the Nexus license file (optional)

Available mount points: `/nexus-store` and `/nexus-data`

Data provisioning is performed on start:

- under K8s, the Helm chart executes the `postStart.sh`
- for development, see [builder/scripts/README.md]

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

Learn more at [www.nuxeo.com](www.nuxeo.com).
