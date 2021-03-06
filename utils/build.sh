#!/bin/bash -xe
#
# Manual build of Nexus
#
# (C) Copyright 2019-2020 Nuxeo SA (http://nuxeo.com/) and contributors.
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
#
# Contributors:
#   Julien Carsique
#

usage() {
  echo -ne "Convenient manual build. See also Makefile and Jenkinsfile.\n\
Usage: utils/build.sh <PARMS> <DESCRIPTION>\n"
}

if [ "$#" -eq 0 -o "$#" -lt 1 ]; then
  usage
  exit 1
elif [ "$1" = "help" ]; then
  usage
  exit 0
fi
export PARMS=$1
shift
DESCRIPTION="$*"

export NEXUS3_VERSION=latest
export SCM_REF=$(git show -s --pretty=format:'%h%d')
export VERSION=$(git rev-parse --symbolic-full-name --abbrev-ref HEAD)
CUSTOM_VERSION=${CUSTOM_VERSION:-$VERSION}
export DOCKER_REGISTRY=localhost:5000

echo "Building $CUSTOM_VERSION from Nexus 3 $NEXUS3_VERSION, codebase $VERSION ($(git rev-parse --short HEAD))"
echo "# Base..."
docker build --no-cache --build-arg NEXUS3_VERSION --build-arg SCM_REF --build-arg VERSION -t "$DOCKER_REGISTRY/nuxeo/nexus3/base:$VERSION" base

echo "# Builder..."
docker build --no-cache --build-arg SCM_REF --build-arg VERSION -t "$DOCKER_REGISTRY/nuxeo/nexus3/builder:$VERSION" builder

export DESCRIPTION="$DESCRIPTION $PARMS $CUSTOM_VERSION"
echo "# Nexus..."
docker build --no-cache --build-arg SCM_REF --build-arg VERSION --build-arg DOCKER_REGISTRY --build-arg PARMS --build-arg DESCRIPTION -t "devtools/nexus3/$PARMS:$CUSTOM_VERSION" .
docker tag "devtools/nexus3/$PARMS:$CUSTOM_VERSION" "dockerpriv.nuxeo.com:443/devtools/nexus3/$PARMS:$CUSTOM_VERSION"
echo docker push dockerpriv.nuxeo.com:443/devtools/nexus3/$PARMS:$CUSTOM_VERSION
