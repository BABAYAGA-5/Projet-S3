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
            """
          }
        }
      }
    }

    stage('Setup Grafana Dashboard') {
      steps {
        script {
          if (isUnix()) {
            sh """
              echo "=== Configuring Grafana Dashboard ==="
              sleep 30
              
              # Get Minikube IP
              MINIKUBE_IP=\$(minikube ip)
              GRAFANA_URL="http://\${MINIKUBE_IP}:30300"
              
              echo "Importing dashboard to Grafana at \${GRAFANA_URL}"
              
              # Wait for Grafana API to be ready
              for i in {1..30}; do
                if curl -s \${GRAFANA_URL}/api/health > /dev/null; then
                  echo "Grafana API is ready"
                  break
                fi
                sleep 2
              done
              
              # Import dashboard
              curl -X POST "\${GRAFANA_URL}/api/dashboards/db" \\
                -H "Content-Type: application/json" \\
                -u admin:admin \\
                -d @docs/projet-s3-dashboard.json
              
              echo "Dashboard imported successfully!"
            """
          } else {
            bat """
              set MINIKUBE_HOME=C:\\Users\\othma\\.minikube
              set KUBECONFIG=C:\\Users\\othma\\.kube\\config
              echo === Configuring Grafana Dashboard ===
              
              REM Get Minikube IP
              for /f %%i in ('\"C:\\ProgramData\\chocolatey\\bin\\minikube.exe\" ip') do set MINIKUBE_IP=%%i
              set GRAFANA_URL=http://!MINIKUBE_IP!:30300
              
              echo Waiting for Grafana to be ready...
              timeout /t 30 /nobreak
              
              echo Importing dashboard to Grafana...
              curl -X POST "!GRAFANA_URL!/api/dashboards/db" -H "Content-Type: application/json" -u admin:admin -d @docs/projet-s3-dashboard.json
              
              echo Dashboard imported successfully!
            """
          }
        }
      }
    }

    stage('SonarQube Analysis') {
      steps {
        script {
          if (isUnix()) {
            sh """
              echo "=== Waiting for SonarQube to be ready ==="
              minikube kubectl -- wait --for=condition=available --timeout=10m deployment/sonarqube
              echo "=== SonarQube is ready ==="
              minikube kubectl -- get pods -l app=sonarqube
              echo "Note: Run 'mvn sonar:sonar' with SonarQube token for code analysis"
            """
          } else {
            bat """
              set MINIKUBE_HOME=C:\\Users\\othma\\.minikube
              set KUBECONFIG=C:\\Users\\othma\\.kube\\config
              echo === Waiting for SonarQube to be ready ===
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- wait --for=condition=available --timeout=10m deployment/sonarqube
              echo === SonarQube is ready ===
              "C:\\ProgramData\\chocolatey\\bin\\minikube.exe" kubectl -- get pods -l app=sonarqube
              echo Note: Access SonarQube and configure project for analysis
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
        def minikubeIP = ''
        if (isUnix()) {
          podCount = sh(script: "minikube kubectl -- get pods -l app=projet-s3 --no-headers | wc -l", returnStdout: true).trim()
          minikubeIP = sh(script: "minikube ip", returnStdout: true).trim()
        } else {
          podCount = bat(script: "@set MINIKUBE_HOME=C:\\Users\\othma\\.minikube && @set KUBECONFIG=C:\\Users\\othma\\.kube\\config && @\"C:\\ProgramData\\chocolatey\\bin\\minikube.exe\" kubectl -- get pods -l app=projet-s3 --no-headers | find /c /v \"\"", returnStdout: true).trim()
          minikubeIP = bat(script: "@\"C:\\ProgramData\\chocolatey\\bin\\minikube.exe\" ip", returnStdout: true).trim()
        }
        echo "=========================================="
        echo "✅ BUILD SUCCESSFUL"
        echo "=========================================="
        echo "📦 Docker Image: ${DOCKER_IMAGE}:${DOCKER_TAG}"
        echo "🚀 Kubernetes Pods: ${podCount} pods running"
        echo ""
        echo "🌐 ACCESS YOUR SERVICES:"
        echo "=========================================="
        echo "Application:  http://${minikubeIP}:30081"
        echo "Prometheus:   http://${minikubeIP}:30090"
        echo "Grafana:      http://${minikubeIP}:30300"
        echo "              Username: admin"
        echo "              Password: admin"
        echo "SonarQube:    http://${minikubeIP}:30900"
        echo "              Username: admin"
        echo "              Password: admin"
        echo "=========================================="
        echo ""
        echo "💡 TIP: Use port-forwarding for localhost access:"
        echo "   kubectl port-forward svc/grafana 3000:3000"
        echo "   kubectl port-forward svc/prometheus 9090:9090"
        echo "   kubectl port-forward svc/sonarqube 9000:9000"
        echo "=========================================="
      }
    }
    failure {
      echo "=========================================="
      echo "❌ BUILD FAILED: ${env.BUILD_TAG}"
      echo "=========================================="
      echo "Check the logs above for error details"
    }
  }
}
