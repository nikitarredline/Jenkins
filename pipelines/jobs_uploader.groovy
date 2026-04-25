pipeline {
    agent any

    environment {
        JJB_CONTAINER = "jenkins-agent-python:1.0"
        WORKSPACE_DIR = "/var/jenkins_home/workspace/jobs_uploader"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Debug workspace') {
            steps {
                sh '''
                    set -e
                    echo "=== WORKSPACE ==="
                    pwd
                    ls -la

                    echo "=== JOBS DIR ==="
                    ls -la jobs || true
                '''
            }
        }

        stage('Generate config.ini') {
            steps {
                withCredentials([string(credentialsId: 'jenkins_pass', variable: 'JENKINS_PASS')]) {
                    sh '''
                        set -e
                        cat > config.ini <<EOF
[job_builder]
keep_descriptions=False
recursive=True

[jenkins]
user=admin
password=$JENKINS_PASS
url=http://jenkins:8080
EOF
                    '''
                }
            }
        }

        stage('Run Jenkins Job Builder') {
            steps {
                sh '''
                    set -e

                    echo "RUNNING JJB INSIDE DOCKER"

                    docker run --rm \
                        -v /var/jenkins_home/workspace/jobs_uploader:/workspace \
                        -w /workspace \
                        jenkins-agent-python:1.0 \
                        bash -c "
                            set -e
                            echo INSIDE CONTAINER
                            pwd
                            ls -la jobs

                            jenkins-jobs --version

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
        success {
            echo "JOB SUCCESS"
        }
        failure {
            echo "JOB FAILED"
        }
    }
}