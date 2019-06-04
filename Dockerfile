FROM centos:centos7 as builder
RUN yum -y makecache && yum install -y groovy
COPY scripts /scripts
WORKDIR /scripts
RUN groovy addUpdateScriptBody.groovy -f blobstore.groovy -n blobstore> blobstore-body.json
RUN groovy addUpdateScriptBody.groovy -f repository.groovy -n repository > repository-body.json
RUN groovy addUpdateScriptBody.groovy -f security.groovy -n security > security-body.json

FROM sonatype/nexus3:3.15.1 as nexus3

ARG VERSION=unknown
ARG SCM_REF=unknown
ARG SCM_REPOSITORY=git@github.com:nuxeo/jx-docker-images.git
ARG DESCRIPTION="Base Nexus 3 image layer for Nuxeo custom deployments"

LABEL description=${DESCRIPTION}
LABEL version=${VERSION}
LABEL scm-ref=${SCM_REF}
LABEL scm-url=${SCM_REPOSITORY}

VOLUME /nexus-data /nexus-store

USER root

RUN yum -y install epel-release
RUN yum -y install supervisor && \
    chown -R nexus:nexus /nexus-store || true

COPY ./supervisord.conf /etc/supervisord.conf
COPY ./supervisord-boot.conf /etc/supervisord.d/boot.conf
COPY ./supervisord-nexus-config-license.conf /etc/supervisord.d/nexus-config-license.conf
COPY ./supervisord-nexus.conf /etc/supervisord.d/nexus.conf

USER nexus

COPY --from=builder /scripts /opt/sonatype/nexus/scripts/
COPY postStart.sh /opt/sonatype/nexus/

USER root

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]

