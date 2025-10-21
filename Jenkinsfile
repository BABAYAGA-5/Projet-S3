pipeline {
  agent any

  options {
    skipDefaultCheckout(false)
    timestamps()
  }

  environment {
    // Configure these in Jenkins: Manage Jenkins → Credentials
    DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
    // IMPORTANT: Replace 'your-dockerhub-username' with your actual Docker Hub username
    DOCKER_IMAGE = "${env.DOCKER_IMAGE_NAME ?: 'your-dockerhub-username/projet-s3'}"
    DOCKER_TAG = "${env.DOCKER_IMAGE_TAG ?: 'latest'}"
  }

  triggers {
    pollSCM('H/5 * * * *')  // Poll every 5 minutes
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build & Test') {
      steps {
        // Works on both Windows (bat) and Linux (sh) if you have Maven in PATH
        script {
          if (isUnix()) {
            sh 'mvn -version && mvn -B -DskipTests=false clean package'
          } else {
            bat 'mvn -version && mvn -B -DskipTests=false clean package'
          }
        }
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
          archiveArtifacts artifacts: 'target/*.war', fingerprint: true, onlyIfSuccessful: true
        }
      }
    }

    stage('Docker Build & Push') {
      steps {
        script {
          if (isUnix()) {
            sh """
              docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
              echo ${DOCKERHUB_CREDENTIALS_PSW} | docker login -u ${DOCKERHUB_CREDENTIALS_USR} --password-stdin
              docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
            """
          } else {
            bat """
              docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
              echo %DOCKERHUB_CREDENTIALS_PSW% | docker login -u %DOCKERHUB_CREDENTIALS_USR% --password-stdin
              docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
            """
          }
        }
      }
    }
  }

  post {
    success {
      echo "Build successful: ${env.BUILD_TAG}"
    }
    failure {
      echo "Build failed: ${env.BUILD_TAG}"
    }
  }
}
