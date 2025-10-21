pipeline {
  agent any

  options {
    skipDefaultCheckout(false)
    timestamps()
  }

  environment {
    DOCKER_IMAGE = "${env.DOCKER_IMAGE_NAME ?: 'babayaga0/projet-s3'}"
    DOCKER_TAG = "${env.DOCKER_IMAGE_TAG ?: 'latest'}"
    // Use Jenkins kubeconfig path
    // Windows: C:\ProgramData\Jenkins\.kube\config
    // Linux: ${HOME}/.kube/config
    KUBECONFIG = 'C:\\ProgramData\\Jenkins\\.kube\\config'
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

    stage('Configure Kubernetes') {
      steps {
        script {
          if (isUnix()) {
            sh '''
              echo "=== Environment Check ==="
              echo "KUBECONFIG: ${KUBECONFIG}"
              echo "=== Updating Minikube context ==="
              minikube update-context
              echo "=== Checking kubectl configuration ==="
              kubectl config get-contexts
              kubectl config current-context
              kubectl config use-context minikube
              echo "=== Verifying cluster connection ==="
              kubectl cluster-info
              kubectl get nodes
            '''
          } else {
            bat '''
              echo === Environment Check ===
              echo KUBECONFIG: %KUBECONFIG%
              echo === Updating Minikube context ===
              minikube update-context
              echo === Checking kubectl configuration ===
              kubectl config get-contexts
              kubectl config current-context
              kubectl config use-context minikube
              echo === Verifying cluster connection ===
              kubectl cluster-info
              kubectl get nodes
            '''
          }
        }
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

    // Run SonarQube analysis (requires Jenkins credential 'sonar-token')
    stage('SonarQube Analysis (CI)') {
      steps {
        script {
          withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
            if (isUnix()) {
              sh "mvn -B sonar:sonar -Dsonar.login=${SONAR_TOKEN} -Dsonar.host.url=http://sonarqube:9000"
            } else {
              bat "mvn -B sonar:sonar -Dsonar.login=%SONAR_TOKEN% -Dsonar.host.url=http://sonarqube:9000"
            }
          }
        }
      }
      post {
        failure {
          echo "SonarQube analysis failed. Ensure Jenkins has credential id 'sonar-token' with a valid token."
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
              echo "=== Ensuring correct context ==="
              kubectl config use-context minikube
              
              echo "=== Verifying cluster connectivity ==="
              kubectl cluster-info || (echo "ERROR: Cannot connect to Kubernetes cluster" && exit 1)
              kubectl get nodes
              
              echo "=== Applying Kubernetes manifests ==="
              kubectl apply -f k8s/deployment.yaml
              kubectl apply -f k8s/service.yaml
              kubectl apply -f k8s/ingress.yaml
              
              echo "=== Waiting for deployment to be ready ==="
              kubectl rollout status deployment/projet-s3 --timeout=5m
              
              echo "=== Deployment verification ==="
              kubectl get all -l app=projet-s3
            """
          } else {
            bat """
              echo === Ensuring correct context ===
              kubectl config use-context minikube
              
              echo === Verifying cluster connectivity ===
              kubectl cluster-info || (echo ERROR: Cannot connect to Kubernetes cluster && exit /b 1)
              kubectl get nodes
              
              echo === Applying Kubernetes manifests ===
              kubectl apply -f k8s/deployment.yaml
              kubectl apply -f k8s/service.yaml
              kubectl apply -f k8s/ingress.yaml
              
              echo === Waiting for deployment to be ready ===
              kubectl rollout status deployment/projet-s3 --timeout=5m
              
              echo === Deployment verification ===
              kubectl get all -l app=projet-s3
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
              kubectl apply -f k8s/monitoring/prometheus-config.yaml
              kubectl apply -f k8s/monitoring/prometheus-deployment.yaml
              echo "=== Deploying Grafana ==="
              kubectl apply -f k8s/monitoring/grafana-deployment.yaml
              echo "=== Deploying SonarQube ==="
              kubectl apply -f k8s/monitoring/sonarqube-deployment.yaml
              echo "=== Waiting for monitoring stack to be ready ==="
              kubectl wait --for=condition=available --timeout=5m deployment/prometheus
              kubectl wait --for=condition=available --timeout=5m deployment/grafana
            """
          } else {
            bat """
              echo === Deploying Prometheus ===
              kubectl apply -f k8s/monitoring/prometheus-config.yaml
              kubectl apply -f k8s/monitoring/prometheus-deployment.yaml
              echo === Deploying Grafana ===
              kubectl apply -f k8s/monitoring/grafana-deployment.yaml
              echo === Deploying SonarQube ===
              kubectl apply -f k8s/monitoring/sonarqube-deployment.yaml
              echo === Waiting for monitoring stack to be ready ===
              kubectl wait --for=condition=available --timeout=5m deployment/prometheus
              kubectl wait --for=condition=available --timeout=5m deployment/grafana
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
              echo === Configuring Grafana Dashboard ===
              
              REM Wait 30 seconds for Grafana to be ready
              echo Waiting 30 seconds for Grafana to be ready...
              ping 127.0.0.1 -n 31 > nul
              
              REM Check Grafana health from inside cluster
              echo Checking Grafana health...
              kubectl run grafana-health-check --image=curlimages/curl:latest --rm -i --restart=Never -- curl -s http://grafana.default.svc.cluster.local:3000/api/health
              
              REM Copy dashboard JSON to Grafana pod
              echo Copying dashboard JSON to Grafana pod...
              kubectl get pod -l app=grafana -o jsonpath="{.items[0].metadata.name}" > pod_name.txt
              set /p GRAFANA_POD=<pod_name.txt
              echo Grafana pod: %GRAFANA_POD%
              
              kubectl cp docs/projet-s3-dashboard.json %GRAFANA_POD%:/tmp/dashboard.json
              
              REM Import dashboard from inside the pod
              echo Importing dashboard to Grafana API...
              kubectl exec %GRAFANA_POD% -- curl -X POST "http://localhost:3000/api/dashboards/db" -H "Content-Type: application/json" -u admin:admin -d @/tmp/dashboard.json
              
              echo Dashboard import completed!
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
              kubectl wait --for=condition=available --timeout=10m deployment/sonarqube
              echo "=== SonarQube is ready ==="
              kubectl get pods -l app=sonarqube
              echo "Note: Run 'mvn sonar:sonar' with SonarQube token for code analysis"
            """
          } else {
            bat """
              echo === Waiting for SonarQube to be ready ===
              kubectl wait --for=condition=available --timeout=10m deployment/sonarqube
              echo === SonarQube is ready ===
              kubectl get pods -l app=sonarqube
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
              echo === Application Deployment Status ===
              kubectl get deployment projet-s3
              echo === Application Pods ===
              kubectl get pods -l app=projet-s3 -o wide
              echo === Application Service ===
              kubectl get service projet-s3
              echo === Application Ingress ===
              kubectl get ingress projet-s3
              echo === Monitoring Stack Status ===
              kubectl get pods -l app=prometheus
              kubectl get pods -l app=grafana
              kubectl get pods -l app=sonarqube
              echo === All Services ===
              kubectl get svc
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
          podCount = sh(script: "kubectl get pods -l app=projet-s3 --no-headers | wc -l", returnStdout: true).trim()
        } else {
          podCount = bat(script: "@kubectl get pods -l app=projet-s3 --no-headers | find /c /v \"\"", returnStdout: true).trim()
        }
        echo "=========================================="
        echo "✅ BUILD SUCCESSFUL"
        echo "=========================================="
        echo "📦 Docker Image: ${DOCKER_IMAGE}:${DOCKER_TAG}"
        echo "🚀 Kubernetes Pods: ${podCount} pods running"
        echo ""
        echo "🌐 ACCESS YOUR SERVICES:"
        echo "=========================================="
        echo "Application:  http://localhost:30081"
        echo "Prometheus:   http://localhost:30090"
        echo "Grafana:      http://localhost:30300"
        echo "              Username: admin"
        echo "              Password: admin"
        echo "SonarQube:    http://localhost:30900"
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
