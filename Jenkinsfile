/*
 * (C) Copyright 2019 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

properties([
  [$class: 'GithubProjectProperty', projectUrlStr: 'https://github.com/nuxeo/docker-nexus3']
])

void setGitHubBuildStatus(String context, String message, String state) {
  step([
    $class: 'GitHubCommitStatusSetter',
    reposSource: [$class: 'ManuallyEnteredRepositorySource', url: 'https://github.com/nuxeo/docker-nexus3'],
    contextSource: [$class: 'ManuallyEnteredCommitContextSource', context: context],
    statusResultSource: [$class: 'ConditionalStatusResultSource', results: [[$class: 'AnyBuildResult', message: message, state: state]]],
  ])
}

void getReleaseVersion() {
  return sh(returnStdout: true, script: 'jx-release-version')
}

pipeline {
  agent {
    label "jenkins-jx-base"
  }
  options {
    disableConcurrentBuilds()
    buildDiscarder(logRotator(daysToKeepStr: '60', numToKeepStr: '60', artifactNumToKeepStr: '1'))
  }
  environment {
    ORG = 'nuxeo'
    APP_NAME = 'nexus3'
    CHARTMUSEUM_CREDS = credentials('jenkins-x-chartmuseum')
    DOCKER_REGISTRY_ORG = 'nuxeo'
  }
  stages {
    stage('Prepare environment') {
      steps {
        container('jx-base') {
          sh(script: 'make skaffold@up')
        }
      }
    }
    stage('Build and push snapshot') {
      when {
        anyOf { branch 'PR-*'; branch 'feature-*'; branch 'fix-*'; branch 'improve-*'; }
      }
      environment {
        PREVIEW_VERSION = "0.0.0-$BRANCH_NAME-$BUILD_NUMBER"
        PREVIEW_NAMESPACE = "$APP_NAME-$BRANCH_NAME".toLowerCase()
        HELM_RELEASE = "$PREVIEW_NAMESPACE".toLowerCase()
        VERSION = "$PREVIEW_VERSION"
      }
      steps {
        setGitHubBuildStatus('snapshot', 'Build and push snapshot images', 'PENDING')
        container('jx-base') {
          sh 'make base'
          sh 'make builder'
          sh 'make jenkins-x'
          sh 'make central'
          sh 'make cluster'
          sh 'make team'
          sh 'make maven-ncp'
        }
      }
      post {
        success {
          setGitHubBuildStatus('snapshot', 'Build and push snapshot images', 'SUCCESS')
        }
        failure {
          setGitHubBuildStatus('snapshot', 'Build and push snapshot images', 'FAILURE')
        }
      }
    }
    stage('Prepare release') {
      when {
        branch 'master'
      }
      steps {
        container('jx-base') {
          sh '''#!/bin/sh -xe
git checkout master
git config --global credential.helper store
jx step git credentials
# Set PROD S3 buckets
sed -i "s,nuxeo-devtools-nexus-central-test,nuxeo-devtools-nexus-central," parms/central/blobstore-parms.json
sed -i "s,nuxeo-devtools-nexus-ncp-test,nuxeo-devtools-nexus-ncp-prod," parms/maven-ncp/blobstore-parms.json
'''
        }
      }
    }
    stage('Build and push release') {
      when {
        branch 'master'
      }
      steps {
        setGitHubBuildStatus('release', 'Build and push release images', 'PENDING')
        container('jx-base') {
          withEnv(["VERSION=${getReleaseVersion()}"]) {
            sh 'make build'
          }
        }
      }
      post {
        success {
          setGitHubBuildStatus('release', 'Build and push release images', 'SUCCESS')
          container('jx-base') {
            withEnv(["VERSION=${getReleaseVersion()}"]) {
              // TODO push to grc.io the images used in GCP
              sh '''#!/bin/sh -xe
docker pull ${JENKINS_X_DOCKER_REGISTRY_SERVICE_HOST}:5000/nuxeo/nexus3/central:${VERSION}
docker tag ${JENKINS_X_DOCKER_REGISTRY_SERVICE_HOST}:5000/nuxeo/nexus3/central:${VERSION} dockerpriv.nuxeo.com/devtools/nexus3/central:${VERSION}
docker push dockerpriv.nuxeo.com/devtools/nexus3/central:${VERSION}
docker rmi ${JENKINS_X_DOCKER_REGISTRY_SERVICE_HOST}:5000/nuxeo/nexus3/central:${VERSION} dockerpriv.nuxeo.com/devtools/nexus3/central:${VERSION}

docker pull ${JENKINS_X_DOCKER_REGISTRY_SERVICE_HOST}:5000/nuxeo/nexus3/maven-ncp:${VERSION}
docker tag ${JENKINS_X_DOCKER_REGISTRY_SERVICE_HOST}:5000/nuxeo/nexus3/maven-ncp:${VERSION} dockerpriv.nuxeo.com/devtools/nexus3/maven-ncp:${VERSION}
docker push dockerpriv.nuxeo.com/devtools/nexus3/maven-ncp:${VERSION}
docker rmi ${JENKINS_X_DOCKER_REGISTRY_SERVICE_HOST}:5000/nuxeo/nexus3/maven-ncp:${VERSION} dockerpriv.nuxeo.com/devtools/nexus3/maven-ncp:${VERSION}
'''
            }
          }
        }
        failure {
          setGitHubBuildStatus('release', 'Build and push release images', 'FAILURE')
        }
      }
    }
    stage('Promote release to environments') {
      when {
        branch 'master'
      }
      steps {
        container('jx-base') {
          withEnv(["VERSION=${getReleaseVersion()}"]) {
            sh 'jx step changelog --version v$VERSION'
          }
        }
      }
    }
  }
  post {
    always {
      container('jx-base') {
        sh 'make skaffold@down'
        cleanWs()
      }
    }
  }
}
