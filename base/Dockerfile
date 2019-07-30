ARG NEXUS3_VERSION=latest
FROM sonatype/nexus3:${NEXUS3_VERSION} as base

ARG NEXUS3_VERSION=latest
ARG VERSION=0.1-SNAPSHOT
ARG SCM_REF=unknown
ARG SCM_REPOSITORY=git@github.com:nuxeo/docker-nexus3.git
ARG DESCRIPTION="Base Nexus 3 image layer for Nuxeo custom deployments"

LABEL description=${DESCRIPTION}
LABEL version=${VERSION}
LABEL scm-ref=${SCM_REF}
LABEL scm-url=${SCM_REPOSITORY}
LABEL com.sonatype.version=${NEXUS3_VERSION}

USER root

RUN yum -y makecache && yum -y install epel-release && yum -y install supervisor && \
    yum install -y unzip wget

COPY ./supervisord.conf /etc/supervisord.conf
COPY ./supervisord-boot.conf /etc/supervisord.d/boot.conf
COPY ./supervisord-nexus-config-license.conf /etc/supervisord.d/nexus-config-license.conf
COPY ./supervisord-nexus-github-plugin.conf /etc/supervisord.d/nexus-github-plugin.conf
COPY ./supervisord-nexus.conf /etc/supervisord.d/nexus.conf
COPY ./githuboauth.properties /opt/sonatype/nexus/etc/githuboauth.properties

RUN chown -R nexus:nexus /opt/sonatype

VOLUME /nexus-data /nexus-store

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
