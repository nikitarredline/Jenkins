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

                        echo "config.ini created"
                        ls -la $WORKSPACE/config.ini
                    '''
                }
            }
        }

        stage('VERIFY DOCKER ACCESS') {
            steps {
                sh '''
                    set -e

                    echo "Testing docker mount..."
                    docker run --rm \
                      -v $WORKSPACE:/workspace \
                      alpine ls -R /workspace
                '''
            }
        }

        stage('DOCKER MOUNT TEST') {
            steps {
                sh '''
            set -e

            echo "WORKSPACE = $WORKSPACE"
            ls -la $WORKSPACE

            echo "=== TEST DOCKER MOUNT ==="
            docker run --rm \
              -v $WORKSPACE:/workspace \
              alpine ls -la /workspace
        '''
            }
        }

        stage('DEBUG HOST PATH') {
            steps {
                sh '''
            echo "REAL PATH TEST"

            pwd
            readlink -f $WORKSPACE || true

            docker run --rm \
              -v $(pwd):/workspace \
              alpine ls -la /workspace
        '''
            }
        }

        stage('MOUNT DEBUG') {
            steps {
                sh '''
            set -e

            echo "HOST WORKSPACE:"
            ls -la /var/jenkins_home/workspace/jobs_uploader

            echo "DOCKER ROOT MOUNT TEST:"
            
            docker run --rm \
              -v /var/jenkins_home/workspace/jobs_uploader:/mnt \
              alpine ls -la /mnt

            echo "CHECK JOBS INSIDE MOUNT:"
            docker run --rm \
              -v /var/jenkins_home/workspace/jobs_uploader:/mnt \
              alpine ls -la /mnt/jobs || true
        '''
            }
        }

        stage('DOCKER DIAG') {
            steps {
                sh '''
            set -e
            echo "DOCKER INFO"
            docker info || true

            echo "WHO AM I"
            whoami

            echo "WORKSPACE REAL"
            ls -la $WORKSPACE

            echo "TRY RAW MOUNT TEST"
            docker run --rm \
              -v "$WORKSPACE:/test" \
              alpine ls -la /test || true
        '''
            }
        }

        stage('REAL HOST PATH') {
            steps {
                sh '''
            echo "JENKINS WORKSPACE INSIDE CONTAINER:"
            pwd

            echo "TRY /proc/self/mounts"
            cat /proc/self/mounts | grep workspace || true

            echo "TRY ENV"
            env | sort | grep WORKSPACE || true
        '''
            }
        }

        stage('RUN JJB') {
            steps {
                sh '''
            set -e

            echo "WORKSPACE=$WORKSPACE"
            ls -la "$WORKSPACE/jobs"

            docker run --rm \
              -v "$WORKSPACE:/workspace" \
              -w /workspace \
              jenkins-agent-python:1.0 bash -c "
                set -e
                echo INSIDE CONTAINER
                pwd
                ls -la /workspace/jobs

                python --version
                jenkins-jobs --version

                jenkins-jobs --conf config.ini update /workspace/jobs/
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