pipeline {
    agent any

    stages {

        stage('DEBUG NODE') {
            steps {
                sh '''
                    set -e
                    echo "HOST=$(hostname)"
                    echo "JENKINS_WORKSPACE=$WORKSPACE"
                    ls -la $WORKSPACE
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

                        echo "CONFIG CREATED"
                        ls -la $WORKSPACE/config.ini
                    '''
                }
            }
        }

        stage('Run JJB') {
            steps {
                sh '''
            set -e

            echo "=== JENKINS WORKSPACE ==="
            echo $WORKSPACE
            ls -la $WORKSPACE

            echo "=== DOCKER RUN ==="

            docker run --rm \
              -v $WORKSPACE:$WORKSPACE \
              -w $WORKSPACE \
              python:3.10 bash -c '
                set -e

                echo "INSIDE CONTAINER"
                ls -la

                echo "CONFIG CHECK"
                cat config.ini

                echo "JOBS"
                ls -la jobs

                pip install --no-cache-dir jenkins-job-builder==5.0.3

                jenkins-jobs --conf config.ini update jobs/
              '
        '''
            }
        }
    }

    post {
        always {
            sh '''
                echo "PIPELINE FINISHED"
                ls -la $WORKSPACE
            '''
        }
    }
}