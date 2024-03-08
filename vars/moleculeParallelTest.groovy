def call(operatingSystems, moleculeDir) {
  tests = [:]
  operatingSystems.each { os ->
   tests["${os}"] =  {
        stage("${os}") {
            sh """
                  export Molecule_Debug=1
                  . virtenv/bin/activate
                  cd ${moleculeDir}
                  molecule test -s ${os}
               """
        }
      }
    }
  parallel tests
}
