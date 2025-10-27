# Graph Mailer Deployment Guide

This guide provides step-by-step instructions for deploying the Graph Mailer microservice in various environments.

## 🛠️ Prerequisites Setup

### 1. Microsoft Graph Application Registration

1. **Navigate to Azure Portal**:

   - Go to [Azure Portal](https://portal.azure.com)
   - Navigate to "Azure Active Directory" → "App registrations"

2. **Create New Registration**:

   - Click "New registration"
   - Name: `graph-mailer-service`
   - Account types: "Accounts in this organizational directory only"
   - Redirect URI: Leave blank (not needed for service-to-service)

3. **Configure API Permissions**:

   - Go to "API permissions"
   - Click "Add a permission"
   - Select "Microsoft Graph" → "Application permissions"
   - Add `Mail.Send` permission
   - Click "Grant admin consent" (requires admin privileges)

4. **Create Client Secret**:

   - Go to "Certificates & secrets"
   - Click "New client secret"
   - Description: `graph-mailer-secret`
   - Expires: 24 months (or as per your policy)
   - **Save the secret value immediately** (won't be shown again)

5. **Note Configuration Values**:
   ```
   Tenant ID: Available in "Overview" tab
   Client ID: Available in "Overview" tab (Application ID)
   Client Secret: The value you just created
   ```

### 2. Test Graph API Access

Before deploying, verify your Graph API setup:

```bash
# Install required tools (if not available)
# Ubuntu/Debian: apt-get install curl jq
# MacOS: brew install curl jq
# Windows: Use PowerShell or WSL

# Get access token
curl -X POST "https://login.microsoftonline.com/{tenant-id}/oauth2/v2.0/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id={client-id}&client_secret={client-secret}&scope=https://graph.microsoft.com/.default&grant_type=client_credentials"

# Test with the token (replace {access-token} and {user-upn})
curl -X GET "https://graph.microsoft.com/v1.0/users/{user-upn}" \
  -H "Authorization: Bearer {access-token}"
```

## Docker Deployment

### Option 1: Docker Run

```bash
# 1. Build the image
docker build -t graph-mailer:latest .

# 2. Create environment file
cp .env.template .env
# Edit .env with your actual values

# 3. Run the container
docker run -d \
  --name graph-mailer \
  --env-file .env \
  -p 8080:8080 \
  --restart unless-stopped \
  graph-mailer:latest

# 4. Check logs
docker logs -f graph-mailer

# 5. Test health
curl http://localhost:8080/actuator/health
```

### Option 2: Docker Compose

```bash
# 1. Create environment file
cp .env.template .env
# Edit .env with your actual values

# 2. Start services
docker-compose up -d

# 3. Check status
docker-compose ps
docker-compose logs -f graph-mailer

# 4. Test the service
./test-api.sh --health
```

### Option 3: Docker Swarm

```yaml
# docker-stack.yml
version: "3.8"

services:
  graph-mailer:
    image: graph-mailer:latest
    ports:
      - "8080:8080"
    environment:
      - GRAPH_TENANT_ID_FILE=/run/secrets/graph_tenant_id
      - GRAPH_CLIENT_ID_FILE=/run/secrets/graph_client_id
      - GRAPH_CLIENT_SECRET_FILE=/run/secrets/graph_client_secret
      - GRAPH_MAILER_API_KEY_FILE=/run/secrets/graph_api_key
      - SPRING_PROFILES_ACTIVE=api-key
    secrets:
      - graph_tenant_id
      - graph_client_id
      - graph_client_secret
      - graph_api_key
    deploy:
      replicas: 3
      restart_policy:
        condition: on-failure
        delay: 5s
        max_attempts: 3
      resources:
        limits:
          memory: 1G
          cpus: "0.5"
        reservations:
          memory: 512M
          cpus: "0.25"

secrets:
  graph_tenant_id:
    external: true
  graph_client_id:
    external: true
  graph_client_secret:
    external: true
  graph_api_key:
    external: true
```

Deploy:

```bash
# Create secrets
echo "your-tenant-id" | docker secret create graph_tenant_id -
echo "your-client-id" | docker secret create graph_client_id -
echo "your-client-secret" | docker secret create graph_client_secret -
echo "your-api-key" | docker secret create graph_api_key -

# Deploy stack
docker stack deploy -c docker-stack.yml graph-mailer
```

## Kubernetes Deployment

### Step 1: Create Namespace and Secrets

```bash
# Create namespace
kubectl create namespace graph-mailer

# Create secrets
kubectl create secret generic graph-secrets \
  --from-literal=tenant-id="your-tenant-id" \
  --from-literal=client-id="your-client-id" \
  --from-literal=client-secret="your-client-secret" \
  --from-literal=api-key="your-api-key" \
  --namespace=graph-mailer
```

### Step 2: Deploy Application

```yaml
# k8s-deployment.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: graph-mailer
  namespace: graph-mailer
  labels:
    app: graph-mailer
    version: v1.0.0
spec:
  replicas: 3
  selector:
    matchLabels:
      app: graph-mailer
  template:
    metadata:
      labels:
        app: graph-mailer
        version: v1.0.0
    spec:
      containers:
        - name: graph-mailer
          image: graph-mailer:latest
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8080
              protocol: TCP
          env:
            - name: GRAPH_TENANT_ID
              valueFrom:
                secretKeyRef:
                  name: graph-secrets
                  key: tenant-id
            - name: GRAPH_CLIENT_ID
              valueFrom:
                secretKeyRef:
                  name: graph-secrets
                  key: client-id
            - name: GRAPH_CLIENT_SECRET
              valueFrom:
                secretKeyRef:
                  name: graph-secrets
                  key: client-secret
            - name: GRAPH_MAILER_API_KEY
              valueFrom:
                secretKeyRef:
                  name: graph-secrets
                  key: api-key
            - name: SPRING_PROFILES_ACTIVE
              value: "api-key"
            - name: JAVA_OPTS
              value: "-Xms512m -Xmx1024m"
          resources:
            limits:
              memory: "1Gi"
              cpu: "500m"
            requests:
              memory: "512Mi"
              cpu: "250m"
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 60
            periodSeconds: 30
            timeoutSeconds: 10
            failureThreshold: 3
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
            timeoutSeconds: 5
            failureThreshold: 3
          securityContext:
            runAsNonRoot: true
            runAsUser: 1001
            allowPrivilegeEscalation: false
            readOnlyRootFilesystem: true
          volumeMounts:
            - name: tmp-volume
              mountPath: /tmp
            - name: logs-volume
              mountPath: /app/logs
      volumes:
        - name: tmp-volume
          emptyDir: {}
        - name: logs-volume
          emptyDir: {}
      securityContext:
        fsGroup: 1001

---
apiVersion: v1
kind: Service
metadata:
  name: graph-mailer-service
  namespace: graph-mailer
  labels:
    app: graph-mailer
spec:
  type: ClusterIP
  ports:
    - port: 80
      targetPort: 8080
      protocol: TCP
      name: http
  selector:
    app: graph-mailer

---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: graph-mailer-ingress
  namespace: graph-mailer
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
    - hosts:
        - graph-mailer.yourdomain.com
      secretName: graph-mailer-tls
  rules:
    - host: graph-mailer.yourdomain.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: graph-mailer-service
                port:
                  number: 80
```

Deploy:

```bash
kubectl apply -f k8s-deployment.yml

# Check deployment
kubectl get pods -n graph-mailer
kubectl logs -l app=graph-mailer -n graph-mailer
```

### Step 3: Configure Horizontal Pod Autoscaler

```yaml
# k8s-hpa.yml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: graph-mailer-hpa
  namespace: graph-mailer
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: graph-mailer
  minReplicas: 3
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
```

## OpenShift Deployment

### Step 1: Create Project and Secrets

```bash
# Login to OpenShift
oc login https://your-openshift-cluster.com

# Create project
oc new-project graph-mailer

# Create secrets
oc create secret generic graph-secrets \
  --from-literal=tenant-id="your-tenant-id" \
  --from-literal=client-id="your-client-id" \
  --from-literal=client-secret="your-client-secret" \
  --from-literal=api-key="your-api-key"
```

### Step 2: Deploy with DeploymentConfig

```yaml
# openshift-deployment.yml
apiVersion: apps.openshift.io/v1
kind: DeploymentConfig
metadata:
  name: graph-mailer
  labels:
    app: graph-mailer
spec:
  replicas: 3
  selector:
    app: graph-mailer
  template:
    metadata:
      labels:
        app: graph-mailer
    spec:
      containers:
        - name: graph-mailer
          image: graph-mailer:latest
          ports:
            - containerPort: 8080
          env:
            - name: GRAPH_TENANT_ID
              valueFrom:
                secretKeyRef:
                  name: graph-secrets
                  key: tenant-id
            - name: GRAPH_CLIENT_ID
              valueFrom:
                secretKeyRef:
                  name: graph-secrets
                  key: client-id
            - name: GRAPH_CLIENT_SECRET
              valueFrom:
                secretKeyRef:
                  name: graph-secrets
                  key: client-secret
            - name: GRAPH_MAILER_API_KEY
              valueFrom:
                secretKeyRef:
                  name: graph-secrets
                  key: api-key
            - name: SPRING_PROFILES_ACTIVE
              value: "api-key"
          resources:
            limits:
              memory: "1Gi"
              cpu: "500m"
            requests:
              memory: "512Mi"
              cpu: "250m"
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 60
            periodSeconds: 30
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10

---
apiVersion: v1
kind: Service
metadata:
  name: graph-mailer-service
  labels:
    app: graph-mailer
spec:
  ports:
    - port: 80
      targetPort: 8080
  selector:
    app: graph-mailer

---
apiVersion: route.openshift.io/v1
kind: Route
metadata:
  name: graph-mailer-route
  labels:
    app: graph-mailer
spec:
  host: graph-mailer.apps.your-cluster.com
  to:
    kind: Service
    name: graph-mailer-service
  tls:
    termination: edge
    insecureEdgeTerminationPolicy: Redirect
```

Deploy:

```bash
oc apply -f openshift-deployment.yml

# Check deployment
oc get pods
oc logs -l app=graph-mailer
```

## 🔧 Post-Deployment Configuration

### 1. Health Checks

```bash
# Basic health check
curl https://your-domain.com/actuator/health

# Detailed health with authentication
curl -H "X-API-Key: your-api-key" https://your-domain.com/actuator/health

# Check all available endpoints
curl https://your-domain.com/actuator
```

### 2. Monitoring Setup

**Prometheus Configuration**:

```yaml
# prometheus-config.yml
scrape_configs:
  - job_name: "graph-mailer"
    static_configs:
      - targets: ["graph-mailer-service:80"]
    metrics_path: "/actuator/prometheus"
    scrape_interval: 30s
```

**Grafana Dashboard**: Import the provided dashboard JSON or create custom dashboards with these metrics:

- `http_server_requests_seconds`
- `jvm_memory_used_bytes`
- `graph_mailer_email_send_total`
- `graph_mailer_email_send_duration_seconds`

### 3. Log Management

**ELK Stack Configuration**:

```yaml
# filebeat.yml
filebeat.inputs:
  - type: container
    paths:
      - "/var/lib/docker/containers/*/*.log"
    processors:
      - add_docker_metadata:
          host: "unix:///var/run/docker.sock"
      - decode_json_fields:
          fields: ["message"]
          target: ""
          overwrite_keys: true

output.elasticsearch:
  hosts: ["elasticsearch:9200"]
  index: "graph-mailer-logs-%{+yyyy.MM.dd}"
```

### 4. Security Hardening

**Network Policies** (Kubernetes):

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: graph-mailer-network-policy
spec:
  podSelector:
    matchLabels:
      app: graph-mailer
  policyTypes:
    - Ingress
    - Egress
  ingress:
    - from:
        - namespaceSelector:
            matchLabels:
              name: ingress-nginx
      ports:
        - protocol: TCP
          port: 8080
  egress:
    - to: []
      ports:
        - protocol: TCP
          port: 443 # HTTPS to Graph API
        - protocol: TCP
          port: 53 # DNS
        - protocol: UDP
          port: 53 # DNS
```

## Testing Deployment

### 1. Automated Testing

```bash
# Set environment variables for your deployment
export GRAPH_MAILER_URL="https://graph-mailer.yourdomain.com"
export GRAPH_MAILER_API_KEY="your-api-key"
export GRAPH_TEST_FROM_UPN="noreply@yourdomain.com"
export GRAPH_TEST_TO_EMAIL="test@yourdomain.com"

# Run basic tests
./test-api.sh

# Run full test suite
./test-api.sh --full
```

### 2. Load Testing

```bash
# Install Apache Bench (if not available)
# Ubuntu/Debian: apt-get install apache2-utils
# MacOS: brew install httpie

# Simple load test
ab -n 100 -c 10 -H "X-API-Key: your-api-key" \
   -p test-payload.json \
   -T "application/json" \
   https://graph-mailer.yourdomain.com/api/v1/mail/send
```

### 3. Integration Testing

Create test scenarios for:

- Authentication validation
- Rate limiting behavior
- Error handling
- Large attachment uploads
- Concurrent request handling

## Troubleshooting

### Common Issues

1. **Graph API Authentication Errors**:

   ```bash
   # Check token validity
   curl -X POST "https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token" \
     -d "client_id={client-id}&client_secret={client-secret}&scope=https://graph.microsoft.com/.default&grant_type=client_credentials"
   ```

2. **Container Startup Issues**:

   ```bash
   # Check container logs
   docker logs graph-mailer

   # Check environment variables
   docker exec graph-mailer env | grep GRAPH
   ```

3. **Network Connectivity**:

   ```bash
   # Test from within container
   docker exec graph-mailer curl -I https://graph.microsoft.com

   # Check DNS resolution
   docker exec graph-mailer nslookup graph.microsoft.com
   ```

4. **Performance Issues**:

   ```bash
   # Monitor JVM metrics
   curl -H "X-API-Key: your-key" https://your-domain/actuator/metrics/jvm.memory.used

   # Check garbage collection
   curl -H "X-API-Key: your-key" https://your-domain/actuator/metrics/jvm.gc.pause
   ```

### Log Analysis

Key log patterns to monitor:

```bash
# Authentication failures
grep "Authentication failed" /app/logs/application.log

# Rate limit violations
grep "Rate limit exceeded" /app/logs/application.log

# Graph API errors
grep "GraphServiceException" /app/logs/application.log

# Performance issues
grep "duration.*[5-9][0-9][0-9][0-9]" /app/logs/application.log
```

## Scaling and Optimization

### Horizontal Scaling

1. **Load Balancer Configuration**:

   - Enable session affinity: **None** (stateless service)
   - Health check path: `/actuator/health`
   - Health check interval: 30 seconds

2. **Auto-scaling Metrics**:
   - CPU utilization: 70%
   - Memory utilization: 80%
   - Custom metric: Request rate (requests/second)

### Performance Tuning

1. **JVM Options**:

   ```bash
   JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"
   ```

2. **Connection Pooling**:

   - Configure HTTP client pool size
   - Set appropriate timeouts
   - Enable connection reuse

3. **Caching Strategy**:
   - Cache Graph API tokens
   - Implement request deduplication
   - Use circuit breakers for resilience

This deployment guide should cover most production scenarios. Adjust the configurations based on your specific infrastructure and security requirements.
