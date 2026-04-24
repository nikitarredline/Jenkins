import groovy.transform.Field

@Field
def CONF_FILE = "./config.ini"

@Field
def JENKINS_HOSTNAME = 'http://89.124.113.71'

@Field
def JOBS_DIR = "./jobs"

node() {

    currentBuild.description = "<p style='color: red;'>Jobs uploader</p>"

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
    pwd
    ls -la
    ls -R
    '''
    }

    stage('Check host Python') {
        sh '''
        echo "=== HOST PYTHON ==="
        python3 --version || python --version
        which python3 || which python
    '''
    }

    stage('Debug workspace') {
        sh '''
        echo "WORKSPACE=$WORKSPACE"
        ls -la $WORKSPACE
        ls -la $WORKSPACE/jobs || true
    '''
    }

    stage('Run JJB') {
        sh """
    docker run --rm \
      -v ${env.WORKSPACE}:/workspace \
      -w /workspace \
      python:3.10 \
      bash -c '
        set -e
        pwd
        ls -R
        pip install --upgrade pip
        pip install jenkins-job-builder==5.0.3
        jenkins-jobs --version
        jenkins-jobs --conf config.ini update jobs/
      '
    """
    }
}