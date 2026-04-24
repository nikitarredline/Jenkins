pipeline {
    agent any

    environment {
        IMAGE = "python:3.10"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Host debug') {
            steps {
                sh '''
                    echo "=== HOST INFO ==="
                    echo "WORKSPACE=$WORKSPACE"
                    ls -la $WORKSPACE
                    echo "CONFIG:"
                    cat $WORKSPACE/config.ini
                    echo "JOBS:"
                    ls -la $WORKSPACE/jobs
                '''
            }
        }

        stage('Run Jenkins Job Builder') {
            steps {
                sh '''
                    set -e

                    echo "=== RUN DOCKER ==="

                    docker run --rm \
                      -v "$WORKSPACE:/workspace" \
                      -w /workspace \
                      python:3.10 bash -c '

                        set -e

                        echo "=== INSIDE CONTAINER ==="
                        pwd
                        ls -la

                        echo "=== CHECK CONFIG ==="
                        ls -la config.ini
                        cat config.ini

                        echo "=== CHECK JOBS ==="
                        ls -la jobs

                        echo "=== PYTHON VERSION ==="
                        python --version

                        echo "=== INSTALL JJB ==="
                        pip install --no-cache-dir jenkins-job-builder==5.0.3

                        echo "=== JJB VERSION ==="
                        jenkins-jobs --version

                        echo "=== RUN UPDATE ==="
                        jenkins-jobs --conf config.ini update jobs/
                      '
                '''
            }
        }
    }

    post {
        always {
            sh 'echo "BUILD FINISHED"'
        }
    }
}