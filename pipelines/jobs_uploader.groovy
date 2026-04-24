pipeline {
    agent any

    stages {

        stage('DEBUG NODE') {
            steps {
                sh '''
                    set -e
                    echo "HOST=$(hostname)"
                    echo "JENKINS_WORKSPACE=$WORKSPACE"
                    ls -la $WORKSPACE
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

                        echo "CONFIG CREATED"
                        ls -la $WORKSPACE/config.ini
                    '''
                }
            }
        }

        stage('Run JJB') {
            steps {
                sh '''
            set -e

            echo "=== CHECK SYSTEM PYTHON ==="
            python3 --version

            echo "=== INSTALL PYTHON 3.11 VENV (safe) ==="
            python3 -m venv venv
            . venv/bin/activate

            echo "=== FORCE PIP UPDATE ==="
            pip install --upgrade pip setuptools wheel

            echo "=== INSTALL COMPATIBLE JJB ==="
            pip install "jenkins-job-builder==5.0.3"

            echo "=== VERIFY JJB ==="
            jenkins-jobs --version

            echo "=== RUN UPDATE ==="
            jenkins-jobs --conf config.ini update jobs/
        '''
            }
        }
    }

    post {
        always {
            sh '''
                echo "PIPELINE FINISHED"
                ls -la $WORKSPACE
            '''
        }
    }
}