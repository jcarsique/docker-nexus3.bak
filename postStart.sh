#!/bin/bash
# Forked from https://github.com/jenkins-x-charts/nexus/blob/v0.1.20/postStart.sh

set -e
HOST=localhost:8081

printf 'Waiting for server start...'
until $(curl --output /dev/null --silent --head --fail http://$HOST/); do
  printf '.'
  sleep 1
done
echo .
grep "Started Sonatype Nexus" /nexus-data/log/nexus.log || true
echo
grep -e ERROR -e WARN /nexus-data/log/nexus.log|head || true
echo

echo "Configuring..."
USERNAME="admin"
SCRIPTS_PATH="/opt/sonatype/nexus/scripts"
CONFIG_PATH="/opt/sonatype/nexus/config"
PASSWORD="admin123"
if [ -f "/nexus-data/admin.password" ]; then
  echo "Read generated admin password"
  PASSWORD="$(cat /nexus-data/admin.password)"
fi
PASSWORD_FROM_FILE="$(cat $CONFIG_PATH/password || true)"
declare -a SCRIPT_LIST=

function die() {
    echo "ERROR: $@" 1>&2
    exit 1
}

function createOrUpdateAndRun() {
    local scriptName=$1
    local scriptFile=$2
    local scriptParms=$3

    if [ "${#SCRIPT_LIST[@]}" = 0 ] || [[ ! " ${SCRIPT_LIST[@]} " =~ " ${scriptName} " ]]; then
        echo "Creating $scriptName script"
        curl --fail -sS -X POST -u $USERNAME:$PASSWORD --header "Content-Type: application/json" "http://$HOST/service/rest/v1/script/" -d @$scriptFile
    else
        echo "Updating $scriptName script"
        curl --fail -sS -X PUT -u $USERNAME:$PASSWORD --header "Content-Type: application/json" "http://$HOST/service/rest/v1/script/$scriptName" -d @$scriptFile
    fi
    echo "Running $scriptName script"
    if [ -z "${scriptParms}" ]; then
      curl --fail -sS -X POST -u $USERNAME:$PASSWORD --header "Content-Type: application/json" "http://$HOST/service/rest/v1/script/$scriptName/run" \
          |sed 's,\\",",g; s,"{,{,g; s,}",},g'|jq .
    else
      curl --fail -sS -X POST -u $USERNAME:$PASSWORD --header "Content-Type: application/json" "http://$HOST/service/rest/v1/script/$scriptName/run" -d @$scriptParms \
          |sed 's,\\",",g; s,"{,{,g; s,}",},g'|jq .
    fi
    echo
}

function setScriptList() {
    # initialising the scripts already present once and assuming that there no duplicate script names in the scripts that follow
    SCRIPT_LIST=($(curl --fail -s -u $USERNAME:$PASSWORD http://$HOST/service/rest/v1/script | grep -oE "\"name\" : \"[^\"]+" | sed 's/"name" : "//'))
}

function testLogin() {
    curl --fail --silent -u $USERNAME:$1 http://$HOST/service/metrics/ping >/dev/null 2>&1
}

function healthcheck() {
    echo "HealthCheck:"
    curl -v -sS -u $USERNAME:$PASSWORD http://$HOST/service/metrics/healthcheck |jq .
}

# Need password upgrade from file?
if test -n "$PASSWORD_FROM_FILE" && ! testLogin "$PASSWORD_FROM_FILE"; then
    echo "Setting password from file"
    createOrUpdateAndRun set_admin_password "$SCRIPTS_PATH/set_admin_password-body.json"
fi
PASSWORD=${PASSWORD_FROM_FILE:-$PASSWORD}
testLogin "$PASSWORD" || die "Login fails."
setScriptList

for script in blobstore repository security task_timeout; do
    body="$SCRIPTS_PATH/${script}-body.json"
    if [ -f "$CONFIG_PATH/${script}.json" ] ; then
        parms="$CONFIG_PATH/${script}.json"
    else
        parms="$SCRIPTS_PATH/${script}-parms.json"
    fi
    createOrUpdateAndRun ${script} ${body} ${parms}
done
echo "Configuration done."

healthcheck
grep ERROR -B1 /nexus-data/log/nexus.log|head || true
