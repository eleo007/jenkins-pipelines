library changelog: false, identifier: "lib@move_params", retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'https://github.com/eleo007/jenkins-pipelines.git'
])

pdpsParamsList(currentBuild.projectName)

pipeline {
    agent {
    label 'docker'
    }

    options {
          withCredentials(moleculePdpsJenkinsCreds())
          disableConcurrentBuilds()
    }
    stages {
        stage('Test'){
             steps {
                 script {
                    currentBuild.displayName = "${env.BUILD_NUMBER}-${env.PLATFORM}-${env.SCENARIO}-${env.MAJOR_REPO}"
                }
            }
        }
    }
}

