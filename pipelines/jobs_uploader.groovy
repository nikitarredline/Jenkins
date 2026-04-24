pipeline {
    agent any

    stages {

        stage('DEBUG NODE') {
            steps {
                sh '''
                    echo "HOST=$(hostname)"
                    echo "WORKSPACE=$WORKSPACE"
                    ls -la
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
                        ls -la config.ini
                    '''
                }
            }
        }

        stage('Host debug') {
            steps {
                sh '''
                    echo "HOST DEBUG"
                    echo "WORKSPACE=$WORKSPACE"

                    ls -la $WORKSPACE
                    ls -la jobs
                '''
            }
        }

        stage('Run JJB') {
            steps {
                sh '''
            set -e

            REAL_WS=/var/jenkins_home/workspace/jobs_uploader

            echo "REAL_WS=$REAL_WS"
            ls -la $REAL_WS

            docker run --rm \
              -v $REAL_WS:/workspace \
              -w /workspace \
              python:3.10 bash -c '
                set -e

                echo "INSIDE"
                ls -la

                echo "CONFIG"
                ls -la config.ini
                cat config.ini

                pip install --no-cache-dir jenkins-job-builder==5.0.3

                jenkins-jobs --conf config.ini update jobs/
              '
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