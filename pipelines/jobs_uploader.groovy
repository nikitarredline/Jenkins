pipeline {
    agent any

    stages {

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

        stage('Run JJB') {
            steps {
                sh '''
                    set -e

                    docker run --rm \
                      -v /root/jenkins_home/workspace/jobs_uploader:/workspace \
                      -w /workspace \
                      jenkins-agent-python:1.0 \
                      bash -c "
                        set -e
                        jenkins-jobs --conf config.ini update jobs/
                      "
                '''
            }
        }
    }

    post {
        always {
            echo "PIPELINE FINISHED"
        }
    }
}