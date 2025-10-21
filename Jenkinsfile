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
              set MINIKUBE_HOME=C:\\Users\\othma\\.minikube
              set KUBECONFIG=C:\\Users\\othma\\.kube\\config
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- apply -f k8s/deployment.yaml
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- apply -f k8s/service.yaml
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- apply -f k8s/ingress.yaml
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- rollout status deployment/projet-s3 --timeout=5m
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- get all -l app=projet-s3
            """
          }
        }
      }
    }

    stage('Deploy Monitoring Stack') {
      steps {
        script {
          if (isUnix()) {
            sh """
              echo "=== Deploying Prometheus ==="
              minikube kubectl -- apply -f k8s/monitoring/prometheus-config.yaml
              minikube kubectl -- apply -f k8s/monitoring/prometheus-deployment.yaml
              echo "=== Deploying Grafana ==="
              minikube kubectl -- apply -f k8s/monitoring/grafana-deployment.yaml
              echo "=== Deploying SonarQube ==="
              minikube kubectl -- apply -f k8s/monitoring/sonarqube-deployment.yaml
              echo "=== Waiting for monitoring stack to be ready ==="
              minikube kubectl -- wait --for=condition=available --timeout=5m deployment/prometheus
              minikube kubectl -- wait --for=condition=available --timeout=5m deployment/grafana
              minikube kubectl -- wait --for=condition=available --timeout=5m deployment/sonarqube
            """
          } else {
            bat """
              set MINIKUBE_HOME=C:\\Users\\othma\\.minikube
              set KUBECONFIG=C:\\Users\\othma\\.kube\\config
              echo === Deploying Prometheus ===
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- apply -f k8s/monitoring/prometheus-config.yaml
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- apply -f k8s/monitoring/prometheus-deployment.yaml
              echo === Deploying Grafana ===
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- apply -f k8s/monitoring/grafana-deployment.yaml
              echo === Deploying SonarQube ===
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- apply -f k8s/monitoring/sonarqube-deployment.yaml
              echo === Waiting for monitoring stack to be ready ===
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- wait --for=condition=available --timeout=5m deployment/prometheus
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- wait --for=condition=available --timeout=5m deployment/grafana
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- wait --for=condition=available --timeout=5m deployment/sonarqube
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
              echo "=== Application Deployment Status ==="
              minikube kubectl -- get deployment projet-s3
              echo "=== Application Pods ==="
              minikube kubectl -- get pods -l app=projet-s3 -o wide
              echo "=== Application Service ==="
              minikube kubectl -- get service projet-s3
              echo "=== Application Ingress ==="
              minikube kubectl -- get ingress projet-s3
              echo "=== Monitoring Stack Status ==="
              minikube kubectl -- get pods -l app=prometheus
              minikube kubectl -- get pods -l app=grafana
              minikube kubectl -- get pods -l app=sonarqube
              echo "=== All Services ==="
              minikube kubectl -- get svc
            """
          } else {
            bat """
              set MINIKUBE_HOME=C:\\Users\\othma\\.minikube
              set KUBECONFIG=C:\\Users\\othma\\.kube\\config
              echo === Application Deployment Status ===
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- get deployment projet-s3
              echo === Application Pods ===
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- get pods -l app=projet-s3 -o wide
              echo === Application Service ===
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- get service projet-s3
              echo === Application Ingress ===
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- get ingress projet-s3
              echo === Monitoring Stack Status ===
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- get pods -l app=prometheus
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- get pods -l app=grafana
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- get pods -l app=sonarqube
              echo === All Services ===
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- get svc
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
          podCount = bat(script: "@set MINIKUBE_HOME=C:\\Users\\othma\\.minikube && @set KUBECONFIG=C:\\Users\\othma\\.kube\\config && @\"C:\\ProgramData\\chocolatey\\bin\\minikube.exe\" kubectl -- get pods -l app=projet-s3 --no-headers | find /c /v \"\"", returnStdout: true).trim()
        }
        echo "✅ Build successful: ${env.BUILD_TAG}"
        echo "✅ Docker image pushed: ${DOCKER_IMAGE}:${DOCKER_TAG}"
        echo "✅ Deployed to Kubernetes: ${podCount} pods running"
        echo "🌐 Application: minikube service projet-s3 --url"
        echo "📊 Prometheus: http://localhost:30090"
        echo "📈 Grafana: http://localhost:30300 (admin/admin)"
        echo "🔍 SonarQube: http://localhost:30900 (admin/admin)"
      }
    }
    failure {
      echo "❌ Build failed: ${env.BUILD_TAG}"
    }
  }
}
