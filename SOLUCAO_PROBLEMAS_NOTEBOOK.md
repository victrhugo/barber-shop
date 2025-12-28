# 🔧 Solução de Problemas - Notebook vs PC

Este guia ajuda a identificar e resolver problemas quando o projeto funciona no PC mas não no notebook.

## 📋 Checklist Rápido

### 1. Docker e Docker Compose
- [ ] Docker Desktop instalado e rodando
- [ ] Docker Compose instalado
- [ ] Versões compatíveis (Docker 20.10+, Compose 2.0+)

**Verificar:**
```powershell
docker --version
docker-compose --version
docker ps
```

**Solução:** Instale/atualize o Docker Desktop: https://www.docker.com/products/docker-desktop

---

### 2. Portas em Uso ⚠️ (PROBLEMA MAIS COMUM)

O sistema precisa das seguintes portas livres:
- **3000** - Frontend
- **8080** - Gateway
- **8081** - Auth Service
- **8082** - User Service
- **8083** - Booking Service
- **5432** - PostgreSQL
- **6379** - Redis

**Verificar portas em uso:**
```powershell
# Verificar todas as portas
Get-NetTCPConnection | Where-Object {$_.LocalPort -in @(3000,8080,8081,8082,8083,5432,6379)} | Format-Table LocalPort, State, OwningProcess

# Ver qual processo está usando uma porta específica
Get-NetTCPConnection -LocalPort 8080 | Select-Object OwningProcess | ForEach-Object { Get-Process -Id $_.OwningProcess }
```

**Soluções:**

**Opção 1: Parar processos que estão usando as portas**
```powershell
# Encontrar e parar processo na porta 8080 (exemplo)
$process = Get-NetTCPConnection -LocalPort 8080 | Select-Object -First 1 -ExpandProperty OwningProcess
Stop-Process -Id $process -Force
```

**Opção 2: Alterar portas no docker-compose.yml**
```yaml
# Exemplo: mudar frontend de 3000 para 3001
frontend:
  ports:
    - "3001:80"  # Acesse em http://localhost:3001
```

**Opção 3: Limpar containers antigos**
```powershell
docker-compose down
docker-compose down -v  # Remove volumes também
```

---

### 3. Recursos do Sistema (RAM/CPU)

**Requisitos mínimos:**
- RAM: 4GB (2GB mínimo, mas pode ser lento)
- CPU: 2 cores
- Espaço em disco: 5GB livre

**Verificar recursos:**
```powershell
# RAM total e disponível
Get-CimInstance Win32_ComputerSystem | Select-Object TotalPhysicalMemory
Get-CimInstance Win32_OperatingSystem | Select-Object FreePhysicalMemory

# CPU
Get-CimInstance Win32_ComputerSystem | Select-Object NumberOfLogicalProcessors
```

**Solução:** Feche outros programas pesados antes de iniciar o sistema.

---

### 4. Firewall e Antivírus

Firewall ou antivírus podem bloquear conexões Docker.

**Soluções:**
1. Adicione exceção no Windows Firewall para Docker
2. Configure antivírus para não escanear pasta do projeto
3. Teste temporariamente desabilitando firewall/antivírus

---

### 5. WSL2 (Windows Subsystem for Linux)

Se estiver usando Docker Desktop no Windows, o WSL2 é necessário.

**Verificar WSL2:**
```powershell
wsl --status
wsl --list --verbose
```

**Instalar/Atualizar WSL2:**
```powershell
# Instalar WSL2
wsl --install

# Atualizar para WSL2
wsl --set-version Ubuntu 2
```

**Reiniciar Docker Desktop após instalar WSL2.**

---

### 6. Cache do Docker Corrompido

**Limpar cache e reconstruir:**
```powershell
# Parar tudo
docker-compose down -v

# Limpar cache do Docker
docker system prune -a --volumes

# Reconstruir sem cache
docker-compose build --no-cache

# Iniciar
docker-compose up -d
```

---

### 7. Variáveis de Ambiente

**Verificar arquivo .env:**
```powershell
# Verificar se existe
Test-Path .env

# Se não existir, copiar do exemplo
Copy-Item env.example .env

# Editar se necessário
notepad .env
```

---

### 8. Permissões

**Verificar permissões da pasta:**
- Certifique-se de ter permissão de leitura/escrita na pasta do projeto
- Se estiver em OneDrive/Dropbox, pode causar problemas - mova para pasta local

