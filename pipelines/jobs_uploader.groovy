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
        apt-get update -y
        apt-get install -y git curl build-essential libssl-dev zlib1g-dev \
            libbz2-dev libreadline-dev libsqlite3-dev wget llvm \
            libncursesw5-dev xz-utils tk-dev libxml2-dev libxmlsec1-dev libffi-dev liblzma-dev

        curl https://pyenv.run | bash

        export PATH="$HOME/.pyenv/bin:$PATH"
        eval "$(pyenv init -)"
        eval "$(pyenv virtualenv-init -)"

        pyenv install 3.10.14
        pyenv global 3.10.14

        pip install --upgrade pip setuptools wheel
        pip install jenkins-job-builder==5.0.3

        jenkins-jobs --conf config.ini update jobs/
    '''
    }
}