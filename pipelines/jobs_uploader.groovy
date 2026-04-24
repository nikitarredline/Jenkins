pipeline {
    agent any

    stages {

        stage('DEBUG') {
            steps {
                sh '''
                    echo "HOST=$(hostname)"
                    echo "WORKSPACE=$WORKSPACE"
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
                    docker run --rm \
                        -v $WORKSPACE:/workspace \
                        -w /workspace \
                        jenkins-agent-python:1.0 \
                        bash -c "
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