pipeline_timeout = 10

pipeline {
    parameters {
        string(
            defaultValue: 'https://github.com/percona/percona-xtradb-cluster',
            description: 'URL to PXC-5.6 repository',
            name: 'PXC56_REPO',
            trim: true)
        string(
            defaultValue: '5.6',
            description: 'Tag/Branch for PXC-5.6 repository',
            name: 'PXC56_BRANCH',
            trim: true)
        string(
            defaultValue: 'https://github.com/percona/percona-xtradb-cluster',
            description: 'URL to PXC-5.7 repository',
            name: 'PXC57_REPO',
            trim: true)
        string(
            defaultValue: '5.7',
            description: 'Tag/Branch for PXC-5.7 repository',
            name: 'PXC57_BRANCH',
            trim: true)
        string(
            defaultValue: 'https://github.com/percona/percona-xtrabackup',
            description: 'URL to PXB24 repository',
            name: 'PXB24_REPO',
            trim: true)
        string(
            defaultValue: 'percona-xtrabackup-2.4.20',
            description: 'Tag/Branch for PXC repository',
            name: 'PXB24_BRANCH',
            trim: true)
        choice(
            choices: 'centos:7',
            description: 'OS version for compilation',
            name: 'DOCKER_OS')
        choice(
            choices: '/usr/bin/cmake',
            description: 'path to cmake binary',
            name: 'JOB_CMAKE')
        choice(
            choices: 'RelWithDebInfo\nDebug',
            description: 'Type of build to produce',
            name: 'CMAKE_BUILD_TYPE')
        string(
            defaultValue: '',
            description: 'cmake options',
            name: 'CMAKE_OPTS')
        string(
            defaultValue: '',
            description: 'make options, like VERBOSE=1',
            name: 'MAKE_OPTS')
	    string(
	        defaultValue: '--suite replication correctness',
	        description: 'qa_framework.py options, for options like: --suite --encryption --debug',
	        name: 'QA_ARGS')
    }
    agent {
        label 'micro-amazon'
    }
    options {
        skipDefaultCheckout()
        skipStagesAfterUnstable()
        timeout(time: 6, unit: 'DAYS')
        buildDiscarder(logRotator(numToKeepStr: '200', artifactNumToKeepStr: '200'))
    }
    stages {
        stage('Prepare') {
            steps {
                script {
                    currentBuild.displayName = "${BUILD_NUMBER} ${CMAKE_BUILD_TYPE}/${DOCKER_OS}"
                }

                sh 'echo Prepare: \$(date -u "+%s")'
                echo 'Checking PXC branch version'
                sh '''
                    MY_BRANCH_BASE_MAJOR=5
                    MY_BRANCH_BASE_MINOR=7
                    RAW_VERSION_LINK=$(echo ${PXC57_REPO%.git} | sed -e "s:github.com:raw.githubusercontent.com:g")
                    wget ${RAW_VERSION_LINK}/${PXC57_BRANCH}/VERSION -O ${WORKSPACE}/VERSION-${BUILD_NUMBER}
                    source ${WORKSPACE}/VERSION-${BUILD_NUMBER}
                    if [[ ${MYSQL_VERSION_MAJOR} -lt ${MY_BRANCH_BASE_MAJOR} ]] ; then
                        echo "Are you trying to build wrong branch?"
                        echo "You are trying to build ${MYSQL_VERSION_MAJOR}.${MYSQL_VERSION_MINOR} instead of ${MY_BRANCH_BASE_MAJOR}.${MY_BRANCH_BASE_MINOR}!"
                        rm -f ${WORKSPACE}/VERSION-${BUILD_NUMBER}
                        exit 1
                    fi
                    rm -f ${WORKSPACE}/VERSION-${BUILD_NUMBER}
                '''
            }
        }
        stage('Check out and Build PXB/PXC') {
            parallel {
                stage('Build PXB24') {
                    agent { label 'docker' }
                    steps {
                        git branch: 'PXC-3309-PXCQA-pipeline-job-updates', url: 'https://github.com/Percona-Lab/jenkins-pipelines'
                        echo 'Checkout PXB24 sources'
                        sh '''
                            # sudo is needed for better node recovery after compilation failure
                            # if building failed on compilation stage directory will have files owned by docker user
                            sudo git reset --hard
                            sudo git clean -xdf
                            sudo rm -rf sources
                            ./pxc/local/checkout57 PXB24
                        '''
                        echo 'Build PXB23'
                        withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'c42456e5-c28d-4962-b32c-b75d161bff27', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                            sh '''
                                sg docker -c "
                                    if [ \$(docker ps -q | wc -l) -ne 0 ]; then
                                        docker ps -q | xargs docker stop --time 1 || :
                                    fi
                                    ./pxc/docker/run-build-pxb24 ${DOCKER_OS}
                                " 2>&1 | tee build.log
                             
                                if [[ -f \$(ls pxc/sources/pxb24/results/*.tar.gz | head -1) ]]; then
                                    until aws s3 cp --no-progress --acl public-read pxc/sources/pxb24/results/*.tar.gz s3://pxc-build-cache/${BUILD_TAG}/pxb24.tar.gz; do
                                        sleep 5
                                    done
                                else
                                    echo cannot find compiled archive
                                    exit 1
                                fi
                            '''
                        }
                    }
                }
                stage('Build PXC56') {
                    agent { label 'docker-32gb' }
                    steps {
                        git branch: 'PXC-3309-PXCQA-pipeline-job-updates', url: 'https://github.com/Percona-Lab/jenkins-pipelines'
                        echo 'Checkout PXC56 sources'
                        sh '''
                            # sudo is needed for better node recovery after compilation failure
                            # if building failed on compilation stage directory will have files owned by docker user
                            sudo git reset --hard
                            sudo git clean -xdf
                            sudo rm -rf sources
							sudo sed -i "22 a GALERA3_BRANCH=3.x" ./pxc/local/checkout56
                            ./pxc/local/checkout56 GALERA3
                            ./pxc/local/checkout56 PXC56
                        '''
                    
                        echo 'Build PXC56'
                        withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'c42456e5-c28d-4962-b32c-b75d161bff27', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                            sh '''							
                                sg docker -c "
                                    if [ \$(docker ps -q | wc -l) -ne 0 ]; then
                                        docker ps -q | xargs docker stop --time 1 || :
                                    fi
                                    ./pxc/docker/run-build-galera3 ${DOCKER_OS}
                                " 2>&1 | tee build.log
                                
                                    cp pxc/sources/galera3/results/libgalera_smm.so ./pxc/sources/pxc56/libgalera_smm.so
                                    cp pxc/sources/galera3/results/garbd ./pxc/sources/pxc56/garbd
								
                                    ./pxc/docker/run-build-pxc56 ${DOCKER_OS}
                                " 2>&1 | tee build.log
         
								
                                    if [[ -f \$(ls pxc/sources/pxc56/results/*.tar.gz | head -1) ]]; then
                                        until aws s3 cp --no-progress --acl public-read pxc/sources/pxc56/results/*.tar.gz s3://pxc-build-cache/${BUILD_TAG}/pxc56.tar.gz; do
                                        sleep 5
                                        done
                                    else
                                        echo cannot find compiled archive
                                        exit 1
                                    fi
                            '''
                       }
                    }
			    }
                stage('Build PXC57') {
                    agent { label 'docker-32gb' }
                    steps {
                        git branch: 'PXC-3309-PXCQA-pipeline-job-updates', url: 'https://github.com/Percona-Lab/jenkins-pipelines'
                        echo 'Checkout PXC57 sources'
                        sh '''
                            # sudo is needed for better node recovery after compilation failure
                            # if building failed on compilation stage directory will have files owned by docker user
                            sudo git reset --hard
                            sudo git clean -xdf
                            sudo rm -rf sources
                            ./pxc/local/checkout57 PXC57
                        '''
                    
                        echo 'Build PXC56'
                        withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'c42456e5-c28d-4962-b32c-b75d161bff27', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                            sh '''							
                                sg docker -c "
                                    if [ \$(docker ps -q | wc -l) -ne 0 ]; then
                                        docker ps -q | xargs docker stop --time 1 || :
                                    fi
                                    ./pxc/docker/run-build-pxc57 ${DOCKER_OS}
                                " 2>&1 | tee build.log
                              
                                if [[ -f \$(ls pxc/sources/pxc57/results/*.tar.gz | head -1) ]]; then
                                    until aws s3 cp --no-progress --acl public-read pxc/sources/pxc57/results/*.tar.gz s3://pxc-build-cache/${BUILD_TAG}/pxc57.tar.gz; do
                                        sleep 5
                                    done
                                else
                                    echo cannot find compiled archive
                                    exit 1
                                fi
                            '''
                        }
                    }
			    }
			}
        }
        stage('Test PXC57') {
                agent { label 'docker-32gb' }
                steps {
                    git branch: 'PXC-3309-PXCQA-pipeline-job-updates', url: 'https://github.com/Percona-Lab/jenkins-pipelines'
                    echo 'Test PXC57'
                    withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'c42456e5-c28d-4962-b32c-b75d161bff27', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                        sh '''
                            until aws s3 cp --no-progress s3://pxc-build-cache/${BUILD_TAG}/pxb24.tar.gz ./pxc/sources/pxc/results/pxb24.tar.gz; do
                                sleep 5
                            done
                            until aws s3 cp --no-progress s3://pxc-build-cache/${BUILD_TAG}/pxc56.tar.gz ./pxc/sources/pxc/results/pxc56.tar.gz; do
                                sleep 5
                            done
                            until aws s3 cp --no-progress s3://pxc-build-cache/${BUILD_TAG}/pxc57.tar.gz ./pxc/sources/pxc/results/pxc57.tar.gz; do
                                sleep 5
                            done

                            sg docker -c "
                                if [ \$(docker ps -q | wc -l) -ne 0 ]; then
                                    docker ps -q | xargs docker stop --time 1 || :
                                fi
                                ./pxc/docker/run-qa-framework-pxc ${DOCKER_OS}
                            "
                        '''
                    }
                    step([$class: 'JUnitResultArchiver', testResults: 'pxc/sources/pxc/results/*.xml', healthScaleFactor: 1.0])
                    archiveArtifacts 'pxc/sources/pxc/results/*.xml,pxc/sources/pxc/results/pxc-qa-framework-run_logs.tar.gz'
                }
        }
    }
    post {
        always {
            sh '''
                echo Finish: \$(date -u "+%s")
            '''
        }
    }
}
