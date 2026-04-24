pipeline {
    agent any

    environment {
        HOST_WORKSPACE = "/root/jenkins_home/workspace/api_tests"
    }

    stages {

        stage('Checkout') {
            steps {
                git url: 'https://github.com/nikitarredline/RestAssuredHomework', branch: 'main'
            }
        }

        stage('Debug paths') {
            steps {
                sh '''
                    set -e

                    echo "=== JENKINS WORKSPACE ==="
                    echo $WORKSPACE
                    ls -la $WORKSPACE

                    echo "=== HOST PATH ==="
                    ls -la ${HOST_WORKSPACE}

                    echo "=== DOCKER CHECK ==="
                    docker run --rm \
                      -v ${HOST_WORKSPACE}:/workspace \
                      alpine ls -la /workspace
                '''
            }
        }

        stage('Run API tests (Maven in Docker)') {
            steps {
                sh '''
                    set -e

                    docker run --rm \
                      -v ${HOST_WORKSPACE}:/workspace \
                      -w /workspace \
                      maven:3.9.9-eclipse-temurin-17 \
                      mvn clean test
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