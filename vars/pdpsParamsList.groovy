// def call() {
//     text(name: 'Some My Name', defaultValue: 'One\nTwo\nThree\n', description: '')
// }

def call() {
    stage('Set build name'){    
        steps {
            sh """
            echo 'Hello from Ella'
            """
        }
    }
}