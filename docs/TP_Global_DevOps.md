# TP Global DevOps — Projet-S3 (simple version)

This document captures the full CI/CD, containerization, K8s deployment, and monitoring setup for the JEE project `Projet-S3`.

> Environment: Windows + PowerShell. Replace placeholders like `your-dockerhub-username`.

---

## 1) GitHub — Gestion du code source

- Repository: https://github.com/BABAYAGA-5/Projet-S3 (main)
- Branching model (simple Git Flow):
  - `main`: stable/prod
  - `develop`: integration
  - `feature/*`: short-lived feature branches merged into `develop` via PR

Questions:
- Convention de branches: Git Flow simplifié (main, develop, feature/*)
- Historique de commits et politique de merge: via PRs (squash or merge-commit). Provide screenshot from GitHub.

Commands (optional):
```powershell
# Create develop and a sample feature branch
git checkout -b develop
git push --set-upstream origin develop

git checkout -b feature/initial-ci
# ... commit changes ...
git push --set-upstream origin feature/initial-ci
```

---

## 2) Jenkins — Intégration Continue

- Jenkinsfile at repo root defines stages: Checkout, Build & Test, SonarQube Analysis, Docker Build & Push.
- Prereqs on Jenkins controller/agent:
  - Tools: JDK21 (Temurin), Maven 3.x, Docker CLI
  - Plugins: Pipeline, Git, SonarQube Scanner for Jenkins, Credentials Binding, Docker Pipeline
  - Credentials:
    - `sonarqube-server-token` (Secret Text) OR configure server named `SonarQubeServer`
    - `dockerhub-creds` (Username/Password)

Trigger: configured with `pollSCM('@daily')` (change to webhooks or multibranch for auto build on push).

Minimal stages:
1) Clone repo — Jenkins builtin
2) Compile + tests — `mvn clean package`
3) Tests — Surefire reports archived
4) Package — WAR archived
5) SonarQube — `mvn sonar:sonar`
6) Docker — build and push `your-dockerhub-username/projet-s3:latest`

Questions:
- Déclenchement auto: yes if multibranch/webhook is set; with current config, polling daily (adjust as needed)
- Étapes exécutées + résultats: visible in Jenkins Console Output (capture required)
- Log du build: attach Jenkins build log screenshot

---

## 3) SonarQube — Qualité du code

- `sonar-project.properties` provided
- In Jenkins: Configure `SonarQubeServer` and run the Sonar stage
- Fix at least 3 issues reported by Sonar (to be done after first analysis)

Questions:
- Score de qualité: add screenshot of Quality Gate & summary
- Anomalies corrigées: list the 3+ issues fixed (e.g., dead code, unused imports, SQL hardcoding)
- Rapport Sonar: attach screenshots/links

---

## 4) Docker — Containerisation

Files:
- `Dockerfile` (multi-stage: Maven build then Tomcat 10.1 + Java 21)
- `.dockerignore`

Build & push:
```powershell
$env:DOCKER_BUILDKIT=1
$IMAGE="your-dockerhub-username/projet-s3:1.0"

docker build -t $IMAGE .
docker login
docker push $IMAGE
```

Run locally:
```powershell
# Note: Use port 8081 since Jenkins uses 8080
docker run --rm -p 8081:8080 --name projet-s3 your-dockerhub-username/projet-s3:1.0
# Open http://localhost:8081/
```

Questions:
- Contenu du Dockerfile: see file in repo root
- Nom/version de l’image: `your-dockerhub-username/projet-s3:1.0` (and `latest`)
- Commande de lancement: see above `docker run`

---

## 5) Kubernetes — Déploiement (Minikube)

Files in `k8s/`:
- `deployment.yaml` — 2 replicas, container port 8080, probes
- `service.yaml` — NodePort 30080 -> 8080
- `ingress.yaml` — host `projet-s3.local` (requires NGINX Ingress Controller)

Minikube quickstart:
```powershell
minikube start
minikube addons enable ingress

kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml

# Access with NodePort
minikube service projet-s3 --url

# Or via Ingress (add hosts entry)
# Add to C:\Windows\System32\drivers\etc\hosts
# 127.0.0.1 projet-s3.local
```

Questions:
- Pods déployés: 2 replicas (see output below)
- `kubectl get all` sample:
```powershell
kubectl get all -l app=projet-s3
```
- URL de l’app: from `minikube service` output or http://projet-s3.local/

---

## 6) Prometheus & Grafana — Supervision (simple)

Install kube-prometheus-stack via Helm:
```powershell
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm install monitoring prometheus-community/kube-prometheus-stack -n monitoring --create-namespace
```

Access Grafana:
```powershell
kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80
# Open http://localhost:3000 (admin / prom-operator)
```

Dashboard contents (suggested):
- CPU/Memory per pod: `Kubernetes / Compute Resources / Namespace (Pods)`
- Service availability: use builtin `Kubernetes / API server` or create a simple uptime panel from `kube_endpoint_address_available`
- Avg response time (optional – if app exposes metrics): otherwise show request rate via NGINX Ingress metrics

Questions:
- Capture du dashboard: add screenshot
- KPI principale: pod availability (or error rate) — simple and robust
- Alertes envisagées: High pod restarts, high 5xx rate, CPU > 80% for 5m, memory > 90%, pod not ready > 5m

---

## 7) Rapport final & Vidéo

Include:
- Short project description
- Pipeline steps with screenshots
- Configs: Jenkinsfile, Dockerfile, k8s YAML snippets
- Grafana dashboards screenshots
- Answers to questions (sections above)
- Short video walk-through (record: build -> image push -> minikube deploy -> dashboard)

---

## Notes & Assumptions

- Packaging is WAR; runtime is Tomcat 10.1 (Jakarta EE 10 compatible)
- Database settings (MySQL) are present in `src/main/resources/DAO.properties`; for a quick demo you can disable DB-backed features or provide a dev MySQL
- Windows PowerShell commands are provided; adapt where Jenkins agents run Linux
