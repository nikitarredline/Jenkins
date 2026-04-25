pipeline {
    agent any

    stages {

        stage('DEBUG HOST') {
            steps {
                sh '''
                    set -e
                    echo "WORKSPACE=$WORKSPACE"
                    ls -la $WORKSPACE
                    ls -la $WORKSPACE/jobs || true
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

                        ls -la $WORKSPACE/config.ini
                    '''
                }
            }
        }

        stage('VERIFY DOCKER ACCESS') {
            steps {
                sh '''
                    set -e
                    echo "Testing mount..."

                    docker run --rm \
                      -v /root/jenkins_home/workspace/jobs_uploader:/workspace \
                      alpine ls -la /workspace
                '''
            }
        }

        stage('RUN JJB') {
            steps {
                sh '''
                    set -e

                    docker run --rm \
                      -v /root/jenkins_home/workspace/jobs_uploader:/workspace \
                      -w /workspace \
                      jenkins-agent-python:1.0 bash -c "
                        set -e

                        echo INSIDE CONTAINER
                        pwd
                        ls -la jobs

                        python --version
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