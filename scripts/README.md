## SYNOPSYS

Scripts to configure Nexus non-interactively once the service is running

## REQUIREMENT

Have Groovy installed

## USAGE

#### Upload script into nexus through its API:

Call provision.sh with 2 parameters : first parameter = remote_script_name / second parameter = local_script_to_upload
ie : `./provision.sh blobstore blobstore.groovy`

#### Exec script through API

The `blobstore.groovy` takes an array of String : `name` / `file` / `path`

    curl -v -d '[{"name":"<blob_name>", "type":"<file / S3>", "path":"<blob_path>"}]' -X POST \
    -u <admin_name>:<admin_password> --header "Content-Type: text/plain" \
    "http://localhost:8081/service/rest/v1/script/<script_name>/run"

It can also be invoked with a JSON file as input:

    curl -v -d @blobstore-parms.json -X POST \
    -u <admin_name>:<admin_password> --header "Content-Type: text/plain" \
    "http://localhost:8081/service/rest/v1/script/<script_name>/run"

## USEFUL COMMANDS

Delete remote script:

`curl -X DELETE "http://localhost:8081/service/rest/v1/script/blobstore" -H "accept: application/json"`

List remote scripts:

`curl -X GET "http://localhost:8081/service/rest/v1/script" -H  "accept:application/json"`
