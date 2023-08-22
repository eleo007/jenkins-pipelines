def call() {
    properties([
        parameters([
                text(name: 'Some My Name', defaultValue: 'One\nTwo\nThree\n', description: '')
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