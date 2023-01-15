library changelog: false, identifier: 'lib@hackday_download_link', retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'https://github.com/eleo007/jenkins-pipelines.git'
]) _

pipeline {
    agent {
        label 'min-focal-x64'
    }

    options {
        skipDefaultCheckout()
    }

    parameters {
        string(
            defaultValue: 'https://github.com/eleo007/percona-qa.git',
            description: '',
            name: 'script_repo',
            trim: false
        )
        string(
            defaultValue: 'hackday_download_bats',
            description: '',
            name: 'repo_branch',
            trim: false
        )
    }

    stages {
        stage("Prepare") {
            steps {
                script {
                    currentBuild.displayName = "#${BUILD_NUMBER}"
                }

                sh '''
                    sudo apt install git
                    git clone https://github.com/sstephenson/bats.git
                    cd bats
                    sudo ./install.sh /usr/local
                    git clone --depth 1 $script_repo -b $repo_branch
                '''
            }
        }

        stage("Test") {
            steps {
                sh '''
                    cd percona-qa
                    /usr/local/bin/bats get_download_link_test.bats
                '''
            }
        }
    }
}