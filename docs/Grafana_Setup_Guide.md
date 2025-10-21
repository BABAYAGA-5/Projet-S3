# Grafana Dashboard Setup Guide

## Access Grafana

1. **URL**: http://localhost:3000
2. **Login**:
   - Username: `admin`
   - Password: `admin`

## Datasource Configuration (Auto-configured)

Prometheus datasource is automatically configured at: `http://prometheus:9090`

## Import Pre-built Kubernetes Dashboards

### Method 1: Import from Grafana.com

1. Click the **"+"** icon in left menu → **Import**
2. Enter one of these dashboard IDs:
   - **315** - Kubernetes cluster monitoring
   - **6417** - Kubernetes Cluster (Prometheus)
   - **8588** - Kubernetes Deployment Statefulset Daemonset metrics
   - **12114** - Kubernetes Monitoring
3. Click **Load**
4. Select **Prometheus** as the datasource
5. Click **Import**

### Method 2: Create Custom Dashboard

1. Click **"+"** → **Dashboard** → **Add new panel**
2. Use these queries:

#### CPU Usage by Pod
```promql
sum(rate(container_cpu_usage_seconds_total{namespace="default",pod=~"projet-s3.*"}[5m])) by (pod)
```

#### Memory Usage by Pod
```promql
sum(container_memory_usage_bytes{namespace="default",pod=~"projet-s3.*"}) by (pod)
```

#### Pod Count
```promql
count(kube_pod_info{namespace="default",pod=~"projet-s3.*"})
```

#### Network Traffic (Received)
```promql
sum(rate(container_network_receive_bytes_total{namespace="default",pod=~"projet-s3.*"}[5m])) by (pod)
```

#### Network Traffic (Transmitted)
```promql
sum(rate(container_network_transmit_bytes_total{namespace="default",pod=~"projet-s3.*"}[5m])) by (pod)
```

#### Pod Restarts
```promql
sum(kube_pod_container_status_restarts_total{namespace="default",pod=~"projet-s3.*"}) by (pod)
```

## Useful Dashboards from Grafana.com

- **Node Exporter Full**: Dashboard ID 1860
- **Kubernetes / Compute Resources / Cluster**: Dashboard ID 7249
- **Kubernetes / Compute Resources / Namespace (Pods)**: Dashboard ID 6417
- **Kubernetes App Metrics**: Dashboard ID 1471

## Tips

1. Change time range in top-right corner
2. Set auto-refresh (5s, 10s, 30s)
3. Add alerts by editing panels
4. Create folders to organize dashboards
5. Star important dashboards for quick access

## Troubleshooting

If data is not showing:
1. Check Prometheus is running: http://localhost:9090
2. Verify datasource connection in Grafana: Configuration → Data Sources
3. Check if metrics are available in Prometheus: http://localhost:9090/targets
