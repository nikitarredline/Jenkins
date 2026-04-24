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

                    echo "=== CHECK WORKSPACE CONTENT ==="
                    ls -la $WORKSPACE
                    ls -la $WORKSPACE/jobs || echo "NO JOBS DIR"

                    docker run --rm \
                        -v $WORKSPACE:/workspace \
                        -w /workspace \
                        jenkins-agent-python:1.0 \
                        bash -c "
                            set -e
                            echo '=== INSIDE CONTAINER ==='
                            ls -la

                            echo '=== PYTHON ==='
                            python --version

                            echo '=== JJB ==='
                            jenkins-jobs --version

                            echo '=== SEARCH YAML ==='
                            find . -name '*.yaml'

                            echo '=== RUN JJB ==='
                            jenkins-jobs --conf config.ini update jobs/
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