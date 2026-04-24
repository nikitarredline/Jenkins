pipeline {
    agent any

    stages {

        stage('DEBUG - CHECK WORKSPACE') {
            steps {
                sh '''
                    set -e

                    echo "=== HOST INFO ==="
                    hostname

                    echo "=== WORKSPACE ==="
                    echo $WORKSPACE
                    ls -la $WORKSPACE

                    echo "=== JOBS DIR ==="
                    ls -la $WORKSPACE/jobs || echo "❌ NO JOBS DIRECTORY"
                '''
            }
        }

        stage('CREATE config.ini') {
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

        stage('RUN JJB IN DOCKER') {
            steps {
                sh '''
                    set -e

                    docker run --rm \
                        -v $WORKSPACE:/workspace \
                        -w /workspace \
                        jenkins-agent-python:1.0 \
                        bash -c "
                            set -e

                            echo '=== INSIDE CONTAINER ==='
                            ls -la

                            echo '=== CHECK JOBS ==='
                            ls -la jobs || echo '❌ NO JOBS IN CONTAINER'

                            echo '=== PYTHON ==='
                            python --version

                            echo '=== JJB VERSION ==='
                            jenkins-jobs --version

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