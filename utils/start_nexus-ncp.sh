#!/bin/bash -e
#
# Manual start for Nexus NCP.
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

CONFIG_PATH=${CONFIG_PATH:-/root/nexus-config/}
IMAGE=devtools/nexus3/maven-ncp
VERSION=${VERSION:-0.0.8}
NAME=nexus
docker run --restart=always -p 8081:8081 --name $NAME -v $CONFIG_PATH/:/opt/sonatype/nexus/config/ \
    -v $CONFIG_PATH/license.lic:/nexus-data/etc/license/.license.lic \
    --ulimit nofile=65536:65536 -e INSTALL4J_ADD_VM_PARAMS="-Xms4G -Xmx4G -XX:MaxDirectMemorySize=2G" \
    -itd dockerpriv.nuxeo.com/$IMAGE:$VERSION

# curl --output /dev/null --silent --head --fail localhost:8081/service/rest/v1/status

echo "Running post start..."
docker exec -u nexus $NAME /opt/sonatype/nexus/postStart.sh 2>&1 |tee postStart.log
clear
cat postStart.log
docker logs $NAME |grep ERROR
