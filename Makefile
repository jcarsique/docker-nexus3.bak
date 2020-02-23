# (C) Copyright 2019 Nuxeo (http://nuxeo.com/) and others.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

..NOTPARALLEL: jenkins-x central cluster team maven-ncp

SCM_REF ?= $(shell git show -s --pretty=format:'%h%d')
VERSION ?= $(shell git rev-parse --symbolic-full-name --abbrev-ref HEAD)

NEXUS3_VERSION = 3.20.1

include make.d/skaffold.mk

.PHONY: all build central

all: skaffold@up build skaffold@down

build: jenkins-x central cluster team maven-ncp

jenkins-x: skaffold.yaml~gen
	$(SKAFFOLD) build -f skaffold.yaml~gen -b nuxeo/nexus3/jenkins-x

central: DESCRIPTION="packages.nuxeo.com central $VERSION"
central: skaffold.yaml~gen
	$(SKAFFOLD) build -f skaffold.yaml~gen -b nuxeo/nexus3/central

cluster: DESCRIPTION="Cluster $VERSION"
cluster: skaffold.yaml~gen
	$(SKAFFOLD) build -f skaffold.yaml~gen -b nuxeo/nexus3/cluster

team: DESCRIPTION="Team (generic) $VERSION"
team: skaffold.yaml~gen
	$(SKAFFOLD) build -f skaffold.yaml~gen -b nuxeo/nexus3/team

maven-ncp: DESCRIPTION="NCP $VERSION"
maven-ncp: skaffold.yaml~gen
	$(SKAFFOLD) build -f skaffold.yaml~gen -b nuxeo/nexus3/maven-ncp
