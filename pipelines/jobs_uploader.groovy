pipeline {
    agent any

    stages {

        stage('DEBUG NODE') {
            steps {
                sh '''
                    set -e
                    echo "HOST=$(hostname)"
                    echo "WORKSPACE=$WORKSPACE"

                    python3 --version || echo "python3 not installed"
                    which python3 || true
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

                        echo "CONFIG CREATED"
                    '''
                }
            }
        }

        stage('Setup venv + Run JJB') {
            steps {
                sh '''
                    set -e

                    echo "=== INSTALL DEPENDENCIES LOCALLY ==="

                    python3 -m venv venv
                    . venv/bin/activate

                    pip install --upgrade pip
                    pip install jenkins-job-builder==5.0.3

                    echo "=== RUN JJB ==="
                    jenkins-jobs --version
                    jenkins-jobs --conf config.ini update jobs/
                '''
            }
        }
    }

    post {
        always {
            sh '''
                echo "PIPELINE FINISHED"
                ls -la
            '''
        }
    }
}