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
            name: 'PXC_VER_FULL')
        string(
            defaultValue: '47',
            description: 'PXB Version for tests. ',
            name: 'PXC57_INNODB')
        string(
            defaultValue: 'site_checks_pull',
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
                    currentBuild.displayName = "${params.PXC_VER_FULL}-${params.TESTING_BRANCH}"
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
                        docker run --env PXC_VER_FULL=${params.PXC_VER_FULL} --env PXC57_INNODB=${params.PXC57_INNODB} \
                            --rm -v `pwd`:/tmp -w /tmp python bash -c \
                            'pip3 install requests pytest setuptools && pytest -s --junitxml=junit.xml test_pxc.py || [ \$? = 1 ] '
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