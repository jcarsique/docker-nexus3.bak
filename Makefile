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

..NOTPARALLEL: base builder jenkins central

include skaffold.mk

.PHONY: all build base builder jenkins central

VERSION ?= 0.0.0
DOCKER_REGISTRY ?= gcr.io/jx-preprod/nxmatic/jx

release: base builder jenkins central cluster

promote: changelog update-environment-nxmatic-dev

changelog:
	jx step changelog --version v$(VERSION)

update-environment-nxmatic-dev:
	jx step create pr regex --regex "^(?m)\s+repository: \"$(subst /,\/,$(DOCKER_REGISTRY))\/nuxeo\/nexus3\/cluster\"\s+tag: \"(.*)\"$"" --version $(VERSION) --files env/jenkins-x-platform/values.tmpl.yaml --repo=https://github.com/pfouh/environment-nxmatic-dev.git
	
base:
	$(MAKE) -I../shared-make.d --directory base build

builder: skaffold.yaml~gen
	$(MAKE) -I../shared-make.d --directory builder build

jenkins: skaffold.yaml~gen
	$(call SKAFFOLD, build -f skaffold.yaml~gen -b nuxeo/nexus3/jenkins)

central: skaffold.yaml~gen
	$(call SKAFFOLD, build -f skaffold.yaml~gen -b nuxeo/nexus3/central)

cluster: skaffold.yaml~gen
	$(call SKAFFOLD, build -f skaffold.yaml~gen -b nuxeo/nexus3/cluster)

