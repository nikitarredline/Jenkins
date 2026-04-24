import groovy.transform.Field

@Field
def JOBS_DIR = "jobs"

node() {

    stage('Checkout') {
        checkout([
                $class: 'GitSCM',
                branches: [[name: '*/main']],
                userRemoteConfigs: [[url: 'https://github.com/nikitarredline/Jenkins']]
        ])
    }

    stage('Create config.ini') {
        withCredentials([usernamePassword(
                credentialsId: "jenkins",
                usernameVariable: "USER",
                passwordVariable: "PASS"
        )]) {
            sh '''
cat > config.ini << EOF
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

    stage('Host debug') {
        sh '''
echo "=== HOST INFO ==="
echo "WORKSPACE=$WORKSPACE"

ls -la $WORKSPACE
ls -la $WORKSPACE/jobs
ls -la $WORKSPACE/config.ini
'''
    }

    stage('Run JJB') {
        sh '''
    set -e

    echo "WORKSPACE=$WORKSPACE"
    ls -la "$WORKSPACE"

    docker run --rm \
      -v "$WORKSPACE:$WORKSPACE" \
      -w "$WORKSPACE" \
      python:3.10 bash -c '
        set -e

        echo "=== INSIDE ==="
        pwd
        ls -la config.ini
        ls -la jobs

        python --version
        pip install --no-cache-dir jenkins-job-builder==5.0.3

        jenkins-jobs --version
        jenkins-jobs --conf config.ini update jobs/
      '
    '''
    }
}