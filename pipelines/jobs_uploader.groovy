pipeline {
    agent any

    stages {

        stage('DEBUG NODE') {
            steps {
                sh '''
                    echo "HOST=$(hostname)"
                    echo "WORKSPACE=$WORKSPACE"
                    ls -la
                '''
            }
        }

        stage('Create config.ini') {
            steps {
                withCredentials([string(credentialsId: 'jenkins-creds', variable: 'JENKINS_PASS')]) {
                    sh '''
                        set -e

                        cat > config.ini <<EOF
[jenkins]
url=http://89.124.113.71/jenkins/
user=admin
password=${JENKINS_PASS}

[job_builder]
recursive=True
keep_descriptions=False
EOF

                        echo "CONFIG CREATED"
                        ls -la config.ini
                        cat config.ini
                    '''
                }
            }
        }

        stage('Host debug') {
            steps {
                sh '''
                    echo "HOST DEBUG"
                    echo "WORKSPACE=$WORKSPACE"
                    ls -la $WORKSPACE
                    ls -la jobs
                '''
            }
        }

        stage('Run Jenkins Job Builder') {
            steps {
                sh '''
                    set -e

                    echo "=== DOCKER RUN ==="

                    docker run --rm \
                      -v $WORKSPACE:/workspace \
                      -w /workspace \
                      python:3.10 \
                      bash -c "
                        set -e

                        echo 'INSIDE CONTAINER'
                        pwd
                        ls -la

                        echo 'CONFIG CHECK'
                        ls -la config.ini
                        cat config.ini

                        echo 'JOBS'
                        ls -la jobs

                        python --version

                        pip install --no-cache-dir jenkins-job-builder==5.0.3

                        jenkins-jobs --version
                        jenkins-jobs --conf config.ini update jobs/
                      "
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