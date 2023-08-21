library changelog: false, identifier: "lib@move_params", retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'https://github.com/eleo007/jenkins-pipelines.git'
])


pipeline {
    agent {
    label 'docker'
    }

    parameters {
        text(pdpsParamsList(), defaultValue: 'One\nTwo\nThree\n', description: '')
    }

    options {
          withCredentials(moleculePdpsJenkinsCreds())
          disableConcurrentBuilds()
    }
    stages {
        stage('Set build name'){
            steps {
                script {
                    currentBuild.displayName = "${env.BUILD_NUMBER}-${env.PLATFORM}-${env.SCENARIO}-${env.MAJOR_REPO}"
                }
            }
        }
    }
}
