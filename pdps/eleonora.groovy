library changelog: false, identifier: "lib@move_params", retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'https://github.com/eleo007/jenkins-pipelines.git'
])


pipeline {
    agent {
    label 'docker'
    }

    parameters {
        text(name: 'Some My Name', defaultValue: 'One\nTwo\nThree\n', description: '')
    }

    options {
          withCredentials(moleculePdpsJenkinsCreds())
          disableConcurrentBuilds()
    }
    stages {
        stage('Set build name'){
            pdpsParamsList()
        }
    }
}
