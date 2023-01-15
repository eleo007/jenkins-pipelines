library changelog: false, identifier: 'lib@master', retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'https://github.com/Percona-Lab/jenkins-pipelines.git'
]) _

pipeline {
    agent {
        label min-focal-x64
    }

    options {
        skipDefaultCheckout()
    }

    parameters {
        string(
            defaultValue: 'https://github.com/eleo007/percona-qa.git',
            description: '',
            name: 'git_repo',
            trim: false
        )
    }

    stages {
        stage("Prepare") {
            steps {
                script {
                    currentBuild.displayName = "#${BUILD_NUMBER}"
                }
            }
        }

        stage("Test") {
            steps {
                sh '''
                    sudo apt install git
                    git clone https://github.com/sstephenson/bats.git
                    cd bats
                    ./install.sh /usr/local
                    git clone $script_repo
                    cd percona-qa
                    /usr/local/bin/bats get_download_link_test.bats
                '''
            }
        }
    }
}