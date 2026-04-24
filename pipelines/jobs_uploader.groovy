pipeline {

    stage('DEBUG NODE') {
        steps {
            sh '''
            echo "NODE NAME: $(hostname)"
            whoami
            pwd
            ls -la
        '''
        }
    }


    agent any

    environment {
        DOCKER_IMAGE = "python:3.10"
    }

    stages {

        sh '''
    HOSTNAME=$(hostname)
    echo "HOST=$HOSTNAME"
'''

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Create config.ini') {

            sh '''
    HOSTNAME=$(hostname)
    echo "HOST=$HOSTNAME"
'''

            steps {
                withCredentials([usernamePassword(credentialsId: 'jenkins', usernameVariable: 'JENKINS_USER', passwordVariable: 'JENKINS_PASS')]) {
                    sh '''
                        set -e

                        echo "=== CREATE config.ini ==="

                        cat > config.ini <<EOF
[jenkins]
url=http://89.124.113.71/jenkins/
user=${JENKINS_USER}
password=${JENKINS_PASS}

[job_builder]
recursive=True
keep_descriptions=False
EOF

                        echo "=== CONFIG CREATED ==="
                        ls -la config.ini
                        cat config.ini
                    '''
                }
            }
        }

        stage('Host debug') {

            sh '''
    HOSTNAME=$(hostname)
    echo "HOST=$HOSTNAME"
'''


            steps {
                sh '''
                    echo "=== HOST DEBUG ==="
                    echo "WORKSPACE=$WORKSPACE"

                    ls -la $WORKSPACE

                    echo "=== CHECK CONFIG ON HOST ==="
                    cat $WORKSPACE/config.ini

                    echo "=== CHECK JOBS DIR ==="
                    ls -la $WORKSPACE/jobs
                '''
            }
        }

        stage('Run JJB') {

            sh '''
    HOSTNAME=$(hostname)
    echo "HOST=$HOSTNAME"
'''


            steps {
                sh '''
rm -rf /tmp/jjb_workspace
mkdir -p /tmp/jjb_workspace

cp -r $WORKSPACE/* /tmp/jjb_workspace/

docker run --rm \
  -v /tmp/jjb_workspace:/workspace \
  -w /workspace \
  python:3.10 bash -c '
    set -e
    ls -la
    ls -la config.ini
    jenkins-jobs --conf config.ini update jobs/
  '
'''
            }
        }
    }

    post {
        always {
            sh '''
                echo "=== PIPELINE FINISHED ==="
                ls -la $WORKSPACE
            '''
        }
    }
}