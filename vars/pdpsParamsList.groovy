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