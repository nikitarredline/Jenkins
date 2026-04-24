pipeline {
    agent any

    stages {

        stage('DEBUG') {
            steps {
                sh '''
                    set -e
                    echo "HOST=$(hostname)"
                    echo "WORKSPACE=$WORKSPACE"
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

                        cat > config.ini <<EOF
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

            docker run --rm \
                -v $WORKSPACE:/workspace \
                -w /workspace \
                jenkins-agent-python:1.0 \
                bash -c "
                    set -e

                    echo '=== PYTHON ==='
                    python --version

                    echo '=== JJB ==='
                    which jenkins-jobs
                    jenkins-jobs --version

                    echo '=== YAML FILES ==='
                    find jobs -type f -name '*.yaml' \
                        -exec echo '==== {} ====' \\; \
                        -exec cat {} \\;

                    echo '=== RUN JJB ==='
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
            '''
        }
    }
}