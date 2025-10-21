# Automated Grafana Dashboard Setup ✅

## 🎉 What's Automated Now

Your Jenkins pipeline now **automatically**:

1. ✅ Deploys Prometheus for metrics collection
2. ✅ Deploys Grafana with pre-configured dashboard
3. ✅ Configures Prometheus as datasource
4. ✅ Loads "Projet-S3 Kubernetes Monitoring" dashboard
5. ✅ Connects dashboard to Prometheus automatically

## 🚀 How It Works

### Pipeline Flow:
```
Jenkins Build → Deploy App → Deploy Monitoring Stack
                              ↓
                  Prometheus + Grafana + SonarQube
                              ↓
                  Dashboard Auto-Loads with 9 Panels
```

### What Gets Auto-Provisioned:

**ConfigMaps Created:**
- `grafana-datasources` → Prometheus connection
- `grafana-dashboard-provider` → Dashboard loader
- `grafana-dashboard-projet-s3` → Your custom dashboard

**Volumes Mounted:**
- `/etc/grafana/provisioning/datasources` → Datasource config
- `/etc/grafana/provisioning/dashboards` → Dashboard provider
- `/var/lib/grafana/dashboards` → Dashboard JSON files

## 📊 Dashboard Panels (Auto-Loaded)

1. **Pod Status** - Number of running pods
2. **CPU Usage per Pod** - Real-time CPU graph
3. **Memory Usage** - Total memory consumption
4. **Deployment Status** - Available replicas
5. **Memory Usage per Pod** - Memory graph over time
6. **Network Traffic** - Incoming/Outgoing data
7. **Pod Restarts** - Restart events tracking
8. **Resource Utilization** - CPU usage bar chart
9. **Service Availability** - Uptime percentage

## 🎯 Access Your Dashboard

### Step 1: Open Grafana
```
http://localhost:3000
```
Login: `admin` / `admin`

### Step 2: Navigate to Dashboard
1. Click **"Dashboards"** in left menu (4-squares icon)
2. You'll see **"Projet-S3 Kubernetes Monitoring"**
3. Click on it

**That's it!** No import needed, no configuration needed!

## 🔄 How to Update Dashboard

### Method 1: Edit in Grafana (Temporary)
1. Open dashboard
2. Click panel title → Edit
3. Make changes
4. Click Apply
5. **Note**: Changes lost on pod restart

### Method 2: Update ConfigMap (Permanent)
1. Edit file: `k8s/monitoring/grafana-deployment.yaml`
2. Find `grafana-dashboard-projet-s3` ConfigMap
3. Update the JSON in `projet-s3-dashboard.json` section
4. Commit and push
5. Trigger Jenkins build

## 📝 Dashboard Queries Reference

### CPU Usage
```promql
sum(rate(container_cpu_usage_seconds_total{namespace="default",pod=~"projet-s3.*",container!=""}[5m])) by (pod) * 100
```

### Memory Usage
```promql
sum(container_memory_usage_bytes{namespace="default",pod=~"projet-s3.*",container!=""}) by (pod) / 1024 / 1024
```

### Network Traffic In
```promql
sum(rate(container_network_receive_bytes_total{namespace="default",pod=~"projet-s3.*"}[5m])) by (pod) / 1024
```

### Network Traffic Out
```promql
sum(rate(container_network_transmit_bytes_total{namespace="default",pod=~"projet-s3.*"}[5m])) by (pod) / 1024
```

### Pod Restarts
```promql
sum(kube_pod_container_status_restarts_total{namespace="default",pod=~"projet-s3.*"}) by (pod)
```

### Service Uptime
```promql
avg(up{namespace="default"}) * 100
```

## 🔧 Troubleshooting

### Dashboard Not Showing?
Wait 30 seconds after Grafana starts, then refresh page.

### No Data in Panels?
- Check Prometheus: http://localhost:9090
- Wait 1-2 minutes for metrics collection
- Verify pods running: `minikube kubectl -- get pods`

### Datasource Error?
ConfigMap automatically configures it, but verify:
- Configuration → Data Sources → Prometheus
- URL should be: `http://prometheus:9090`

## 🎨 Customization Tips

### Add New Panel:
1. Edit `k8s/monitoring/grafana-deployment.yaml`
2. Add new panel object to `panels` array
3. Use existing panels as template
4. Commit, push, trigger Jenkins build

### Change Refresh Rate:
Find `"refresh": "10s"` and change to:
- `"5s"` - Every 5 seconds
- `"30s"` - Every 30 seconds
- `"1m"` - Every minute

### Change Time Range:
Find `"from": "now-1h"` and change to:
- `"now-30m"` - Last 30 minutes
- `"now-6h"` - Last 6 hours
- `"now-24h"` - Last 24 hours

## ✅ Benefits of Automation

- ✅ **Zero manual setup** - Dashboard loads automatically
- ✅ **Consistent deployments** - Same dashboard every time
- ✅ **Version controlled** - Dashboard in Git
- ✅ **Easy updates** - Change ConfigMap and redeploy
- ✅ **Team collaboration** - Everyone gets same dashboard
- ✅ **CI/CD integrated** - Part of pipeline

## 🚀 Next Steps

1. Trigger a Jenkins build to deploy everything
2. Wait for pipeline to complete
3. Open http://localhost:3000
4. Login and see your dashboard auto-loaded
5. Monitor your application in real-time!

**No more manual imports! Everything is automated!** 🎉
