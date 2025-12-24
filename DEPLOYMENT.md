# 🚀 Guia de Deploy - BarberShop

Este documento fornece instruções detalhadas para fazer deploy da aplicação BarberShop em diferentes ambientes.

## 📋 Índice
1. [Deploy Local com Docker](#deploy-local)
2. [Deploy em Cloud (AWS)](#deploy-aws)
3. [Deploy em Cloud (Google Cloud)](#deploy-gcp)
4. [Deploy em Cloud (Azure)](#deploy-azure)
5. [Deploy com Kubernetes](#deploy-kubernetes)
6. [CI/CD](#cicd)

---

## Deploy Local com Docker

### Pré-requisitos
- Docker 20.10+
- Docker Compose 2.0+

### Passos

1. **Configure as variáveis de ambiente**
```bash
cp .env.example .env
# Edite .env com suas configurações
```

2. **Build e Execute**
```bash
docker-compose up -d --build
```

3. **Verifique os serviços**
```bash
docker-compose ps
docker-compose logs -f
```

4. **Acesse a aplicação**
- Frontend: http://localhost:3000
- API Gateway: http://localhost:8080

---

## Deploy AWS

### Opção 1: AWS Elastic Beanstalk

#### Backend (Cada Microserviço)

1. **Instale AWS CLI e EB CLI**
```bash
pip install awscli awsebcli
aws configure
```

2. **Inicialize EB**
```bash
cd gateway-service
eb init -p docker barbershop-gateway
```

3. **Crie ambiente**
```bash
eb create barbershop-gateway-prod
```

4. **Deploy**
```bash
eb deploy
```

Repita para cada microserviço (auth, user, booking).

#### Frontend (S3 + CloudFront)

1. **Build do Frontend**
```bash
cd frontend
npm install
npm run build
```

2. **Upload para S3**
```bash
aws s3 sync dist/ s3://barbershop-frontend --delete
```

3. **Configure CloudFront** (Console AWS)
- Origin: S3 bucket
- Behavior: Redirect to HTTPS
- Custom Error Pages: 404 → /index.html

### Opção 2: AWS ECS (Elastic Container Service)

1. **Crie repositórios ECR**
```bash
aws ecr create-repository --repository-name barbershop-gateway
aws ecr create-repository --repository-name barbershop-auth
aws ecr create-repository --repository-name barbershop-user
aws ecr create-repository --repository-name barbershop-booking
aws ecr create-repository --repository-name barbershop-frontend
```

2. **Build e Push das imagens**
```bash
# Login no ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

# Gateway
cd gateway-service
docker build -t barbershop-gateway .
docker tag barbershop-gateway:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/barbershop-gateway:latest
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/barbershop-gateway:latest

# Repita para outros serviços
```

3. **Crie Task Definitions** (via Console ou CLI)

4. **Crie ECS Cluster e Services**
```bash
aws ecs create-cluster --cluster-name barbershop-cluster
```

5. **Configure Load Balancer**
- Application Load Balancer
- Target Groups para cada serviço
- Rules de roteamento

### Banco de Dados (RDS PostgreSQL)

```bash
aws rds create-db-instance \
    --db-instance-identifier barbershop-db \
    --db-instance-class db.t3.micro \
    --engine postgres \
    --master-username admin \
    --master-user-password <password> \
    --allocated-storage 20
```

### Cache (ElastiCache Redis)

```bash
aws elasticache create-cache-cluster \
    --cache-cluster-id barbershop-redis \
    --cache-node-type cache.t3.micro \
    --engine redis \
    --num-cache-nodes 1
```

---

## Deploy Google Cloud Platform

### Opção: Cloud Run (Serverless)

#### 1. Configure gcloud CLI
```bash
gcloud init
gcloud auth configure-docker
```

#### 2. Deploy cada serviço

**Gateway Service:**
```bash
cd gateway-service
gcloud builds submit --tag gcr.io/PROJECT_ID/barbershop-gateway
gcloud run deploy barbershop-gateway \
    --image gcr.io/PROJECT_ID/barbershop-gateway \
    --platform managed \
    --region us-central1 \
    --allow-unauthenticated
```

**Auth Service:**
```bash
cd auth-service
gcloud builds submit --tag gcr.io/PROJECT_ID/barbershop-auth
gcloud run deploy barbershop-auth \
    --image gcr.io/PROJECT_ID/barbershop-auth \
    --platform managed \
    --region us-central1 \
    --no-allow-unauthenticated
```

Repita para user-service e booking-service.

#### 3. Frontend (Firebase Hosting)

```bash
cd frontend
npm install -g firebase-tools
firebase login
firebase init hosting
npm run build
firebase deploy
```

#### 4. Banco de Dados (Cloud SQL)

```bash
gcloud sql instances create barbershop-db \
    --database-version=POSTGRES_14 \
    --tier=db-f1-micro \
    --region=us-central1
```

#### 5. Cache (Memorystore Redis)

```bash
gcloud redis instances create barbershop-redis \
    --size=1 \
    --region=us-central1 \
    --redis-version=redis_6_x
```

---

## Deploy Azure

### Azure Container Instances

#### 1. Login e Resource Group
```bash
az login
az group create --name barbershop-rg --location eastus
```

#### 2. Container Registry
```bash
az acr create --resource-group barbershop-rg \
    --name barbershopregistry --sku Basic
```

#### 3. Build e Push
```bash
cd gateway-service
az acr build --registry barbershopregistry \
    --image barbershop-gateway:latest .
```

#### 4. Deploy Containers
```bash
az container create \
    --resource-group barbershop-rg \
    --name barbershop-gateway \
    --image barbershopregistry.azurecr.io/barbershop-gateway:latest \
    --dns-name-label barbershop-gateway \
    --ports 8080
```

#### 5. Banco de Dados (Azure Database for PostgreSQL)
```bash
az postgres server create \
    --resource-group barbershop-rg \
    --name barbershop-db \
    --location eastus \
    --admin-user adminuser \
    --admin-password <password> \
    --sku-name B_Gen5_1
```

#### 6. Frontend (Azure Static Web Apps)
```bash
cd frontend
az staticwebapp create \
    --name barbershop-frontend \
    --resource-group barbershop-rg \
    --source . \
    --location eastus \
    --branch main
```

---

## Deploy com Kubernetes

### 1. Crie arquivos de manifesto

**deployment.yaml** (exemplo para gateway):
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gateway-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: gateway
  template:
    metadata:
      labels:
        app: gateway
    spec:
      containers:
      - name: gateway
        image: <registry>/barbershop-gateway:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
---
apiVersion: v1
kind: Service
metadata:
  name: gateway-service
spec:
  selector:
    app: gateway
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

### 2. Deploy no Kubernetes

```bash
# Apply manifests
kubectl apply -f k8s/

# Verifique deployments
kubectl get deployments
kubectl get services
kubectl get pods

# Logs
kubectl logs -f deployment/gateway-service
```

### 3. Ingress Controller (Nginx)

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: barbershop-ingress
spec:
  rules:
  - host: api.barbershop.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: gateway-service
            port:
              number: 80
```

### 4. ConfigMaps e Secrets

```bash
# Secrets
kubectl create secret generic db-credentials \
    --from-literal=username=barbershop \
    --from-literal=password=<password>

# ConfigMap
kubectl create configmap app-config \
    --from-file=application.yml
```

---

## CI/CD

### GitHub Actions

**.github/workflows/deploy.yml:**
```yaml
name: Deploy

on:
  push:
    branches: [ main ]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 17
      uses: actions/setup-java@v2
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build Gateway Service
      run: |
        cd gateway-service
        mvn clean package -DskipTests
    
    - name: Build Docker Image
      run: |
        docker build -t ${{ secrets.REGISTRY }}/barbershop-gateway:${{ github.sha }} ./gateway-service
    
    - name: Push to Registry
      run: |
        echo ${{ secrets.REGISTRY_PASSWORD }} | docker login -u ${{ secrets.REGISTRY_USER }} --password-stdin
        docker push ${{ secrets.REGISTRY }}/barbershop-gateway:${{ github.sha }}
    
    - name: Deploy to Kubernetes
      run: |
        kubectl set image deployment/gateway-service gateway=${{ secrets.REGISTRY }}/barbershop-gateway:${{ github.sha }}
```

### GitLab CI/CD

**.gitlab-ci.yml:**
```yaml
stages:
  - build
  - test
  - deploy

build-gateway:
  stage: build
  script:
    - cd gateway-service
    - mvn clean package
  artifacts:
    paths:
      - gateway-service/target/*.jar

deploy-production:
  stage: deploy
  script:
    - docker build -t $CI_REGISTRY_IMAGE/gateway:latest ./gateway-service
    - docker push $CI_REGISTRY_IMAGE/gateway:latest
  only:
    - main
```

---

## Checklist de Produção

### Segurança
- [ ] HTTPS configurado
- [ ] Secrets em variáveis de ambiente
- [ ] Firewall rules configuradas
- [ ] JWT secret forte e único
- [ ] Rate limiting ativado
- [ ] CORS configurado corretamente

### Performance
- [ ] Connection pooling otimizado
- [ ] Redis cache configurado
- [ ] CDN para assets estáticos
- [ ] Gzip compression ativada
- [ ] Database indexes criados

### Monitoramento
- [ ] Health checks configurados
- [ ] Logs centralizados
- [ ] Alertas configurados
- [ ] Backup automático do banco

### Alta Disponibilidade
- [ ] Múltiplas réplicas de cada serviço
- [ ] Load balancer configurado
- [ ] Auto-scaling habilitado
- [ ] Database replicas (read replicas)

---

## Troubleshooting

### Serviço não inicia
```bash
# Ver logs
docker logs <container-id>
kubectl logs <pod-name>

# Verificar saúde
curl http://localhost:8080/actuator/health
```

### Erro de conexão com banco
```bash
# Testar conectividade
telnet <db-host> 5432

# Verificar credenciais
psql -h <db-host> -U <username> -d barbershop
```

### Alto uso de memória
```bash
# Ajustar JVM options
-Xms512m -Xmx1024m

# Kubernetes resources
resources:
  limits:
    memory: "1Gi"
  requests:
    memory: "512Mi"
```

---

## Suporte

Para problemas de deploy:
1. Verifique logs
2. Consulte documentação da cloud provider
3. Abra issue no GitHub

**Happy Deploying! 🚀**



