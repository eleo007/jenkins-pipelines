def call() {
    properties([
        parameters([
                choice(
                name: 'REPO',
                description: 'Repo for testing',
                choices: [
                        'testing',
                        'experimental',
                        'release'
                        ]
                )
                booleanParam(
                name: 'MAJOR_REPO',
                description: "Enable to use major (pdps-8.0) repo instead of pdps-8.0.XX"
                )
        ])
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