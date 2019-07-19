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

ARG DOCKER_REGISTRY=jenkins-x-docker-registry
ARG VERSION=0.1-SNAPSHOT
FROM ${DOCKER_REGISTRY}/nuxeo/nexus3/builder:${VERSION} as builder

ARG DOCKER_REGISTRY=jenkins-x-docker-registry
ARG VERSION=0.1-SNAPSHOT
FROM ${DOCKER_REGISTRY}/nuxeo/nexus3/base:${VERSION} as base

# Custom Nexus
FROM base
ARG DOCKER_REGISTRY=jenkins-x-docker-registry
ARG VERSION=0.1-SNAPSHOT
ARG PARMS=jenkins
ARG DESCRIPTION="JX default Nexus 3"
ARG SCM_REF=unknown

LABEL description=${DESCRIPTION}
LABEL version=${VERSION}
LABEL scm-ref=${SCM_REF}

COPY postStart.sh /opt/sonatype/nexus/
COPY --from=builder /scripts /opt/sonatype/nexus/scripts/
COPY parms/${PARMS}/*.json /opt/sonatype/nexus/scripts/
