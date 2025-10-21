# Étape 5 : Kubernetes – Déploiement sur Minikube

## Installation de Minikube (Windows)

### Prérequis
- Docker Desktop installé et en cours d'exécution
- Hyper-V ou VirtualBox (Docker Desktop recommandé comme driver)

### Installation

**Option 1: Avec Chocolatey (recommandé)**
```powershell
choco install minikube
```

**Option 2: Installation manuelle**
1. Télécharger depuis: https://minikube.sigs.k8s.io/docs/start/
2. Renommer en `minikube.exe`
3. Ajouter au PATH système

### Installer kubectl (si pas déjà installé)
```powershell
choco install kubernetes-cli
# OU
curl.exe -LO "https://dl.k8s.io/release/v1.28.0/bin/windows/amd64/kubectl.exe"
```

---

## Démarrage de Minikube

```powershell
# Démarrer Minikube avec Docker driver
minikube start --driver=docker

# Vérifier le statut
minikube status

# Activer l'Ingress controller
minikube addons enable ingress

# Vérifier que tout fonctionne
kubectl cluster-info
kubectl get nodes
```

---

## Déploiement de l'Application

### 1. Appliquer les manifests Kubernetes

```powershell
cd D:\DevOps\Projet-S3

# Déployer l'application
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml

# Vérifier le déploiement
kubectl get deployments
kubectl get pods
kubectl get services
kubectl get ingress
```

### 2. Réponses aux Questions

**Question: Combien de pods ont été déployés ?**
```powershell
kubectl get pods -l app=projet-s3
# Réponse: 2 réplicas configurées dans deployment.yaml
```

**Question: Commande et sortie de kubectl get all**
```powershell
kubectl get all -l app=projet-s3

# Exemple de sortie attendue:
# NAME                             READY   STATUS    RESTARTS   AGE
# pod/projet-s3-xxxxxxxxxx-xxxxx   1/1     Running   0          2m
# pod/projet-s3-xxxxxxxxxx-xxxxx   1/1     Running   0          2m
#
# NAME                TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)        AGE
# service/projet-s3   NodePort   10.96.xxx.xxx   <none>        80:30081/TCP   2m
#
# NAME                        READY   UP-TO-DATE   AVAILABLE   AGE
# deployment.apps/projet-s3   2/2     2            2           2m
#
# NAME                                   DESIRED   CURRENT   READY   AGE
# replicaset.apps/projet-s3-xxxxxxxxxx   2         2         2       2m
```

**Commande détaillée pour logs et debug:**
```powershell
# Voir les pods en détail
kubectl get pods -o wide

# Voir les logs d'un pod
kubectl logs <pod-name>

# Décrire le déploiement
kubectl describe deployment projet-s3
```

### 3. Accéder à l'Application

**Option A: Via NodePort**
```powershell
# Obtenir l'URL du service
minikube service projet-s3 --url

# Accéder via le navigateur à l'URL retournée
# Exemple: http://127.0.0.1:xxxxx
```

**Option B: Via Ingress**
```powershell
# Obtenir l'IP de Minikube
minikube ip

# Ajouter à C:\Windows\System32\drivers\etc\hosts (en tant qu'Admin)
# <minikube-ip> projet-s3.local

# Exemple: 192.168.49.2 projet-s3.local

# Accéder via: http://projet-s3.local
```

**Question: URL de l'application déployée**
- **Via NodePort**: Sortie de `minikube service projet-s3 --url`
- **Via Ingress**: http://projet-s3.local (après configuration hosts)

---

## Commandes Utiles

```powershell
# Voir tous les objets K8s
kubectl get all

# Surveiller les pods en temps réel
kubectl get pods -w

# Port-forward direct (alternative)
kubectl port-forward service/projet-s3 8082:80
# Accès: http://localhost:8082

# Supprimer le déploiement
kubectl delete -f k8s/

# Redémarrer Minikube
minikube stop
minikube start
```

---

## Troubleshooting

**Si les pods ne démarrent pas:**
```powershell
# Voir les événements
kubectl get events --sort-by=.metadata.creationTimestamp

# Décrire un pod problématique
kubectl describe pod <pod-name>

# Voir les logs
kubectl logs <pod-name>
```

**Si l'image n'est pas trouvée:**
```powershell
# Vérifier que l'image est sur Docker Hub
docker search babayaga0/projet-s3

# Pull manuel pour tester
docker pull babayaga0/projet-s3:latest
```

**Si Ingress ne fonctionne pas:**
```powershell
# Vérifier l'addon
minikube addons list | findstr ingress

# Vérifier le controller
kubectl get pods -n ingress-nginx
```

---

## Captures d'écran à fournir

1. Sortie de `kubectl get all -l app=projet-s3`
2. Sortie de `kubectl get pods -o wide`
3. Sortie de `minikube service projet-s3 --url`
4. Screenshot de l'application dans le navigateur
5. Sortie de `kubectl describe deployment projet-s3`

