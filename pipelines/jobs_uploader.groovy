import groovy.transform.Field

@Field
def JENKINS_URL = 'http://89.124.113.71/jenkins/'

node() {

    currentBuild.description = "<p style='color: red;'>Jobs uploader (JJB)</p>"

    stage('Checkout') {
        checkout scm
    }

    stage('Create config.ini') {
        withCredentials([usernamePassword(
                credentialsId: "jenkins",
                usernameVariable: "USER",
                passwordVariable: "PASS"
        )]) {

            sh """
cat > config.ini <<EOF
[jenkins]
url=${JENKINS_URL}
user=${USER}
password=${PASS}

[job_builder]
recursive=True
keep_descriptions=False
EOF
"""
        }
    }

    stage('Debug workspace (host)') {
        sh '''
            echo "=== HOST WORKSPACE ==="
            pwd
            ls -R
            echo "=== JOBS DIR ==="
            ls -la jobs
        '''
    }

    stage('Run Jenkins Job Builder') {
        sh """
docker run --rm \
  -v ${env.WORKSPACE}:/workspace \
  -w /workspace \
  python:3.10 bash -c '
    set -e

    echo "=== INSIDE CONTAINER ==="
    pwd
    ls -R

    echo "=== PYTHON VERSION ==="
    python --version

    echo "=== INSTALL JJB ==="
    pip install --no-cache-dir jenkins-job-builder==5.0.3

    echo "=== JJB VERSION ==="
    jenkins-jobs --version

    echo "=== VALIDATE CONFIG ==="
    cat config.ini

    echo "=== RUN UPDATE ==="
    jenkins-jobs --conf config.ini update jobs/
  '
"""
    }
}