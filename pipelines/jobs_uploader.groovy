pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "python:3.10"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Create config.ini') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'jenkins-creds', usernameVariable: 'JENKINS_USER', passwordVariable: 'JENKINS_PASS')]) {
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

                        echo "=== CONFIG CREATED ==="
                        ls -la config.ini
                        cat config.ini
                    '''
                }
            }
        }

        stage('Host debug') {
            steps {
                sh '''
                    echo "=== HOST DEBUG ==="
                    echo "WORKSPACE=$WORKSPACE"

                    ls -la $WORKSPACE

                    echo "=== CHECK CONFIG ON HOST ==="
                    cat $WORKSPACE/config.ini

                    echo "=== CHECK JOBS DIR ==="
                    ls -la $WORKSPACE/jobs
                '''
            }
        }

        stage('Run Jenkins Job Builder') {
            steps {
                sh '''
                    set -e

                    echo "=== START DOCKER ==="

                    docker run --rm \
                      -v "$WORKSPACE:/workspace" \
                      -w /workspace \
                      python:3.10 bash -c '

                        set -e

                        echo "=== INSIDE CONTAINER ==="
                        pwd
                        ls -la

                        echo "=== VERIFY FILES ==="
                        ls -la config.ini
                        cat config.ini

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
            sh '''
                echo "=== PIPELINE FINISHED ==="
                ls -la $WORKSPACE
            '''
        }
    }
}