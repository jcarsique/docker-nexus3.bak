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
  [$class: 'GithubProjectProperty', projectUrlStr: 'https://github.com/nuxeo/jx-docker-images'],
  [$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', daysToKeepStr: '60', numToKeepStr: '60', artifactNumToKeepStr: '1']],
])

pipeline {
  agent {
    label "jenkins-jx-base"
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
          dir('nexus3') { 
            sh(script: 'make skaffold@up')
          }
        }
      }
    }
    stage('Build and push snapshot') {
      when {
        anyOf { branch 'PR-*'; branch 'feature-*'; branch 'fix-*'; }
      }
      environment {
        PREVIEW_VERSION = "0.0.0-$BRANCH_NAME-$BUILD_NUMBER"
        PREVIEW_NAMESPACE = "$APP_NAME-$BRANCH_NAME".toLowerCase()
        HELM_RELEASE = "$PREVIEW_NAMESPACE".toLowerCase()
        VERSION = "$PREVIEW_VERSION"
      }
      steps {
        container('jx-base') {
          dir('nexus3') {
	    sh 'make base'
	    sh 'make builder'
	    sh 'make jenkins'
	    sh 'make central'
	  }
        }
      }
    }
    stage('Build and push release') {
      when {
        branch 'master'
      }
      steps {
        container('jx-base') {
          dir('nexus3') { sh 'VERSION=$(jx-release-version) make build' }
        }
      }
    }
    stage('Promote release to environments') {
      when {
        branch 'master'
      }
      steps {
        container('jx-base') {
          dir('nexus3') {
            // ensure we're not on a detached head
            sh '''#!/bin/sh -xe
git checkout master
git config --global credential.helper store
jx step git credentials'''
            sh 'jx step changelog --version v$(jx-release-version)'
          }
        }
      }
    }
  }
  post {
    always {
      container('jx-base') {
        dir('nexus3') { sh 'make skaffold@down' }
        cleanWs()
      }
    }
  }
}
