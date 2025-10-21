# Projet-S3 — CI/CD, Docker, Kubernetes (simple)

This repo includes a minimal setup to build a JEE WAR, analyze it with SonarQube, package it into a Docker image (Tomcat 10.1 + Java 21), and deploy to Kubernetes (Minikube).

- Jenkins pipeline: `Jenkinsfile`
- SonarQube config: `sonar-project.properties`
- Docker: `Dockerfile`, `.dockerignore`
- Kubernetes: `k8s/` (deployment, service, ingress)
- Full step-by-step guide: `docs/TP_Global_DevOps.md`

Quick start (local Docker):
```powershell
$env:DOCKER_BUILDKIT=1
$IMAGE="your-dockerhub-username/projet-s3:1.0"

docker build -t $IMAGE .
# Use port 8081 (Jenkins uses 8080)
docker run --rm -p 8081:8080 $IMAGE
# http://localhost:8081/
```
