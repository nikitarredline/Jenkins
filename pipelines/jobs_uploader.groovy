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
        checkout scm
    }

    stage('Create conf.ini') {
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

    stage('Run JJB') {
        sh '''
        docker run --rm \
          -v $WORKSPACE:/workspace \
          -w /workspace \
          python:3.11 bash -c "
            pip install --upgrade pip setuptools wheel &&
            pip install jenkins-job-builder==5.0.3 &&
            jenkins-jobs --conf config.ini update jobs/
          "
    '''
    }
}