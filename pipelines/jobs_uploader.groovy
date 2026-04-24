pipeline {
    agent any

    stages {

        stage('DEBUG HOST') {
            steps {
                sh '''
                    set -e
                    echo "WORKSPACE=$WORKSPACE"
                    ls -la $WORKSPACE
                    ls -la $WORKSPACE/jobs || true
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

                        cat > $WORKSPACE/config.ini <<EOF
[jenkins]
url=http://89.124.113.71/jenkins/
user=${JENKINS_USER}
password=${JENKINS_PASS}

[job_builder]
recursive=True
keep_descriptions=False
EOF

                        echo "config.ini created"
                        ls -la $WORKSPACE/config.ini
                    '''
                }
            }
        }

        stage('VERIFY DOCKER ACCESS') {
            steps {
                sh '''
                    set -e

                    echo "Testing docker mount..."
                    docker run --rm \
                      -v $WORKSPACE:/workspace \
                      alpine ls -R /workspace
                '''
            }
        }

        stage('DOCKER MOUNT TEST') {
            steps {
                sh '''
            set -e

            echo "WORKSPACE = $WORKSPACE"
            ls -la $WORKSPACE

            echo "=== TEST DOCKER MOUNT ==="
            docker run --rm \
              -v $WORKSPACE:/workspace \
              alpine ls -la /workspace
        '''
            }
        }

        stage('RUN JJB') {
            steps {
                sh '''
            set -e

            echo "RUNNING JJB DIRECTLY IN JENKINS"

            ls -la jobs

            jenkins-jobs --version
            jenkins-jobs --conf config.ini update jobs/
        '''
            }
        }
    }

    post {
        always {
            sh 'echo PIPELINE FINISHED'
        }
    }
}