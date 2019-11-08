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

Usage: `provision.sh <script_name> <path/to/script/file.groovy>`

### Execute Script

```shell script
curl -v -d <JSON data> -X POST --header "Content-Type: text/plain" -u admin:<admin_password> \
http://localhost:8081/service/rest/v1/script/<script_name>/run
```

#### `blobstore.groovy`

Expected array data:
* `name`: the name for the new BlobStore
* `type`: optional. Accepted values: `file` (default) or `s3`
* `path`: if `type=file`, the path to filesystem BlobStore
* `config`: if `type=s3`, the config of S3 BlobStore

Samples:
```shell script
curl -v -d @path/to/blobstore-parms.json -X POST --header "Content-Type: text/plain" \
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
