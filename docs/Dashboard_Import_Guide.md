# Import Projet-S3 Dashboard to Grafana

## Dashboard Features

This dashboard includes **16 panels** showing:

### 📊 **Overview Stats (Top Row)**
1. **Pod Status** - Number of running pods
2. **Total Requests** - Network packets per second
3. **Average Response Time** - Application response metrics
4. **Deployment Status** - Available replicas
5. **Memory Usage** - Total memory consumption in MB

### 📈 **Performance Graphs**
6. **CPU Usage per Pod** - Real-time CPU usage for each pod
7. **Memory Usage per Pod** - Memory consumption over time
8. **Network Traffic In** - Incoming data rate (KB/s)
9. **Network Traffic Out** - Outgoing data rate (KB/s)

### 🔍 **Monitoring Panels**
10. **Pod Restarts** - Track pod restart events
11. **Container State** - Table showing container status
12. **Disk Usage** - Gauge showing storage utilization
13. **HTTP Response Distribution** - Pie chart of traffic
14. **Resource Utilization** - Bar gauge for CPU/Memory %
15. **Service Availability** - Uptime percentage
16. **Pod Age** - How long pods have been running

## 🚀 How to Import

### Method 1: Import from File (EASIEST)

1. Open Grafana: http://localhost:3000
2. Login (admin/admin)
3. Click **"Dashboards"** in left menu
4. Click **"New"** → **"Import"**
5. Click **"Upload JSON file"**
6. Select file: `D:/DevOps/Projet-S3/docs/projet-s3-dashboard.json`
7. Select **"Prometheus"** as data source
8. Click **"Import"**

### Method 2: Copy-Paste JSON

1. Open Grafana: http://localhost:3000
2. Login (admin/admin)
3. Click **"Dashboards"** → **"New"** → **"Import"**
4. Open file: `D:/DevOps/Projet-S3/docs/projet-s3-dashboard.json`
5. **Copy ALL the content** (Ctrl+A, Ctrl+C)
6. **Paste** into Grafana import box
7. Click **"Load"**
8. Select **"Prometheus"** as data source
9. Click **"Import"**

## ⚙️ Dashboard Settings

- **Auto-refresh**: Every 10 seconds
- **Time range**: Last 1 hour (adjustable)
- **Refresh intervals**: 5s, 10s, 30s, 1m, 5m

## 🎯 What You'll See

### Color Coding:
- 🟢 **Green**: Healthy/Good performance
- 🟡 **Yellow**: Warning/Medium usage
- 🔴 **Red**: Critical/High usage

### Key Metrics:
- **CPU**: Shows % usage (0-100%)
- **Memory**: Shows MB used
- **Network**: Shows KB/s transfer rate
- **Uptime**: Shows % availability (target: >99%)

## 📋 Quick Actions

### Change Time Range:
- Top right corner → Click "Last 1 hour"
- Select: Last 5m, 15m, 1h, 6h, 24h, etc.

### Zoom In:
- Click and drag on any graph to zoom
- Click "Zoom out" button to reset

### Export/Share:
- Click share icon (🔗) at top
- Get link or export as PNG

## 🔧 Troubleshooting

### No Data Showing?
1. Check Prometheus is running: http://localhost:9090
2. Wait 30-60 seconds for data collection
3. Verify pods are running: `minikube kubectl -- get pods`

### Panels Empty?
- Data might not be available yet
- Change time range to "Last 5 minutes"
- Click refresh button (🔄)

### Errors in Panels?
- Check datasource: Configuration → Data Sources → Prometheus
- Test connection (should show green checkmark)

## 📊 Understanding the Metrics

### CPU Usage
- Shows processor utilization
- Target: < 70%
- Alert if: > 85%

### Memory Usage
- Shows RAM consumption
- Target: < 400 MB (with 512 MB limit)
- Alert if: > 450 MB

### Network Traffic
- IN: Data received by application
- OUT: Data sent by application
- Spike = High activity

### Pod Restarts
- Should be 0 or very low
- Frequent restarts = problem

### Uptime
- Target: > 99%
- Shows service availability

## 🎨 Customization

### Edit Panel:
1. Click panel title → Edit
2. Modify query
3. Change visualization type
4. Click "Apply"

### Add New Panel:
1. Click "Add" → "Visualization"
2. Select Prometheus
3. Enter query
4. Click "Apply"

### Save Changes:
- Click 💾 icon at top
- Enter description
- Click "Save"
