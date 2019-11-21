#!/bin/bash
# Forked from https://github.com/sonatype-nexus-community/nexus-scripting-examples

# A simple example script that publishes a number of scripts to the Nexus Repository Manager

# fail if anything errors
set -e
# fail if a function call is missing an argument
set -u

username=${NX_USERNAME:-admin}
password=${NX_PASSWORD:-admin123}
HERE=$(cd $(dirname $0); pwd -P)
name=$1
file=$2

# add the context if you are not using the root context
host=http://localhost:8081

# add a script to the repository manager and run it
function addScript {
  # using grape config that points to local Maven repo and Central Repository , default grape config fails on some downloads although artifacts are in Central
  # change the grapeConfig file to point to your repository manager, if you are already running one in your organization
  CMD='groovy -Dgroovy.grape.report.downloads=true -Dgrape.config=grapeConfig.xml $HERE/addUpdateScript.groovy -u "$username" -p "$password" -n "$name" -f "$file" -h "$host"'
  echo $CMD
  eval $CMD
  #echo "groovy -Dgroovy.grape.report.downloads=true -Dgrape.config=grapeConfig.xml $HERE/addUpdateScript.groovy -u \"$username\" -p \"$password\" -n \"$name\" -f \"$file\" -h \"$host\""
  #groovy -Dgroovy.grape.report.downloads=true -Dgrape.config=grapeConfig.xml $HERE/addUpdateScript.groovy -u "$username" -p "$password" -n "$name" -f "$file" -h "$host"
  printf "\nPublished $file as $name\n\n"
}

printf "Publishing on $host\n"

addScript

echo "Run the script with:"
echo "curl -v -d@path/to/data.json -X POST --header 'Content-Type: text/plain' -u $username:$password  http://localhost:8081/service/rest/v1/script/$name/run"
