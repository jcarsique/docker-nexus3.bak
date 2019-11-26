# Nexus 3 Builder

## Synopsis

Scripts to configure Nexus non-interactively once the service is running

## Requirements

- Groovy
- Provisioned resources
```shell script
docker volume create nexus-config

#pass show nuxeo/vaultbolt-devtools/code/ci-casc/tf/nexus/prod/password > password
#pass show nuxeo/vaultbolt-devtools/code/ci-casc/tf/nexus/prod/passwords.json > passwords.json
#pass show nuxeo/vaultbolt-devtools/code/ci-casc/tf/nexus/prod/license_nexus.lic > .license.lic
#pass show nuxeo/vaultbolt-devtools/code/ci-casc/tf/nexus/prod/datadog_key > .datadog_key
#pass show nuxeo/vaultbolt-devtools/code/ci-casc/tf/nexus/prod/cluster-agent-token > .cluster-agent-token
```

```text
config
├── password
└── passwords.json
```

## Development

### Startup

```shell script
docker run -itd --name nexus -p 8081:8081 --mount source=nexus-config,target=/opt/sonatype/nexus/config/ <IMAGE>

docker exec nexus cat /nexus-data/admin.password
docker exec -u nexus nexus /opt/sonatype/nexus/postStart.sh
xdg-open http://localhost:8081
```

### Upload Script into Nexus

Optional environment variables: `NX_USERNAME` and `NX_PASSWORD`

Usage: `provision.sh <script_name> <path/to/script/file.groovy>`

Sample:

```shell script
export CONFIG_PATH=<CONFIG>
NX_USERNAME=admin NX_PASSWORD=$(cat $CONFIG_PATH/password) ./provision.sh repository repository.groovy

curl -v -X POST --header 'Content-Type: text/plain' -u admin:$(cat /tmp/nexus-ncp/config/password) http://localhost:8081/service/rest/v1/script/repository/run -d@../../parms/maven-ncp/repository-parms.json
```

### Execute Script

```shell script
# Provide JSON data file
curl -v --fail -X POST -u $NX_USERNAME:$NX_PASSWORD --header "Content-Type: text/plain" http://localhost:8081/service/rest/v1/script/<script_name>/run -d @<JSON path>

# Inline JSON data
curl -v --fail -X POST -u $NX_USERNAME:$NX_PASSWORD --header "Content-Type: text/plain" http://localhost:8081/service/rest/v1/script/<script_name>/run -d <JSON data>
```

#### `blobstore.groovy`

Expected array data:
* `name`: the name for the new BlobStore
* `type`: optional. Accepted values: `file` (default) or `s3`
* `path`: if `type=file`, the path to filesystem BlobStore
* `config`: if `type=s3`, the config of S3 BlobStore

Samples:
```shell script
curl -v -d@path/to/blobstore-parms.json -X POST --header "Content-Type: text/plain" \
-u admin:password  "http://localhost:8081/service/rest/v1/script/<script_name>/run"

curl -v -d [{"name":"aLocalBlobstore", "type":"file", "path":"/var/lib/nexus/blobstore"}] \
-X POST --header "Content-Type: text/plain" \
-u admin:password  "http://localhost:8081/service/rest/v1/script/<script_name>/run"
```

#### `TODO.groovy`



### Other Sample Commands

#### Delete remote script

`curl -X DELETE "http://localhost:8081/service/rest/v1/script/<script_name>" -H "accept: application/json"`

#### List remote scripts

`curl -X GET "http://localhost:8081/service/rest/v1/script" -H  "accept:application/json"`

### IDE Debug

Import the project and https://github.com/sonatype/nexus-public in your IDE.

```shell script
docker run -p 5005:5005 -e INSTALL4J_ADD_VM_PARAMS="-Xms1200m -Xmx1200m -XX:MaxDirectMemorySize=2g -Djava.util.prefs.userRoot=/nexus-data/javaprefs -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005" \
           -p 8081:8081 -v $CONFIG_PATH/:/opt/sonatype/nexus/config/ -v $CONFIG_PATH/license.lic:/nexus-data/etc/licence.lic --name nexus -itd <IMAGE>
```