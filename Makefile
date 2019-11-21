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

..NOTPARALLEL: base builder jenkins-x central cluster team

include make.d/skaffold.mk

.PHONY: all build base builder jenkins central

VERSION ?= 0.0.0

all: skaffold@up build skaffold@down

build: base builder jenkins central cluster team maven-ncp

base:
	$(MAKE) --directory base build

builder: skaffold.yaml~gen
	$(MAKE) --directory builder build

jenkins-x: skaffold.yaml~gen
	$(SKAFFOLD) build -f skaffold.yaml~gen -b nuxeo/nexus3/jenkins-x

central: skaffold.yaml~gen
	$(SKAFFOLD) build -f skaffold.yaml~gen -b nuxeo/nexus3/central

cluster: skaffold.yaml~gen
	$(SKAFFOLD) build -f skaffold.yaml~gen -b nuxeo/nexus3/cluster

team: skaffold.yaml~gen
	$(SKAFFOLD) build -f skaffold.yaml~gen -b nuxeo/nexus3/team

maven-ncp: skaffold.yaml~gen
	$(SKAFFOLD) build -f skaffold.yaml~gen -b nuxeo/nexus3/maven-ncp
