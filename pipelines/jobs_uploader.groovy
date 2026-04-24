pipeline {
    agent any

    stages {

        stage('DEBUG HOST') {
            steps {
                sh '''
                    set -e
                    echo "HOST=$(hostname)"
                    echo "WORKSPACE=$WORKSPACE"
                    ls -la $WORKSPACE
                '''
            }
        }

        stage('Checkout + VERIFY') {
            steps {
                checkout scm

                sh '''
                    set -e
                    echo "=== CHECK JOBS ON HOST ==="
                    ls -la jobs || echo "NO JOBS ON HOST"
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
cat > $WORKSPACE/config.ini <<EOF
[jenkins]
url=http://89.124.113.71/jenkins/
user=${JENKINS_USER}
password=${JENKINS_PASS}

[job_builder]
recursive=True
keep_descriptions=False
EOF

ls -la $WORKSPACE/config.ini
                    '''
                }
            }
        }

        stage('RUN JJB') {
            steps {
                sh '''
                    set -e

                    echo "=== CHECK INSIDE CONTAINER ==="

                    docker run --rm \
                        -v $WORKSPACE:/workspace \
                        -w /workspace \
                        jenkins-agent-python:1.0 \
                        bash -c "
                            set -e
                            echo 'INSIDE:'
                            ls -la /workspace

                            echo 'PYTHON:'
                            python --version

                            echo 'JJB:'
                            which jenkins-jobs
                            jenkins-jobs --version

                            echo 'RUN UPDATE:'
                            jenkins-jobs --conf /workspace/config.ini update /workspace/jobs
                        "
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