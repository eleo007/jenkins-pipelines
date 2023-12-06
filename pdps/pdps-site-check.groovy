library changelog: false, identifier: "lib@site_checks", retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'https://github.com/eleo007/jenkins-pipelines.git'
])

pipeline {
    agent {
        label 'docker'
    }
    environment {
        PATH = '/usr/local/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/home/ec2-user/.local/bin'
    }
    parameters {
        string(
            defaultValue: '8.0.34-26.1',
            description: 'PS Version for tests. ',
            name: 'PS_VER_FULL')
        string(
            defaultValue: '8.0.34-29.1',
            description: 'PXB Version for tests. ',
            name: 'PXB_VER_FULL')
        string(
            defaultValue: '3.2.6-10',
            description: 'Orchestrator Version for tests. ',
            name: 'ORCH_VER_FULL')
        string(
            defaultValue: '3.5.4',
            description: 'PT Version for tests. ',
            name: 'PT_VER')
        string(
            defaultValue: '2.5.5',
            description: 'Proxysql Version for tests. ',
            name: 'PROXYSQL_VER')
        string(
            defaultValue: 'main',
            description: 'Branch for testing repository',
            name: 'TESTING_BRANCH')
        string(
            defaultValue: 'eleo007',
            description: 'Branch for testing repository',
            name: 'TESTING_GIT_ACCOUNT')
    }
    stages {
        stage('Set build name'){
            steps {
                script {
                    currentBuild.displayName = "${params.PS_VER_FULL}-${params.TESTING_BRANCH}"
                }
            }
        }
        stage('Checkout') {
            steps {
                deleteDir()
                git poll: false, branch: TESTING_BRANCH, url: "https://github.com/${TESTING_GIT_ACCOUNT}/package-testing.git"
            }
        }
        stage('Test') {
            steps {
                script {
                    sh """
                        cd site_checks
                        docker run --env PS_VER_FULL=${params.PS_VER_FULL} --env PXB_VER_FULL=${params.PXB_VER_FULL} --env ORCH_VER_FULL=${params.ORCH_VER_FULL} \
                        --env PT_VER=${params.PT_VER} --env PROXYSQL_VER=${params.PROXYSQL_VER} --rm -v `pwd`:/tmp -w /tmp python bash -c \
                        'pip3 install requests pytest setuptools && \
                        pytest -s --junitxml=junit.xml test_pdps.py || [ \$? = 1 ] '
                    """
                }
            }
        }
    }
    post {
        always {
            script {
                junit testResults: "**/junit.xml", keepLongStdio: true, allowEmptyResults: true, skipPublishingChecks: true
                sh '''
                    sudo rm -rf ./*
                '''
                deleteDir()
            }
        }
    }
}