pipeline {
    agent any

    stages {

        stage('CLEAN + CHECKOUT') {
            steps {
                cleanWs()

                checkout scm

                sh '''
                    echo "=== AFTER CHECKOUT ==="
                    ls -R .
                '''
            }
        }

        stage('DEBUG JOBS') {
            steps {
                sh '''
                    echo "=== CHECK JOBS ==="
                    ls -la jobs
                    find jobs -type f -name "*.yaml"
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

        stage('RUN JJB') {
            steps {
                sh '''
            set -e

            echo "WORKSPACE=$WORKSPACE"
            ls -la $WORKSPACE

            docker run --rm \
                -v "$WORKSPACE:$WORKSPACE" \
                -w "$WORKSPACE" \
                jenkins-agent-python:1.0 \
                bash -c "
                    set -e

                    echo '=== INSIDE CONTAINER ==='
                    ls -R

                    echo '=== JJB ==='
                    jenkins-jobs --version
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