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

            echo "=== WORKSPACE CHECK ==="
            echo $WORKSPACE
            ls -la $WORKSPACE

            echo "=== CHECK CONFIG ==="
            cat $WORKSPACE/config.ini

            echo "=== CREATE VENV (Python safe layer) ==="
            python3 -m venv venv
            . venv/bin/activate

            echo "=== PYTHON VERSION ==="
            python --version
            pip --version

            echo "=== INSTALL JJB ==="
            pip install --no-cache-dir --upgrade pip
            pip install jenkins-job-builder==5.0.3

            echo "=== VERIFY JJB ==="
            jenkins-jobs --version

            echo "=== RUN JJB UPDATE ==="
            jenkins-jobs --conf $WORKSPACE/config.ini update $WORKSPACE/jobs/
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