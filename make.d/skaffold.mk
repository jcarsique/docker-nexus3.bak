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
endef
export skaffold_pod_template

define SKAFFOLD =
	tar cf - . | kubectl exec -i $(skaffold-pod-name) tar xf -
	kubectl exec $(skaffold-pod-name) -- env VERSION=$(VERSION) DOCKER_REGISTRY=$(DOCKER_REGISTRY) skaffold
endef
export SKAFFOLD


skaffold@up:
	@echo "$$skaffold_pod_template" | kubectl apply -f -
	@kubectl wait --timeout=-1s --for=condition=Ready pod/$(skaffold-pod-name)

skaffold@down:
	kubectl delete pod/$(skaffold-pod-name)

skaffold.yaml~gen: skaffold.yaml
	VERSION=$(VERSION) DOCKER_REGISTRY=$(DOCKER_REGISTRY) envsubst '$$DOCKER_REGISTRY $$VERSION' < skaffold.yaml > skaffold.yaml~gen
