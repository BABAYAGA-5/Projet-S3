# Complete CI/CD + K8s Deployment Setup

## Prerequisites Setup (One-time)

### 1. Start Minikube
```powershell
# Start Minikube with Docker driver
minikube start --driver=docker

# Enable Ingress
minikube addons enable ingress

# Verify
minikube status
kubectl cluster-info
```

### 2. Configure Jenkins for Kubernetes

**Option A: Configure kubectl context in Jenkins**
1. Jenkins needs access to kubectl and your kubeconfig
2. On Jenkins server/agent:
   ```powershell
   # Copy kubeconfig to Jenkins user
   xcopy /Y %USERPROFILE%\.kube\config C:\ProgramData\Jenkins\.kube\config
   # Or ensure Jenkins runs as user with kubectl access
   ```

**Option B: Install Kubernetes plugin in Jenkins (Recommended)**
1. Install **Kubernetes CLI Plugin** in Jenkins
2. Manage Jenkins → Tools → Add Kubernetes CLI
3. Or use the approach above with direct kubectl commands

---

## Jenkins Pipeline Overview

The complete pipeline now includes:

1. **Checkout** - Clone code from GitHub
2. **Build & Test** - Maven build and package WAR
3. **Docker Build & Push** - Build and push to Docker Hub
4. **Deploy to Kubernetes** - Apply K8s manifests to Minikube
5. **Verify Deployment** - Check pods, services, ingress status

---

## Running the Complete Pipeline

### Step 1: Ensure Minikube is Running
```powershell
minikube status
# If not running: minikube start --driver=docker
```

### Step 2: Trigger Jenkins Build
1. Go to Jenkins → Projet-S3-Pipeline
2. Click **Build Now**
3. Watch the console output for all stages

### Step 3: Access the Deployed App

**Via NodePort (easiest):**
```powershell
minikube service projet-s3 --url
# Open the returned URL in browser
```

**Via Ingress:**
```powershell
# Get Minikube IP
minikube ip

# Add to C:\Windows\System32\drivers\etc\hosts (as Admin):
# <minikube-ip> projet-s3.local

# Access: http://projet-s3.local
```

---

## Manual Kubernetes Commands (if needed)

```powershell
# View all resources
kubectl get all -l app=projet-s3

# Check pod logs
kubectl get pods
kubectl logs <pod-name>

# Restart deployment
kubectl rollout restart deployment/projet-s3

# Delete and redeploy
kubectl delete -f k8s/
kubectl apply -f k8s/

# Port forward for testing
kubectl port-forward service/projet-s3 8082:80
# Access: http://localhost:8082
```

---

## Monitoring Setup (Next Step)

After successful deployment, set up monitoring:

```powershell
# Install Prometheus & Grafana via Helm
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

helm install monitoring prometheus-community/kube-prometheus-stack -n monitoring --create-namespace

# Access Grafana
kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80
# Open: http://localhost:3000
# Login: admin / prom-operator
```

---

## Answers to TP Questions

### Étape 5: Kubernetes

**Q: Combien de pods ont été déployés ?**
- Réponse: 2 pods (configurés dans `deployment.yaml` avec `replicas: 2`)
- Commande: `kubectl get pods -l app=projet-s3`

**Q: Commande et sortie de kubectl get all**
```powershell
kubectl get all -l app=projet-s3
```
Capture d'écran de la sortie à inclure dans le rapport.

**Q: URL de l'application déployée**
- Via NodePort: Résultat de `minikube service projet-s3 --url`
- Via Ingress: http://projet-s3.local (après config hosts)

---

## Troubleshooting

**Pipeline fails at Deploy to Kubernetes stage:**
- Ensure Minikube is running: `minikube status`
- Check Jenkins can access kubectl: Test in Jenkins script console
- Verify kubeconfig is accessible to Jenkins user

**Pods not starting:**
```powershell
kubectl describe pod <pod-name>
kubectl logs <pod-name>
```

**Image pull errors:**
- Ensure Docker image was pushed: Check Docker Hub
- Verify image name matches: `babayaga0/projet-s3:latest`

**Ingress not working:**
```powershell
# Verify ingress controller
kubectl get pods -n ingress-nginx

# Check ingress resource
kubectl describe ingress projet-s3
```

---

## Complete Workflow Summary

1. ✅ Code pushed to GitHub → Jenkins webhook/polling detects
2. ✅ Jenkins builds WAR with Maven
3. ✅ Jenkins builds Docker image
4. ✅ Jenkins pushes image to Docker Hub
5. ✅ Jenkins deploys to Kubernetes cluster (Minikube)
6. ✅ Application accessible via NodePort/Ingress
7. ✅ Monitoring with Prometheus & Grafana (next step)

All automated through one Jenkins pipeline! 🚀
