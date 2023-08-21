// def call() {
//     text(name: 'Some My Name', defaultValue: 'One\nTwo\nThree\n', description: '')
// }

def call() {
        script {
        currentBuild.displayName = "${env.BUILD_NUMBER}-${env.PLATFORM}-${env.SCENARIO}-${env.MAJOR_REPO}"
    }
}