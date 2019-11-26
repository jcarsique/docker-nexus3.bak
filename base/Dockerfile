ARG NEXUS3_VERSION=latest
FROM sonatype/nexus3:${NEXUS3_VERSION} as base

ARG NEXUS3_VERSION=latest
ARG VERSION=0.1-SNAPSHOT
ARG SCM_REF=unknown
ARG SCM_REPOSITORY=git@github.com:nuxeo/docker-nexus3.git
ARG DESCRIPTION="Base Nexus 3 image layer for Nuxeo custom deployments"

USER root

RUN yum -y makecache && \
    yum install -y unzip wget python36 python3-pip

RUN pip3 install supervisor

ENV JQ_VERSION='1.5'
RUN wget --no-check-certificate https://raw.githubusercontent.com/stedolan/jq/master/sig/jq-release.key -O /tmp/jq-release.key && \
    wget --no-check-certificate https://raw.githubusercontent.com/stedolan/jq/master/sig/v${JQ_VERSION}/jq-linux64.asc -O /tmp/jq-linux64.asc && \
    wget --no-check-certificate https://github.com/stedolan/jq/releases/download/jq-${JQ_VERSION}/jq-linux64 -O /tmp/jq-linux64 && \
    gpg --import /tmp/jq-release.key && \
    gpg --verify /tmp/jq-linux64.asc /tmp/jq-linux64 && \
    cp /tmp/jq-linux64 /usr/bin/jq && \
    chmod +x /usr/bin/jq && \
    rm -f /tmp/jq-release.key && \
    rm -f /tmp/jq-linux64.asc && \
    rm -f /tmp/jq-linux64

COPY ./supervisord.conf /etc/supervisord.conf
COPY ./supervisord-boot.conf /etc/supervisord.d/boot.conf
COPY ./supervisord-nexus-config-license.conf /etc/supervisord.d/nexus-config-license.conf
COPY ./supervisord-nexus-config-license.sh /etc/supervisord.d/nexus-config-license.sh
COPY ./supervisord-nexus-github-plugin.conf /etc/supervisord.d/nexus-github-plugin.conf
COPY ./supervisord-nexus.conf /etc/supervisord.d/nexus.conf
COPY ./githuboauth.properties /opt/sonatype/nexus/etc/githuboauth.properties

LABEL description=${DESCRIPTION}
LABEL version=${VERSION}
LABEL scm-ref=${SCM_REF}
LABEL scm-url=${SCM_REPOSITORY}
LABEL com.sonatype.version=${NEXUS3_VERSION}

RUN chown -R nexus:nexus /opt/sonatype

VOLUME /nexus-data /nexus-store

CMD ["/usr/local/bin/supervisord", "-c", "/etc/supervisord.conf"]
