import groovy.transform.Field

@Field
def CONF_FILE = "./config.ini"

@Field
def JENKINS_HOSTNAME = 'http://144.124.231.59'

@Field
def JOBS_DIR = "./jobs"

node() {
    currentBuild.description = "<p style='color: red;'>Jobs uploader</p"

    stage('Checkout') {
        checkout scm
    }

    stage('Create conf.ini') {
        withCredentials([usernamePassword(credentialsId: "jenkins", usernameVariable: "user", passwordVariable: 'pass')]) {
            sh """
            cat > $CONF_FILE << EOF
[jenkins]
url=$JENKINS_HOSTNAME/jenkins/
user=$user
password=$pass

[job_builder]
recursive=True
keep_descriptions=False
EOF"""
        }
    }

    stage('Deploy jobs to jenkins') {
        sh "jenkins-jobs --conf $CONF_FILE --flush-cache update $JOBS_DIR"
    }
}
