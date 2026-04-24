pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                git url: 'https://github.com/nikitarredline/RestAssuredHomework',
                        branch: 'main'
            }
        }

        stage('Debug workspace') {
            steps {
                sh '''
                    set -e

                    echo "=== WORKSPACE (Jenkins) ==="
                    echo $WORKSPACE
                    ls -la $WORKSPACE

                    echo "=== CHECK POM ==="
                    find $WORKSPACE -name pom.xml || true
                '''
            }
        }

        stage('Run API tests (Docker Maven)') {
            steps {
                sh '''
                    set -e

                    echo "RUNNING MAVEN IN DOCKER"

                    docker run --rm \
                        -v $WORKSPACE:/app \
                        -w /app \
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

        failure {
            echo "BUILD FAILED"
        }

        success {
            echo "BUILD SUCCESS"
        }
    }
}