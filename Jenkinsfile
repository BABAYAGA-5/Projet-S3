pipeline {
  agent any

  options {
    skipDefaultCheckout(false)
    timestamps()
  }

  environment {
    DOCKER_IMAGE = "${env.DOCKER_IMAGE_NAME ?: 'babayaga0/projet-s3'}"
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
          docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-creds') {
            def app = docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
            app.push()
          }
        }
      }
    }

    stage('Deploy to Kubernetes') {
      steps {
        script {
          if (isUnix()) {
            sh """
              minikube kubectl -- apply -f k8s/deployment.yaml
              minikube kubectl -- apply -f k8s/service.yaml
              minikube kubectl -- apply -f k8s/ingress.yaml
              minikube kubectl -- rollout status deployment/projet-s3 --timeout=5m
              minikube kubectl -- get all -l app=projet-s3
            """
          } else {
            bat """
              minikube kubectl -- apply -f k8s/deployment.yaml
              minikube kubectl -- apply -f k8s/service.yaml
              minikube kubectl -- apply -f k8s/ingress.yaml
              minikube kubectl -- rollout status deployment/projet-s3 --timeout=5m
              minikube kubectl -- get all -l app=projet-s3
            """
          }
        }
      }
    }

    stage('Verify Deployment') {
      steps {
        script {
          if (isUnix()) {
            sh """
              echo "=== Deployment Status ==="
              minikube kubectl -- get deployment projet-s3
              echo "=== Pods ==="
              minikube kubectl -- get pods -l app=projet-s3 -o wide
              echo "=== Service ==="
              minikube kubectl -- get service projet-s3
              echo "=== Ingress ==="
              minikube kubectl -- get ingress projet-s3
            """
          } else {
            bat """
              echo === Deployment Status ===
              minikube kubectl -- get deployment projet-s3
              echo === Pods ===
              minikube kubectl -- get pods -l app=projet-s3 -o wide
              echo === Service ===
              minikube kubectl -- get service projet-s3
              echo === Ingress ===
              minikube kubectl -- get ingress projet-s3
            """
          }
        }
      }
    }
  }

  post {
    success {
      script {
        def podCount = ''
        if (isUnix()) {
          podCount = sh(script: "minikube kubectl -- get pods -l app=projet-s3 --no-headers | wc -l", returnStdout: true).trim()
        } else {
          podCount = bat(script: "@minikube kubectl -- get pods -l app=projet-s3 --no-headers | find /c /v \"\"", returnStdout: true).trim()
        }
        echo "✅ Build successful: ${env.BUILD_TAG}"
        echo "✅ Docker image pushed: ${DOCKER_IMAGE}:${DOCKER_TAG}"
        echo "✅ Deployed to Kubernetes: ${podCount} pods running"
        echo "🌐 Access via: minikube service projet-s3 --url"
      }
    }
    failure {
      echo "❌ Build failed: ${env.BUILD_TAG}"
    }
  }
}
