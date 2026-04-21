pipelineJob('ui-tests') {
    definition {
        cps {
            script("""
                pipeline {
                    agent any
                    stages {
                        stage('UI tests') {
                            steps {
                                sh 'ansible-playbook -i ./hosts running_tests.yaml --ui'
                            }
                        }
                    }
                }
            """)
        }
    }
}

pipelineJob('api-tests') {
    definition {
        cps {
            script("""
                pipeline {
                    agent any
                    stages {
                        stage('API tests') {
                            steps {
                                sh 'ansible-playbook -i ./hosts running_tests.yaml --api'
                            }
                        }
                    }
                }
            """)
        }
    }
}

pipelineJob('mobile-tests') {
    definition {
        cps {
            script("""
                pipeline {
                    agent any
                    stages {
                        stage('Mobile tests') {
                            steps {
                                sh 'ansible-playbook -i ./hosts running_tests.yaml --mobile'
                            }
                        }
                    }
                }
            """)
        }
    }
}

pipelineJob('runner') {
    definition {
        cps {
            script("""
                pipeline {
                    agent any
                    stages {
                        stage('Parallel run') {
                            parallel {
                                stage('UI') {
                                    steps { build job: 'ui-tests' }
                                }
                                stage('API') {
                                    steps { build job: 'api-tests' }
                                }
                                stage('Mobile') {
                                    steps { build job: 'mobile-tests' }
                                }
                            }
                        }
                    }
                }
            """)
        }
    }
}
