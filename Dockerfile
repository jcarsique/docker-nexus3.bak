FROM centos:centos7 as builder
RUN yum -y makecache && yum install -y groovy
COPY scripts /scripts
WORKDIR /scripts
RUN groovy addUpdateScriptBody.groovy -f blobstore.groovy -n blobstore> blobstore-body.json
RUN groovy addUpdateScriptBody.groovy -f repository.groovy -n repository > repository-body.json
RUN groovy addUpdateScriptBody.groovy -f security.groovy -n security > security-body.json

FROM sonatype/nexus3:3.15.1 as base

ARG VERSION=unknown
ARG SCM_REF=unknown
ARG SCM_REPOSITORY=git@github.com:nuxeo/jx-docker-images.git
ARG DESCRIPTION="Base Nexus 3 image layer for Nuxeo custom deployments"

LABEL description=${DESCRIPTION}
LABEL version=${VERSION}
LABEL scm-ref=${SCM_REF}
LABEL scm-url=${SCM_REPOSITORY}

USER root

RUN yum -y makecache && yum -y install epel-release && yum -y install supervisor

COPY ./supervisord.conf /etc/supervisord.conf
COPY ./supervisord-boot.conf /etc/supervisord.d/boot.conf
COPY ./supervisord-nexus-config-license.conf /etc/supervisord.d/nexus-config-license.conf
COPY ./supervisord-nexus.conf /etc/supervisord.d/nexus.conf

USER nexus

COPY --from=builder /scripts /opt/sonatype/nexus/scripts/
COPY postStart.sh /opt/sonatype/nexus/

USER root

VOLUME /nexus-data /nexus-store

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]

# Jenkins default embedded Nexus
FROM base as jenkins
COPY parms/jenkins/*.json /scripts

# GCP packages-<team>.prod.dev.nuxeo.com & packages-<team>.preprod.dev.nuxeo.com
FROM base as team
COPY parms/team/*.json /scripts

# GCP packages.prod.dev.nuxeo.com & packages.preprod.dev.nuxeo.com
FROM base as cluster
COPY parms/cluster/*.json /scripts

# AWS packages.nuxeo.com
FROM base as central
COPY parms/central/*.json /scripts
