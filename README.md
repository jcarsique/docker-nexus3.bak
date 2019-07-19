# Nexus 3

## SYNOPSIS

Nuxeo Nexus Docker custom image.

Leverages Nexus API to upload and run Groovy scripts for configuration at boot.

## Usage

### Image Build

    export PARMS=<PARMS> SCM_REF="$(git id)" VERSION="..." DESCRIPTION="..."
    docker build --build-arg PARMS --build-arg SCM_REF --build-arg VERSION --build-arg DESCRIPTION -t nexus3 .

- `<PARMS>`: custom parameters folder name

      .
      ├── Dockerfile
      ├── parms
      │   ├── central
      │   ├── cluster
      │   ├── jenkins
      │   └── <team>..

  - `jenkins`: Jenkins default embedded Nexus. See [parms/jenkins](parms/jenkins)
  - `central`: AWS packages.nuxeo.com
  - `team`: Team namespace level.
[packages-\<team\>.prod.dev.nuxeo.com](packages-\<team\>.prod.dev.nuxeo.com) or
[packages-\<team\>.preprod.dev.nuxeo.com](packages-\<team\>.preprod.dev.nuxeo.com)
  - `cluster`: Google cluster level.
packages.prod.dev.nuxeo.com or packages.preprod.dev.nuxeo.com

Sample usage:

    docker build -t nexus3:jenkins .

    export PARMS=central SCM_REF="$(git id)" VERSION="0.1" DESCRIPTION="README sample with 'central'"
    docker build --build-arg PARMS=$PARMS --build-arg SCM_REF=$SCM_REF --build-arg VERSION=$VERSION --build-arg               DESCRIPTION=$DESCRIPTION -t nexus3:$PARMS .

### Image Run

    docker run -p 8081:8081 -v <CONFIG>:/opt/sonatype/nexus/config/ \
    [-v nexus-store:/nexus-store] [-v nexus-data:/nexus-data] [-v <LICENSE>:/nexus-data/etc/licence.lic] -itd nexus3

`<CONFIG>`: configuration folder containing
- `password`: the admin credentials file (mandatory)
- `passwords.json`: the users credentials file (optional)

    ```json
    {
        "user1" : "password1",
        "user2" : "password2"
    }
    ```
- `<blobstore name>-config.json`: a config file per blobstore (mandatory for S3)

    ```json
    "config": [
      {
        "bucket": "<blobstore name>",
        "accessKeyId": "****",
        "secretAccessKey": "****",
        "prefix": "storage",
        "region": "<AWS region>",
        "expiration": "3"
      }
    ]
    ```
`<LICENSE>`: path to the Nexus license file (optional)

Available mount points: `/nexus-store` and `/nexus-data`

Data provisioning is performed on start:
- under K8s, the Helm chart executes the `postStart.sh`
- for development, see [scripts/README.md]


## Resources

https://github.com/jenkins-x-charts/nexus
https://github.com/sonatype-nexus-community/nexus-scripting-examples

[Nexus Repository Manager 3 > REST and Integration API > Script API > Examples](https://help.sonatype.com/repomanager3/rest-and-integration-api/script-api/examples)

https://blog.sonatype.com/deploy-private-docker-registry-on-google-cloud-platform-with-nexus
