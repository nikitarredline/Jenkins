pipeline {
    agent any

    stages {

        stage('Parse config') {
            steps {
                script {

                    def config = readYaml text: params.YAML_CONFIG

                    env.BASE_URL = config.BASE_URL
                    env.SELENOID_URL = config.SELENOID_URL
                    env.BROWSER = config.BROWSER
                    env.BROWSER_VERSION = config.BROWSER_VERSION

                    echo "CONFIG LOADED"
                    echo "BASE_URL = ${env.BASE_URL}"
                }
            }
        }

        stage('Run tests in parallel') {
            steps {
                script {
                    def branches = [:]

                    if (params.YAML_CONFIG.contains('ui')) {
                        branches['UI'] = {
                            build job: 'ui_tests', parameters: [
                                string(name: 'SELENOID_URL', value: env.SELENOID_URL),
                                string(name: 'BROWSER', value: env.BROWSER),
                                string(name: 'BROWSER_VERSION', value: env.BROWSER_VERSION)
                            ]
                        }
                    }

                    if (params.YAML_CONFIG.contains('api')) {
                        branches['API'] = {
                            build job: 'api_tests'
                        }
                    }

                    if (params.YAML_CONFIG.contains('mobile')) {
                        branches['MOBILE'] = {
                            build job: 'mobile_tests', parameters: [
                                string(name: 'APPIUM_SERVER', value: env.SELENOID_URL)
                            ]
                        }
                    }

                    parallel branches
                }
            }
        }
    }

    post {
        always {
            echo "TESTS RUNNER FINISHED"
        }
    }
}