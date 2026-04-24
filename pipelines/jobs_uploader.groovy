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

        stage('Run JJB') {
            steps {
                sh '''
            set -e

            echo "=== FIND HOST MOUNT ==="

            CONTAINER_ID=$(hostname)

            HOST_BASE=$(docker inspect $CONTAINER_ID \
              --format='{{ range .Mounts }}{{ if eq .Destination "/var/jenkins_home" }}{{ .Source }}{{ end }}{{ end }}')

            echo "HOST_BASE=$HOST_BASE"

            HOST_WS="$HOST_BASE/workspace/jobs_uploader"

            echo "HOST_WS=$HOST_WS"

            if [ ! -d "$HOST_WS" ]; then
                echo "ERROR: not found on host"
                ls -la "$HOST_BASE/workspace"
                exit 1
            fi

            echo "=== RUN DOCKER ==="

            docker run --rm \
              -v "$HOST_WS:/workspace" \
              -w /workspace \
              python:3.10 bash -c '
                set -e
                echo "INSIDE"
                ls -la

                echo "CONFIG"
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