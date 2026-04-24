pipeline {
    agent any

    stages {

        stage('DEBUG HOST') {
            steps {
                sh '''
                    set -e
                    echo "WORKSPACE=$WORKSPACE"
                    ls -la $WORKSPACE
                    ls -la $WORKSPACE/jobs
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
                    '''
                }
            }
        }

        stage('VERIFY DOCKER MOUNT') {
            steps {
                sh '''
                    set -e
                    docker run --rm \
                        -v $WORKSPACE:/workspace \
                        alpine ls -R /workspace
                '''
            }
        }

        stage('RUN JJB') {
            steps {
                sh '''
                    set -e

                    docker run --rm \
                        -v /var/jenkins_home:/var/jenkins_home \
                        -w /workspace \
                        jenkins-agent-python:1.0 \
                        bash -c "
                            set -e
                            echo 'INSIDE:'
                            ls -la

                            echo 'JOBS CHECK:'
                            ls -la jobs

                            echo 'PYTHON:'
                            python --version

                            echo 'JJB:'
                            jenkins-jobs --version

                            echo 'RUN:'
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