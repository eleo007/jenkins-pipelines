library changelog: false, identifier: "lib@DISTMYSQL-213-repo80", retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'https://github.com/eleo007/jenkins-pipelines.git'
])


pipeline {
  agent {
      label 'min-centos-7-x64'
  }
  environment {
      PATH = '/usr/local/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/home/ec2-user/.local/bin';
      MOLECULE_DIR = "molecule/pdmysql/${SCENARIO}";

  }
  parameters {
        choice(
            name: 'REPO',
            description: 'Repo for testing',
            choices: [
                'testing',
                'experimental',
                'release'
            ]
        )
        string(
            defaultValue: '8.0.28',
            description: 'PXC version for test',
            name: 'VERSION'
         )
        string(
            defaultValue: '2.3.2',
            description: 'Proxysql version for test',
            name: 'PROXYSQL_VERSION'
         )
        string(
            defaultValue: '2.5.6',
            description: 'HAProxy version for test',
            name: 'HAPROXY_VERSION'
         )
        string(
            defaultValue: '8.0.28',
            description: 'PXB version for test',
            name: 'PXB_VERSION'
         )
        string(
            defaultValue: '3.4.0',
            description: 'Percona toolkit version for test',
            name: 'PT_VERSION'
         )
        choice(
            name: 'SCENARIO',
            description: 'Scenario for test',
            choices: pdpxcScenarios()
        )
        string(
            defaultValue: 'DISTMYSQL-213-repo80',
            description: 'Branch for testing repository',
            name: 'TESTING_BRANCH'
        )
        booleanParam(
            name: 'MAJOR_REPO', 
            description: "Enable to use major (pdpxc-8.0) repo instead of pdpxc-8.0.XX"
        )
  }
  options {
          withCredentials(moleculePdpxcJenkinsCreds())
          disableConcurrentBuilds()
  }
    stages {
        stage('Set build name'){
          steps {
                    script {
                        currentBuild.displayName = "${env.BUILD_NUMBER}-${env.SCENARIO}"
                    }
                }
            }
        stage('Checkout') {
            steps {
                deleteDir()
                git poll: false, branch: TESTING_BRANCH, url: 'https://github.com/eleo007/package-testing.git'
            }
        }
        stage ('Prepare') {
          steps {
                script {
                   installMolecule()
             }
           }
        }
        stage('Test') {
          steps {
                script {
                    moleculeParallelTest(pdpxcOperatingSystems(), env.MOLECULE_DIR)
                }
            }
         }
  }
    post {
        always {
          script {
              moleculeParallelPostDestroy(pdpxcOperatingSystems(), env.MOLECULE_DIR)
         }
      }
   }
}
