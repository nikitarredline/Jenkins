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
url=http://144.124.231.59/jenkins/
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
        apt-get update -y
        apt-get install -y python3 python3-pip python3-venv

        python3 -m venv venv
        . venv/bin/activate

        pip install --upgrade pip setuptools wheel
        pip install jenkins-job-builder

        jenkins-jobs --conf config.ini update jobs/
    '''
    }
}