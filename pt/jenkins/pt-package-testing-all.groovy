library changelog: false, identifier: 'lib@PT-2018-pt-packagetesting-pxc', retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'https://github.com/eleo007/jenkins-pipelines.git'
]) _

void runNodeBuild(String node_to_test) {
    build(
        job: 'pt-package-testing',
        parameters: [
            string(name: 'product_to_test', value: product_to_test),
            string(name: 'install_repo', value: params.install_repo),
            string(name: 'node_to_test', value: node_to_test),
            string(name: 'git_repo', value: params.git_repo),
            booleanParam(name: 'skip_ps57', value: params.skip_ps57)
        ],
        propagate: true,
        wait: true
    )
}

pipeline {
    agent none

    parameters {
        choice(
            choices: ['pt3'],
            description: 'Product version to test',
            name: 'product_to_test'
        )
        choice(
            choices: ['testing', 'main', 'experimental'],
            description: 'Choose the repo to install percona toolkit packages from',
            name: 'install_repo'
        )
        string(
            defaultValue: 'https://github.com/eleo007/package-testing.git',
            description: '',
            name: 'git_repo',
            trim: false
        )
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '15'))
        skipDefaultCheckout()
    }

    stages {
        stage('Run parallel') {
            parallel {
                stage('Debian Stretch') {
                    steps {
                        runNodeBuild('min-stretch-x64')
                    }
                }

                stage('Debian Buster') {
                    steps {
                        runNodeBuild('min-buster-x64')
                    }
                }

                stage('Debian Bullseye') {
                    steps {
                        runNodeBuild('min-bullseye-x64')
                    }
                }

                stage('Ubuntu Xenial') {
                    steps {
                        runNodeBuild('min-xenial-x64')
                    }
                }

                stage('Ubuntu Bionic') {
                    steps {
                        runNodeBuild('min-bionic-x64')
                    }
                }

                stage('Ubuntu Focal') {
                    steps {
                        runNodeBuild('min-focal-x64')
                    }
                }

                stage('Centos 7') {
                    steps {
                        runNodeBuild('min-centos-7-x64')
                    }
                }

                stage('Centos 8') {
                    steps {
                        runNodeBuild('min-centos-8-x64')
                    }
                }
            }
        }
    }
}
