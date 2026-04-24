pipeline {
    agent any

    stages {

        stage('DEBUG NODE') {
            steps {
                sh '''
                    set -e
                    echo "HOST=$(hostname)"
                    echo "WORKSPACE=$WORKSPACE"
                    ls -la
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

                        echo "=== CREATE config.ini ==="

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
                        cat config.ini
                    '''
                }
            }
        }

        stage('Host debug') {
            steps {
                sh '''
                    set -e

                    echo "=== HOST DEBUG ==="
                    echo "WORKSPACE=$WORKSPACE"

                    ls -la "$WORKSPACE"
                    ls -la jobs
                '''
            }
        }

        stage('Run Jenkins Job Builder') {
            steps {
                sh '''
                    set -e

                    echo "=== RESOLVED PATH (HOST) ==="

                    REAL_WS=/root/jenkins_home/workspace/jobs_uploader

                    echo "REAL_WS=$REAL_WS"

                    if [ ! -d "$REAL_WS" ]; then
                        echo "ERROR: REAL_WS not found on host"
                        exit 1
                    fi

                    ls -la "$REAL_WS"

                    echo "=== DOCKER RUN ==="

                    docker run --rm \
                      -v /root/jenkins_home/workspace/jobs_uploader:/workspace \
                      -w /workspace \
                      python:3.10 bash -c '
                        set -e

                        echo "=== INSIDE CONTAINER ==="
                        pwd
                        ls -la

                        echo "=== CONFIG CHECK ==="
                        ls -la config.ini
                        cat config.ini

                        echo "=== JOBS ==="
                        ls -la jobs

                        python --version

                        pip install --no-cache-dir jenkins-job-builder==5.0.3

                        jenkins-jobs --version

                        echo "=== RUN JJB ==="
                        jenkins-jobs --conf config.ini update jobs/
                      '
                '''
            }
        }
    }

    post {
        always {
            sh '''
                echo "=== PIPELINE FINISHED ==="
                ls -la
            '''
        }
    }
}