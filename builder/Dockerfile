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

# JSON early builder (see addUpdateScriptBody.groovy)
FROM centos:centos7 as builder
RUN yum -y makecache && yum install -y groovy

ARG VERSION=0.1-SNAPSHOT
ARG SCM_REF=unknown
ARG SCM_REPOSITORY=git@github.com:nuxeo/docker-nexus3.git

LABEL version=${VERSION}
LABEL scm-ref=${SCM_REF}
LABEL scm-url=${SCM_REPOSITORY}

COPY scripts /scripts
WORKDIR /scripts
RUN groovy addUpdateScriptBody.groovy -f blobstore.groovy -n blobstore> blobstore-body.json
RUN groovy addUpdateScriptBody.groovy -f repository.groovy -n repository > repository-body.json
RUN groovy addUpdateScriptBody.groovy -f security.groovy -n security > security-body.json
RUN groovy addUpdateScriptBody.groovy -f tasks.groovy -n tasks > tasks-body.json
RUN groovy addUpdateScriptBody.groovy -f task_timeout.groovy -n task_timeout > task_timeout-body.json
RUN groovy addUpdateScriptBody.groovy -f set_admin_password.groovy -n set_admin_password > set_admin_password-body.json
