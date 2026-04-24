pipeline {
    agent any

    stages {

        stage('DEBUG') {
            steps {
                sh '''
                    set -e
                    echo "HOST=$(hostname)"
                    echo "WORKSPACE=$WORKSPACE"
                    ls -la $WORKSPACE
                    ls -la $WORKSPACE/jobs || echo "NO JOBS DIR"
                    which docker || echo "DOCKER NOT FOUND"
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
                    '''
                }
            }
        }

        stage('Run JJB in Docker') {
            steps {
                sh '''
            set -e

            echo "=== WORKSPACE CONTENT ==="
            ls -R $WORKSPACE

            docker run --rm \
                -v $WORKSPACE:/workspace \
                -w /workspace \
                jenkins-agent-python:1.0 \
                bash -c "
                    set -e
                    echo '=== INSIDE CONTAINER ==='
                    ls -R /workspace

                    echo '=== PYTHON ==='
                    python --version

                    echo '=== JJB ==='
                    jenkins-jobs --version

                    echo '=== RUN JJB ==='
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