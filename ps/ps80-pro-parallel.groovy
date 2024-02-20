library changelog: false, identifier: "lib@pro_build_job", retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'https://github.com/eleo007/jenkins-pipelines.git'
])

pipeline {
  agent {
    label 'min-centos-7-x64'
  }
  environment {
    PATH = '/usr/local/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/home/ec2-user/.local/bin';
    MOLECULE_DIR = "molecule/ps-80-pro/${SCENARIO}";
  }
  parameters {
    choice(
      name: "product_to_test",
      choices: ["ps80", "client_test"],
      description: "Product for which the packages will be tested"
    )

    choice(
      name: "install_repo",
      choices: ["testing", "main", "experimental"],
      description: "Repo to use in install test"
    )

    // choice(
    //     name: "action_to_test",
    //     choices: ["all"] + all_actions,
    //     description: "Action to test on the product"
    // )

    choice(
      name: "check_warnings",
      choices: ["yes", "no"],
      description: "check warning in client_test"
    )

    choice(
      name: "install_mysql_shell",
      choices: ["yes", "no"],
      description: "install and check mysql-shell for ps80"
    )
    choice(
      name: 'SCENARIO',
      description: 'PDMYSQL scenario for test',
      choices: ["install",]
    )
    choice(
      name: 'pro_test',
      description: 'Mark whether the test is for pro packages or not',
      choices: ["yes",'no']
    )
    string(
      defaultValue: 'pro_build_job',
      description: 'Branch for package-testing repository',
      name: 'TESTING_BRANCH'
    )
    string(
      defaultValue: 'eleo007',
      description: 'Git account for package-testing repository',
      name: 'TESTING_GIT_ACCOUNT'
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
          currentBuild.displayName = "${env.BUILD_NUMBER}-${env.SCENARIO}"
          currentBuild.description = "${env.install_repo}-${env.TESTING_BRANCH}"
        }
      }
    }
    stage('Check version param and checkout') {
      steps {
        deleteDir()
        git poll: false, branch: TESTING_BRANCH, url: "https://github.com/${TESTING_GIT_ACCOUNT}/package-testing.git"
      }
    }
    stage ('Prepare') {
      steps {
        script {
          installMolecule()
        }
      }
    }
    stage('INSTALL') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'PS_PRIVATE_REPO_ACCESS', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
          script {
            moleculeParallelTest(['ubuntu-jammy',], env.MOLECULE_DIR)
          }
        }
      }
    }
  }
  post {
    always {
      script {
        moleculeParallelPostDestroy(['ubuntu-jammy',], env.MOLECULE_DIR)
      }
    }
  }
}
