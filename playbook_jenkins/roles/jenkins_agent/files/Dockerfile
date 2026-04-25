FROM python:3.10-slim

RUN apt-get update && apt-get install -y \
    git curl maven \
    && rm -rf /var/lib/apt/lists/*

RUN pip install jenkins-job-builder==5.0.3

RUN useradd -m -s /bin/bash jenkins

USER jenkins
WORKDIR /home/jenkins