---

### 9. Versões Diferentes

**Verificar versões no PC e notebook:**
```powershell
# Docker
docker --version
docker-compose --version

# Java (se rodando sem Docker)
java -version

# Node.js (se rodando frontend sem Docker)
node --version
```

**Solução:** Use as mesmas versões em ambos os computadores.

---

### 10. Logs de Erro

**Ver logs de todos os serviços:**
```powershell
docker-compose logs -f
```

**Ver logs de um serviço específico:**
```powershell
docker logs barbershop-auth
docker logs barbershop-gateway
docker logs barbershop-user
docker logs barbershop-booking
docker logs barbershop-frontend
docker logs barbershop-postgres
docker logs barbershop-redis
```

**Erros comuns nos logs:**

1. **"Connection refused" ou "Cannot connect to database"**
   - PostgreSQL não está pronto ainda
   - Aguarde mais tempo ou verifique healthcheck

2. **"Port already in use"**
   - Porta está ocupada (veja item 2)

3. **"Out of memory"**
   - RAM insuficiente (veja item 3)

4. **"Cannot find module" ou "File not found"**
   - Arquivos faltando ou caminho incorreto

---

## 🚀 Passo a Passo para Resolver

### Passo 1: Executar Script de Diagnóstico
```powershell
.\diagnostico-problemas.ps1
```

### Passo 2: Limpar Ambiente
```powershell
# Parar e remover tudo
docker-compose down -v

# Limpar cache
docker system prune -a --volumes
```

### Passo 3: Verificar Portas
```powershell
# Verificar portas
Get-NetTCPConnection | Where-Object {$_.LocalPort -in @(3000,8080,8081,8082,8083,5432,6379)}
```

### Passo 4: Reconstruir e Iniciar
```powershell
# Reconstruir sem cache
docker-compose build --no-cache

# Iniciar
docker-compose up -d

# Ver logs
docker-compose logs -f
```

### Passo 5: Verificar Status
```powershell
# Ver status de todos os serviços
docker-compose ps

# Todos devem estar "Up" e "healthy"
```

---

## 🔍 Comparar PC vs Notebook

Execute no **PC (funcionando)** e no **notebook (não funcionando)** e compare:

```powershell
# Versões
docker --version
docker-compose --version

# Recursos
Get-CimInstance Win32_ComputerSystem | Select-Object TotalPhysicalMemory, NumberOfLogicalProcessors

# Portas em uso
Get-NetTCPConnection | Where-Object {$_.LocalPort -in @(3000,8080,8081,8082,8083,5432,6379)} | Format-Table LocalPort, State, OwningProcess

# Containers rodando
docker ps

# Logs
docker-compose logs --tail=50
```

---

## 📞 Erros Específicos

### Erro: "Cannot connect to Docker daemon"
**Solução:** Inicie o Docker Desktop

### Erro: "Port 5432 is already allocated"
**Solução:** Pare PostgreSQL local ou altere porta no docker-compose.yml

### Erro: "No space left on device"
**Solução:** Limpe espaço em disco e cache do Docker:
```powershell
docker system prune -a --volumes
```

### Erro: "Service 'gateway' failed to start"
**Solução:** Verifique logs:
```powershell
docker logs barbershop-gateway
docker logs barbershop-auth
```

### Erro: Frontend não carrega (página em branco)
**Solução:** 
1. Verifique logs: `docker logs barbershop-frontend`
2. Verifique se gateway está acessível: `curl http://localhost:8080/api/services`
3. Verifique console do navegador (F12)

---

## ✅ Checklist Final

Antes de reportar problema, verifique:

- [ ] Docker Desktop está rodando
- [ ] Todas as portas estão livres
- [ ] RAM disponível > 2GB
- [ ] Arquivo .env existe e está configurado
- [ ] Executei `docker-compose down -v` e `docker-compose build --no-cache`
- [ ] Verifiquei os logs: `docker-compose logs -f`
- [ ] Comparei versões com o PC que funciona

---

## 💡 Dica Final

Se nada funcionar, tente:

1. **Reinstalar Docker Desktop** completamente
2. **Clonar projeto novamente** do GitHub
3. **Usar WSL2** ao invés de Hyper-V (se disponível)
4. **Executar como Administrador** o PowerShell

---

**Criado para ajudar a resolver problemas comuns entre diferentes ambientes Windows.**

