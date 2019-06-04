## SYNOPSIS

Nuxeo Nexus Docker custom image.

Leverages Nexus API to upload and run Groovy scripts for configuration at boot.

## Usage

### Image Parameters

TODO how to customize parameters

Available mount points: `/nexus-store` and `/nexus-data`


### Image Build

    docker build -t nexus3 .


### Image Run

    docker run -p 8081:8081 -v nexus-store:/nexus-store -v nexus-data:/nexus-data \
    -v </path/to/admin_password_file>:/opt/sonatype/nexus/config/password \
    [-v </path/to/nexus_licence_file>:/nexus-data/etc/licence.lic] \
    -itd --name nexus3 nexus3

The `admin_password_file` is mandatory.

The `nexus_licence_file` is optional.

Data provisioning is performed on start:
- under K8s, the Helm chart executes the `postStart.sh`
- for development, see [scripts/README.md]


## Resources

https://github.com/jenkins-x-charts/nexus

https://blog.sonatype.com/deploy-private-docker-registry-on-google-cloud-platform-with-nexus

