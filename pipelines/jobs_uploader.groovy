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
        withCredentials([usernamePassword(credentialsId: "jenkins", usernameVariable: "user", passwordVariable: 'pass')]) {
            sh '''
cat > config.ini << EOF
[jenkins]
url=http://89.124.113.71/jenkins/
user=$user
password=$pass

[job_builder]
recursive=True
keep_descriptions=False
EOF
'''
        }
    }

    stage('Debug host') {
        sh '''
echo "WORKSPACE=$WORKSPACE"
ls -la $WORKSPACE
ls -la $WORKSPACE/jobs
ls -la $WORKSPACE/config.ini
'''
    }

    stage('Run JJB') {
        sh '''
docker run --rm \
  -v $WORKSPACE:/workspace \
  -w /workspace \
  python:3.10 bash -c "
    set -e

    echo '=== INSIDE CONTAINER ==='
    pwd
    ls -la
    ls -la config.ini
    ls -la jobs

    echo '=== PYTHON ==='
    python --version

    echo '=== INSTALL ==='
    pip install --no-cache-dir jenkins-job-builder==5.0.3

    echo '=== RUN JJB ==='
    jenkins-jobs --version
    jenkins-jobs --conf config.ini update jobs/
"
'''
    }
}