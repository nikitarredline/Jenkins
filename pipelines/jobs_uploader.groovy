import groovy.transform.Field

@Field
def CONF_FILE = "./config.ini"

@Field
def JENKINS_HOSTNAME = 'http://144.124.231.59'

@Field
def JOBS_DIR = "./jobs"

node() {

    sh 'docker --version'
    sh 'docker ps'

    currentBuild.description = "<p style='color: red;'>Jobs uploader</p>"

    stage('Checkout') {
        checkout scm
    }

    stage('Create conf.ini') {
        withCredentials([usernamePassword(credentialsId: "jenkins", usernameVariable: "user", passwordVariable: 'pass')]) {
            sh '''
            cat > config.ini << EOF
[jenkins]
url=''' + JENKINS_HOSTNAME + '''/jenkins/
user=$user
password=$pass

[job_builder]
recursive=True
keep_descriptions=False
EOF
            '''
        }
    }

    stage('Run JJB in Docker') {
        docker.image('python:3.11').inside {
            sh '''
                pip install jenkins-job-builder
                jenkins-jobs --conf config.ini --flush-cache update jobs/
            '''
        }
    }
}