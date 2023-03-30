library changelog: false, identifier: "lib@DISTMYSQL-278_orch_docker_new", retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'https://github.com/eleo007/jenkins-pipelines.git'
])


pipeline {
  agent {
      label "min-focal-x64"
  }

  parameters {
    choice(
      name: 'DOCKER_ACC',
      description: 'Docker repo to use: percona or perconalab',
      choices: [
        'percona',
        'perconalab'
      ]
    )
    string(
      defaultValue: '3.2.6-8',
      description: 'Full orchestrator version',
      name: 'OCHESTARTOR_VERSION'
    )
    string(
      defaultValue: '8.0.32-24',
      description: 'Full PS version to test with orchestrator',
      name: 'PS_VERSION'
    )
    string(
    defaultValue: 'https://github.com/eleo007/package-testing.git',
    description: 'Repo for package-testing repository',
    name: 'TESTING_REPO'
   )
    string(
      defaultValue: 'DISTMYSQL-278_orch_docker_new',
      description: 'Branch for package-testing repository',
      name: 'TESTING_BRANCH'
    )
  }

  stages {
    stage('Run test') {
      steps {
          script {
            currentBuild.displayName = "#${BUILD_NUMBER}-${PS_VERSION}-${OCHESTARTOR_VERSION}"
            currentBuild.description = "${DOCKER_ACC}"
          }
          sh '''
            # run test
            export PATH=${PATH}:~/.local/bin
            sudo yum install -y python3 python3-pip
            rm -rf package-testing
            git clone ${TESTING_REPO} -b ${TESTING_BRANCH} --depth 1
            cd package-testing/docker-image-tests/orchestrator
            pip3 install --user -r requirements.txt
            ./run.sh
          '''
      } //end steps
    } //end Run test stage
  } //end stages
  post {
    always {
      junit 'package-testing/docker-image-tests/orchestrator/report.xml'
    }
  }
}
