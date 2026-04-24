pipeline {
    agent any

    stages {

        stage('DEBUG NODE') {
            steps {
                sh '''
                    set -e
                    hostname
                    echo "WORKSPACE=$WORKSPACE"
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
                        cat config.ini
                    '''
                }
            }
        }

        stage('Host debug') {
            steps {
                sh '''
                    set -e
                    echo "HOST DEBUG"
                    echo "WORKSPACE=$WORKSPACE"
                    ls -la $WORKSPACE
                    ls -la $WORKSPACE/jobs
                '''
            }
        }

        stage('Run Jenkins Job Builder') {
            steps {
                sh '''
            set -e

            echo "=== DOCKER MOUNT RESOLUTION ==="

            CONTAINER_ID=$(hostname)

            echo "JENKINS CONTAINER: $CONTAINER_ID"

            HOST_WS=$(docker inspect $CONTAINER_ID \
                --format='{{ range .Mounts }}{{ if eq .Destination "/var/jenkins_home" }}{{ .Source }}{{ end }}{{ end }}')/workspace/jobs_uploader

            echo "RESOLVED HOST_WS=$HOST_WS"

            if [ ! -d "$HOST_WS" ]; then
                echo "ERROR: workspace not found on host"
                exit 1
            fi

            ls -la "$HOST_WS"

            echo "=== RUN DOCKER ==="

            docker run --rm \
              -v "$HOST_WS:/workspace" \
              -w /workspace \
              python:3.10 bash -c '
                set -e
                echo "INSIDE CONTAINER"
                ls -la

                echo "CONFIG CHECK"
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
                ls -la $WORKSPACE
            '''
        }
    }
}