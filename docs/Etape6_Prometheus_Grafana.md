# Étape 6 : Prometheus & Grafana – Supervision

## Installation via Helm (Méthode Recommandée)

### 1. Installer Helm

```powershell
# Avec Chocolatey
choco install kubernetes-helm

# OU télécharger depuis: https://github.com/helm/helm/releases
```

### 2. Ajouter le repo Prometheus Community

```powershell
# Ajouter le repository
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts

# Mettre à jour
helm repo update

# Vérifier
helm search repo prometheus
```

### 3. Installer kube-prometheus-stack

```powershell
# Créer le namespace
kubectl create namespace monitoring

# Installer la stack complète (Prometheus + Grafana + Alertmanager)
helm install monitoring prometheus-community/kube-prometheus-stack `
  -n monitoring `
  --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false `
  --set grafana.adminPassword=admin

# Vérifier l'installation
kubectl get pods -n monitoring
kubectl get svc -n monitoring
```

**Note**: L'installation peut prendre 2-3 minutes. Attendez que tous les pods soient `Running`.

---

## Accéder à Grafana

### Option 1: Port-Forward (Recommandé pour local)

```powershell
# Exposer Grafana sur le port 3000
kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80

# Ouvrir dans le navigateur: http://localhost:3000
# Identifiants par défaut:
#   Username: admin
#   Password: admin (ou celui configuré ci-dessus)
```

### Option 2: NodePort (pour accès externe)

```powershell
# Modifier le service en NodePort
kubectl patch svc monitoring-grafana -n monitoring -p '{"spec": {"type": "NodePort"}}'

# Obtenir l'URL
minikube service monitoring-grafana -n monitoring --url
```

---

## Accéder à Prometheus

```powershell
# Port-forward Prometheus
kubectl port-forward -n monitoring svc/monitoring-kube-prometheus-prometheus 9090:9090

# Accès: http://localhost:9090
```

---

## Configuration des Dashboards Grafana

### Dashboards Pré-configurés

Grafana est livré avec plusieurs dashboards par défaut:

1. **Kubernetes / Compute Resources / Namespace (Pods)**
   - ID: Dans Grafana → Dashboards → Browse
   - Affiche: CPU, Mémoire par pod

2. **Kubernetes / Compute Resources / Cluster**
   - Vue globale du cluster

3. **Node Exporter / Nodes**
   - Métriques des nœuds

### Dashboard Personnalisé pour Projet-S3

**Étapes:**

1. **Se connecter à Grafana** (http://localhost:3000)

2. **Créer un nouveau Dashboard**
   - Click **+** → **Dashboard** → **Add visualization**
   - Data source: **Prometheus**

3. **Panel 1: Consommation CPU des Pods**
   ```promql
   # Requête PromQL
   sum(rate(container_cpu_usage_seconds_total{namespace="default", pod=~"projet-s3.*"}[5m])) by (pod)
   ```
   - Type: **Time series** ou **Graph**
   - Titre: "CPU Usage - Projet-S3 Pods"

4. **Panel 2: Consommation Mémoire des Pods**
   ```promql
   # Requête PromQL
   sum(container_memory_usage_bytes{namespace="default", pod=~"projet-s3.*"}) by (pod)
   ```
   - Type: **Time series**
   - Titre: "Memory Usage - Projet-S3 Pods"
   - Unit: **bytes(IEC)**

5. **Panel 3: Disponibilité du Service (Pods Ready)**
   ```promql
   # Requête PromQL
   kube_deployment_status_replicas_available{deployment="projet-s3"}
   ```
   - Type: **Stat**
   - Titre: "Available Pods"

6. **Panel 4: Nombre de Restarts**
   ```promql
   # Requête PromQL
   sum(kube_pod_container_status_restarts_total{namespace="default", pod=~"projet-s3.*"}) by (pod)
   ```
   - Type: **Stat**
   - Titre: "Pod Restarts"

7. **Panel 5: Taux de requêtes HTTP (si Ingress avec metrics)**
   ```promql
   # Requête PromQL (nécessite nginx-ingress metrics)
   rate(nginx_ingress_controller_requests{ingress="projet-s3"}[5m])
   ```
   - Type: **Graph**
   - Titre: "HTTP Request Rate"

8. **Sauvegarder le Dashboard**
   - Click **Save dashboard**
   - Nom: "Projet-S3 Monitoring"

---

## Métriques Principales (KPIs)

### KPI Principal Recommandé: **Disponibilité du Service**

**Pourquoi?**
- Critique pour la production
- Mesure directe de la santé de l'application
- Facile à comprendre et à surveiller

**Formule:**
```promql
(kube_deployment_status_replicas_available{deployment="projet-s3"} / kube_deployment_spec_replicas{deployment="projet-s3"}) * 100
```

**Autres KPIs Importants:**
1. **CPU Utilization** - Prévenir la surcharge
2. **Memory Usage** - Détecter les fuites mémoire
3. **Pod Restart Count** - Identifier les crashs
4. **Response Time** - Performance utilisateur

---

## Configuration des Alertes

### Alertes Recommandées

1. **Pod Non Disponible**
   ```yaml
   # Alerte si moins de 2 pods disponibles
   expr: kube_deployment_status_replicas_available{deployment="projet-s3"} < 2
   for: 5m
   severity: critical
   ```

2. **CPU Élevé**
   ```yaml
   # Alerte si CPU > 80% pendant 5 minutes
   expr: sum(rate(container_cpu_usage_seconds_total{pod=~"projet-s3.*"}[5m])) > 0.8
   for: 5m
   severity: warning
   ```

3. **Mémoire Élevée**
   ```yaml
   # Alerte si mémoire > 90%
   expr: (container_memory_usage_bytes{pod=~"projet-s3.*"} / container_spec_memory_limit_bytes{pod=~"projet-s3.*"}) > 0.9
   for: 5m
   severity: warning
   ```

4. **Restarts Fréquents**
   ```yaml
   # Alerte si plus de 3 restarts en 10 minutes
   expr: rate(kube_pod_container_status_restarts_total{pod=~"projet-s3.*"}[10m]) > 0.3
   for: 10m
   severity: warning
   ```

5. **Pod Not Ready**
   ```yaml
   # Alerte si un pod n'est pas prêt pendant 5 minutes
   expr: kube_pod_status_ready{pod=~"projet-s3.*", condition="true"} == 0
   for: 5m
   severity: critical
   ```

### Créer des Alertes dans Grafana

1. **Grafana → Alerting → Alert rules**
2. Click **New alert rule**
3. Configurer les seuils et conditions
4. Ajouter des notifications (email, Slack, etc.)

---

## Commandes Utiles

```powershell
# Vérifier les métriques collectées
kubectl get servicemonitors -n monitoring

