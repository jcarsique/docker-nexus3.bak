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

# workaround against kaniko insecure registries accesses, run a pod providing
# the patched skaffold version aligned to the jx provided one (v0.29.0)
# to be reworked in NXBT-2910

.PHONY: skaffold@up skaffold@down

VERSION ?= 0.0.0-SNAPSHOT
DOCKER_REGISTRY ?= jenkins-x-docker-registry
SCM_REF ?= unknown
DESCRIPTION ?= empty
NEXUS3_VERSION ?= unknown

export VERSION DOCKER_REGISTRY SCM_REF DESCRIPTION NEXUS3_VERSION

skaffold-pod-name := $(shell hostname)-skaffold
define skaffold_pod_template =
apiVersion: v1
kind: Pod
metadata:
  name: $(skaffold-pod-name)
spec:
  serviceAccountName: jenkins
  containers:
  - name: skaffold
    image: $(DOCKER_REGISTRY)/nuxeo/skaffold
    command: ["/usr/bin/tail"]
    args: [ "-f", "/dev/null" ]
    volumeMounts:
        - name: docker-config
          mountPath: /kaniko/.docker
  volumes:
    - name: docker-config
      configMap:
        name: docker-config
endef
export skaffold_pod_template

define SKAFFOLD =
	skaffold
endef
export SKAFFOLD


skaffold@up:

skaffold@down:

skaffold.yaml~gen: skaffold.yaml
	envsubst '$$NEXUS3_VERSION $$DESCRIPTION $$DOCKER_REGISTRY $$VERSION $$SCM_REF' < skaffold.yaml > skaffold.yaml~gen
