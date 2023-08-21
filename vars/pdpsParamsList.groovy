// def call() {
//     text(name: 'Some My Name', defaultValue: 'One\nTwo\nThree\n', description: '')
// }

def call() {
    steps {
        sh """
        echo 'Hello from Ella'
        """
    }
}