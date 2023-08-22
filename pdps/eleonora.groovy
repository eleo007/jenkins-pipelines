library changelog: false, identifier: "lib@move_params", retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'https://github.com/eleo007/jenkins-pipelines.git'
])

pdpsParamsList()

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
                sh """
                echo 'Hello from Ella'
                """
                }
            }
        }
    }
}

