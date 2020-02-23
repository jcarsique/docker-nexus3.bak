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

ARG NEXUS3_VERSION=unknown
FROM sonatype/nexus3:${NEXUS3_VERSION}

ARG NEXUS3_VERSION
ARG SCM_REF=unknown
ARG SCM_REPOSITORY=git@github.com:nuxeo/docker-nexus3.git
ARG DESCRIPTION="Base Nexus 3 image layer for Nuxeo custom deployments"
ARG DOCKER_REGISTRY=jenkins-x-docker-registry
ARG VERSION=0.1-SNAPSHOT
ARG VERSION
ARG PARMS=jenkins-x

USER root

RUN yum -y makecache && \
    yum install -y unzip wget python36 python3-pip

RUN pip3 install supervisor

ARG JQ_VERSION='1.5'
RUN wget https://raw.githubusercontent.com/stedolan/jq/master/sig/jq-release.key -O /tmp/jq-release.key && \
    wget https://raw.githubusercontent.com/stedolan/jq/master/sig/v${JQ_VERSION}/jq-linux64.asc -O /tmp/jq-linux64.asc && \
    wget https://github.com/stedolan/jq/releases/download/jq-${JQ_VERSION}/jq-linux64 -O /tmp/jq-linux64 && \
    gpg --import /tmp/jq-release.key && \
    gpg --verify /tmp/jq-linux64.asc /tmp/jq-linux64 && \
    cp /tmp/jq-linux64 /usr/bin/jq && \
    chmod +x /usr/bin/jq && \
    rm -f /tmp/jq-release.key /tmp/jq-linux64.asc /tmp/jq-linux64

ARG GH_PLUGIN_VERSION='2.0.2'
RUN mkdir -p /opt/sonatype/nexus/system/com/larscheidschmitzhermes/ && \
    wget https://github.com/larscheid-schmitzhermes/nexus3-github-oauth-plugin/releases/download/${GH_PLUGIN_VERSION}/nexus3-github-oauth-plugin.zip -O /opt/sonatype/nexus/system/com/larscheidschmitzhermes/nexus3-github-oauth-plugin.zip && \
    unzip /opt/sonatype/nexus/system/com/larscheidschmitzhermes/nexus3-github-oauth-plugin.zip -d /opt/sonatype/nexus/system/com/larscheidschmitzhermes/

COPY ./conf/supervisord.conf /etc/supervisord.conf
COPY ./conf/supervisord-boot.conf /etc/supervisord.d/boot.conf
COPY ./conf/supervisord-nexus-config-license.conf /etc/supervisord.d/nexus-config-license.conf
COPY ./conf/supervisord-nexus-config-license.sh /etc/supervisord.d/nexus-config-license.sh
COPY ./conf/supervisord-nexus-github-plugin.conf /etc/supervisord.d/nexus-github-plugin.conf
COPY ./conf/supervisord-nexus.conf /etc/supervisord.d/nexus.conf
COPY ./conf/githuboauth.properties /opt/sonatype/nexus/etc/githuboauth.properties
COPY ./conf/supervisord-nexus-config-ha.sh /etc/supervisord.d/nexus-config-ha.sh
COPY ./conf/supervisord-nexus-config-ha.conf /etc/supervisord.d/nexus-config-ha.conf
COPY ./conf/supervisord-nexus-disable-wizard.sh /etc/supervisord.d/nexus-disable-wizard.sh
COPY ./conf/supervisord-nexus-disable-wizard.conf /etc/supervisord.d/nexus-disable-wizard.conf
COPY ./conf/supervisord-nexus-vmoptions.sh /etc/supervisord.d/nexus-vmoptions.sh
COPY ./conf/supervisord-nexus-vmoptions.conf /etc/supervisord.d/nexus-vmoptions.conf

LABEL description=${DESCRIPTION}
LABEL version=${VERSION}
LABEL scm-ref=${SCM_REF}
LABEL scm-url=${SCM_REPOSITORY}
LABEL com.sonatype.version=${NEXUS3_VERSION}
LABEL parms=${PARMS}

COPY postStart.sh /opt/sonatype/nexus/
COPY ./scripts /opt/sonatype/nexus/scripts/
COPY parms/${PARMS}/*.json /opt/sonatype/nexus/scripts/

RUN chown -R nexus:nexus /opt/sonatype

VOLUME /nexus-data /nexus-store

CMD ["/usr/local/bin/supervisord", "-c", "/etc/supervisord.conf"]