# Voir les pods de monitoring
kubectl get pods -n monitoring

# Logs de Prometheus
kubectl logs -n monitoring -l app.kubernetes.io/name=prometheus

# Logs de Grafana
kubectl logs -n monitoring -l app.kubernetes.io/name=grafana

# Désinstaller la stack
helm uninstall monitoring -n monitoring
kubectl delete namespace monitoring
```

---

## Export du Dashboard

Pour partager ou sauvegarder:

1. **Grafana → Dashboard → Settings (icône ⚙️)**
2. **JSON Model** → Copy to clipboard
3. Sauvegarder dans `docs/grafana-dashboard.json`

---

## Réponses aux Questions

### Question: Capture du dashboard
- Screenshot de Grafana montrant les 4-5 panels configurés
- Inclure: CPU, Mémoire, Pods disponibles, Restarts

### Question: Métrique KPI principale
**KPI: Disponibilité du Service (Service Availability)**

**Raison:**
- Mesure directe de la santé de l'application
- Impact direct sur l'expérience utilisateur
- Facile à comprendre pour tous les stakeholders
- Base pour le calcul du SLA (99.9% uptime, etc.)

**Formule:** `(Pods disponibles / Pods requis) × 100`

### Question: Alertes envisagées
1. **Haute Criticité:**
   - Aucun pod disponible (downtime complet)
   - Moins de 50% des pods disponibles
   
2. **Moyenne Criticité:**
   - CPU > 80% pendant 5 minutes
   - Mémoire > 90% pendant 5 minutes
   - Plus de 3 restarts en 10 minutes

3. **Basse Criticité:**
   - Pod individuel en état non-Ready
   - Latence élevée (si metrics disponibles)

---

## Captures d'écran à fournir

1. Dashboard Grafana avec tous les panels
2. Page Prometheus avec métriques
3. Liste des pods de monitoring (`kubectl get pods -n monitoring`)
4. Configuration d'une alerte
5. Export JSON du dashboard

