library changelog: false, identifier: "lib@move_params", retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'https://github.com/eleo007/jenkins-pipelines.git'
])


pipeline {
    agent {
    label 'docker'
    }
    parameters {
        choice(
            name: 'PLATFORM',
            description: 'For what platform (OS) need to test',
            choices: pdpsOperatingSystems()
        )
        choice(
            name: 'REPO',
            description: 'Repo for testing',
            choices: [
                'testing',
                'experimental',
                'release'
            ]
        )
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
