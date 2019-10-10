#!/bin/bash
# Forked from https://github.com/jenkins-x-charts/nexus/blob/master/postStart.sh

set -ex
HOST=localhost:8081

until $(curl --output /dev/null --silent --head --fail http://$HOST/); do
  printf '.'
  sleep 5
done

#chgrp -R 0 /nexus-data
#chmod -R g+rw /nexus-data
#find /nexus-data -type d -exec chmod g+x {} +

USERNAME=admin
PASSWORD="$(cat /nexus-data/admin.password || true)"
PASSWORD_FROM_FILE="$(cat /opt/sonatype/nexus/config/password || true)"
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
        echo "Creating $scriptName repository script"
        curl --fail -X POST -u $USERNAME:$PASSWORD --header "Content-Type: application/json" "http://$HOST/service/rest/v1/script/" -d @$scriptFile
    else
        echo "Updating $scriptName repository script"
        curl --fail -X PUT -u $USERNAME:$PASSWORD --header "Content-Type: application/json" "http://$HOST/service/rest/v1/script/$scriptName" -d @$scriptFile
    fi
    echo "Running $scriptName repository script"
    if [ -z "${scriptParms}" ]; then
      curl --fail -X POST -u $USERNAME:$PASSWORD --header "Content-Type: text/plain" "http://$HOST/service/rest/v1/script/$scriptName/run"
    else
      curl --fail -X POST -u $USERNAME:$PASSWORD --header "Content-Type: text/plain" "http://$HOST/service/rest/v1/script/$scriptName/run" -d @$scriptParms
    fi
    echo
}

function setScriptList() {
    # initialising the scripts already present once and assuming that there no duplicate script names in the scripts that follow
    SCRIPT_LIST=($(curl --fail -s -u $USERNAME:$PASSWORD http://$HOST/service/rest/v1/script | grep -oE "\"name\" : \"[^\"]+" | sed 's/"name" : "//'))
}

function setPasswordFromFile() {
    if [ -n "${PASSWORD_FROM_FILE}" ]; then
        echo "Updating PASSWORD variable from password file."
        PASSWORD="${PASSWORD_FROM_FILE}"
    else
        echo "Not updating PASSWORD var. Password file either non-existent or not readable."
    fi
}

if test -n "$PASSWORD_FROM_FILE" && curl --fail --silent -u $USERNAME:$PASSWORD_FROM_FILE http://$HOST/service/metrics/ping; then
    echo "Loging to nexues suceeded. kubernetes provided password worked."
    setPasswordFromFile
    setScriptList
elif curl --fail --silent -u $USERNAME:$PASSWORD http://$HOST/service/metrics/ping; then
    echo "Login to nexus succeeded. Default password worked. Updating password if available..."
    setScriptList
    if test -n "$PASSWORD_FROM_FILE"; then
	  createOrUpdateAndRun set_admin_password /opt/sonatype/nexus/scripts/set_admin_password-body.json
	  setPasswordFromFile
      setScriptList
    fi
else
    die "Login to nexus failed. Tried the default password only since no password secret file was provided."
fi

# if not explicitly enabled, then Helm chart switches to disabled
if [ -z "${ENABLE_ANONYMOUS_ACCESS}" ]; then
    secFile="/opt/sonatype/nexus/scripts/security-parms.json"
    jq '.[] | select (.type == "anonymous") .enabled = "false"' ${secFile} > ${secFile}.tmp && \
    mv ${secFile}.tmp ${secFile}
fi

for script in blobstore repository security; do
    createOrUpdateAndRun ${script} /opt/sonatype/nexus/scripts/${script}-body.json /opt/sonatype/nexus/scripts/${script}-parms.json
done
