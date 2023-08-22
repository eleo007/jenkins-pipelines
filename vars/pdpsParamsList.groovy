def call(jobName) {
    common_parameters=[
        choice(
        name: 'REPO',
        description: 'Repo for testing',
        choices: [
                'testing',
                'experimental',
                'release'
                ]
        ),
        string(
        defaultValue: '8.0.33-25',
        description: 'Percona Server version for test. Possible values are with and without percona release and build: 8.0.32, 8.0.32-24 OR 8.0.32-24.2',
        name: 'VERSION'
        ),
        string(
        defaultValue: '',
        description: 'Percona Server revision for test. Empty by default (not checked).',
        name: 'PS_REVISION'
        ),
        string(
        defaultValue: '2.5.1',
        description: 'Proxysql version for test',
        name: 'PROXYSQL_VERSION'
        ),
        string(
        defaultValue: '8.0.33-27',
        description: 'PXB version for test. Possible values are with and without percona release and build: 8.0.32, 8.0.32-25 OR 8.0.32-25.1',
        name: 'PXB_VERSION'
        ),
        string(
        defaultValue: '3.5.3',
        description: 'Percona toolkit version for test',
        name: 'PT_VERSION'
        ),
        string(
        defaultValue: '3.2.6-9',
        description: 'Percona orchestrator version for test',
        name: 'ORCHESTRATOR_VERSION'
        ),
        string(
        defaultValue: '',
        description: 'Orchestrator revision for version from https://github.com/percona/orchestrator . Empty by default (not checked).',
        name: 'ORCHESTRATOR_REVISION'
        ),
        choice(
        name: 'SCENARIO',
        description: 'PDMYSQL scenario for test',
        choices: pdpsScenarios()
        ),
        string(
        defaultValue: 'master',
        description: 'Branch for package-testing repository',
        name: 'TESTING_BRANCH'
        ),
        string(
        defaultValue: 'Percona-QA',
        description: 'Git account for package-testing repository',
        name: 'TESTING_GIT_ACCOUNT'
        ),
        string(
        defaultValue: 'master',
        description: 'Tests will be run from branch of  https://github.com/percona/orchestrator',
        name: 'ORCHESTRATOR_TESTS_VERSION'
        )
    ]
    if (currentBuild.projectName == 'eleonora'){
        common_parameters.add(
                        booleanParam(
                        name: 'ELLA_JOB',
                        description: "Your if condition works like a charm!"
                        ))
        } 
    properties([
        parameters(
                common_parameters
        )
    ])
}

// def call() {
//     stage('Set build name'){    
//             sh """
//             echo 'Hello from Ella'
//             """
//     }
//     stage('Set one more build name'){    
//             sh """
//             echo 'Hello from Ella2'
//             """
//     }
// }