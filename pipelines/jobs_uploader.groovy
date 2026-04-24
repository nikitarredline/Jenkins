pipeline {
    agent {
        docker {
            image 'jenkins-agent-python:1.0'
            args '-u jenkins'
        }
    }

    stages {

        stage('DEBUG NODE') {
            steps {
                sh '''
                    set -e
                    echo "HOST=$(hostname)"
                    echo "WORKSPACE=$WORKSPACE"
                    python --version
                    jenkins-jobs --version
                '''
            }
        }

        stage('Create config.ini') {
            steps {
                withCredentials([usernamePassword(
                        credentialsId: 'jenkins',
                        usernameVariable: 'JENKINS_USER',
                        passwordVariable: 'JENKINS_PASS'
                )]) {

                    sh '''
                        set -e

                        cat > config.ini <<EOF
[jenkins]
url=http://89.124.113.71/jenkins/
user=${JENKINS_USER}
password=${JENKINS_PASS}

[job_builder]
recursive=True
keep_descriptions=False
EOF

                        echo "CONFIG CREATED"
                        ls -la config.ini
                    '''
                }
            }
        }

        stage('Run JJB') {
            steps {
                sh '''
                    set -e

                    echo "=== RUN JJB ==="
                    jenkins-jobs --conf config.ini update jobs/
                '''
            }
        }
    }

    post {
        always {
            sh '''
                echo "PIPELINE FINISHED"
                ls -la
            '''
        }
    }
}