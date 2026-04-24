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

            sh '''
cat > config.ini <<EOF
[jenkins]
url=http://89.124.113.71/jenkins/
user=$USER
password=$PASS

[job_builder]
recursive=True
keep_descriptions=False
EOF
'''
        }
    }

    stage('Debug host config') {
        sh '''
echo "HOST CHECK:"
ls -la $WORKSPACE
ls -la $WORKSPACE/config.ini
cat $WORKSPACE/config.ini
'''
    }

    stage('Run JJB') {
        sh """
docker run --rm \
  -v ${env.WORKSPACE}:${env.WORKSPACE} \
  -w ${env.WORKSPACE} \
  python:3.10 bash -c '
    set -e

    echo "=== DEBUG ==="
    pwd
    ls -la

    echo "=== CONFIG CHECK ==="
    ls -la config.ini
    cat config.ini

    pip install --no-cache-dir jenkins-job-builder==5.0.3
    jenkins-jobs --version

    jenkins-jobs --conf config.ini update jobs/
  '
"""
    }
}