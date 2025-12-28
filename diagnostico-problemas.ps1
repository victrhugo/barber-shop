# Script de Diagnostico - BarberShop System
# Execute este script no notebook para identificar problemas

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DIAGNOSTICO BARBERSHOP SYSTEM" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Verificar Docker
Write-Host "[1/8] Verificando Docker..." -ForegroundColor Yellow
$dockerCheck = Get-Command docker -ErrorAction SilentlyContinue
if ($dockerCheck) {
    $dockerVersion = docker --version 2>&1
    if ($?) {
        Write-Host "[OK] Docker instalado: $dockerVersion" -ForegroundColor Green
    } else {
        Write-Host "[ERRO] Docker NAO esta funcionando corretamente!" -ForegroundColor Red
        Write-Host "  Instale Docker Desktop: https://www.docker.com/products/docker-desktop" -ForegroundColor Yellow
        exit 1
    }
} else {
    Write-Host "[ERRO] Docker NAO esta instalado!" -ForegroundColor Red
    Write-Host "  Instale Docker Desktop: https://www.docker.com/products/docker-desktop" -ForegroundColor Yellow
    exit 1
}

# 2. Verificar Docker Compose
Write-Host "[2/8] Verificando Docker Compose..." -ForegroundColor Yellow
$composeCheck = Get-Command docker-compose -ErrorAction SilentlyContinue
if ($composeCheck) {
    $composeVersion = docker-compose --version 2>&1
    if ($?) {
        Write-Host "[OK] Docker Compose instalado: $composeVersion" -ForegroundColor Green
    } else {
        Write-Host "[ERRO] Docker Compose NAO esta funcionando corretamente!" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "[ERRO] Docker Compose NAO esta instalado!" -ForegroundColor Red
    exit 1
}

# 3. Verificar se Docker esta rodando
Write-Host "[3/8] Verificando se Docker esta rodando..." -ForegroundColor Yellow
docker ps 2>&1 | Out-Null
if ($?) {
    Write-Host "[OK] Docker esta rodando" -ForegroundColor Green
} else {
    Write-Host "[ERRO] Docker NAO esta rodando!" -ForegroundColor Red
    Write-Host "  Inicie o Docker Desktop" -ForegroundColor Yellow
    exit 1
}

# 4. Verificar portas em uso
Write-Host "[4/8] Verificando portas necessarias..." -ForegroundColor Yellow
$ports = @(3000, 8080, 8081, 8082, 8083, 5432, 6379)
$portsInUse = @()

foreach ($port in $ports) {
    $connection = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($connection) {
        $process = Get-Process -Id $connection.OwningProcess -ErrorAction SilentlyContinue
        $portsInUse += "$port (PID: $($connection.OwningProcess), Processo: $($process.ProcessName))"
        Write-Host "[AVISO] Porta $port esta em uso: $($process.ProcessName)" -ForegroundColor Yellow
    } else {
        Write-Host "[OK] Porta $port esta livre" -ForegroundColor Green
    }
}

if ($portsInUse.Count -gt 0) {
    Write-Host ""
    Write-Host "[ATENCAO] As seguintes portas estao em uso:" -ForegroundColor Yellow
    foreach ($portInfo in $portsInUse) {
        Write-Host "  - $portInfo" -ForegroundColor Yellow
    }
    Write-Host ""
    Write-Host "Solucoes:" -ForegroundColor Cyan
    Write-Host "  1. Pare os processos que estao usando essas portas" -ForegroundColor White
    Write-Host "  2. Ou altere as portas no docker-compose.yml" -ForegroundColor White
}

# 5. Verificar recursos do sistema
Write-Host "[5/8] Verificando recursos do sistema..." -ForegroundColor Yellow
$totalRAM = [math]::Round((Get-CimInstance Win32_ComputerSystem).TotalPhysicalMemory / 1GB, 2)
$availableRAM = [math]::Round((Get-CimInstance Win32_OperatingSystem).FreePhysicalMemory / 1MB, 2)
$cpuCores = (Get-CimInstance Win32_ComputerSystem).NumberOfLogicalProcessors

Write-Host "  RAM Total: ${totalRAM} GB" -ForegroundColor White
Write-Host "  RAM Disponivel: ${availableRAM} GB" -ForegroundColor White
Write-Host "  CPU Cores: $cpuCores" -ForegroundColor White

if ($totalRAM -lt 4) {
    Write-Host "[AVISO] RAM total menor que 4GB (recomendado)" -ForegroundColor Yellow
}
if ($availableRAM -lt 2) {
    Write-Host "[AVISO] RAM disponivel menor que 2GB" -ForegroundColor Yellow
    Write-Host "  Feche outros programas antes de iniciar o sistema" -ForegroundColor Yellow
}

# 6. Verificar se containers ja existem
Write-Host "[6/8] Verificando containers existentes..." -ForegroundColor Yellow
$existingContainers = docker ps -a --filter "name=barbershop" --format "{{.Names}}" 2>&1
if ($existingContainers -and $existingContainers -notmatch "error") {
    Write-Host "[AVISO] Containers existentes encontrados:" -ForegroundColor Yellow
    foreach ($container in $existingContainers) {
        Write-Host "  - $container" -ForegroundColor White
    }
    Write-Host ""
    Write-Host "Para limpar containers antigos, execute:" -ForegroundColor Cyan
    Write-Host "  docker-compose down" -ForegroundColor White
    Write-Host "  docker-compose down -v  # Remove volumes tambem" -ForegroundColor White
} else {
    Write-Host "[OK] Nenhum container antigo encontrado" -ForegroundColor Green
}

# 7. Verificar arquivos necessarios
Write-Host "[7/8] Verificando arquivos do projeto..." -ForegroundColor Yellow
$requiredFiles = @(
    "docker-compose.yml",
    "init-db.sql",
    "auth-service/Dockerfile",
    "user-service/Dockerfile",
    "booking-service/Dockerfile",
    "gateway-service/Dockerfile",
    "frontend/Dockerfile"
)

$missingFiles = @()
foreach ($file in $requiredFiles) {
    if (Test-Path $file) {
        Write-Host "[OK] $file" -ForegroundColor Green
    } else {
        Write-Host "[ERRO] $file NAO encontrado!" -ForegroundColor Red
        $missingFiles += $file
    }
}

if ($missingFiles.Count -gt 0) {
    Write-Host ""
    Write-Host "[ERRO] Arquivos faltando! Certifique-se de que esta no diretorio correto do projeto." -ForegroundColor Red
}

# 8. Verificar variaveis de ambiente
Write-Host "[8/8] Verificando variaveis de ambiente..." -ForegroundColor Yellow
if (Test-Path ".env") {
    Write-Host "[OK] Arquivo .env encontrado" -ForegroundColor Green
} else {
    Write-Host "[AVISO] Arquivo .env nao encontrado" -ForegroundColor Yellow
    if (Test-Path "env.example") {
        Write-Host "  Copie env.example para .env:" -ForegroundColor Cyan
        Write-Host "    Copy-Item env.example .env" -ForegroundColor White
    }
}

# Resumo e recomendacoes
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  RESUMO E PROXIMOS PASSOS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($portsInUse.Count -gt 0) {
    Write-Host "[PROBLEMA] Portas em uso" -ForegroundColor Red
    Write-Host "  Solucao: Pare os processos ou altere as portas" -ForegroundColor Yellow
    Write-Host ""
}

if ($missingFiles.Count -gt 0) {
    Write-Host "[PROBLEMA] Arquivos faltando" -ForegroundColor Red
    Write-Host "  Solucao: Certifique-se de estar no diretorio correto" -ForegroundColor Yellow
    Write-Host ""
}

Write-Host "Para iniciar o sistema, execute:" -ForegroundColor Cyan
Write-Host "  1. docker-compose down -v  # Limpar tudo (se necessario)" -ForegroundColor White
Write-Host "  2. docker-compose build    # Construir imagens" -ForegroundColor White
Write-Host "  3. docker-compose up -d    # Iniciar servicos" -ForegroundColor White
Write-Host "  4. docker-compose logs -f  # Ver logs" -ForegroundColor White
Write-Host ""

Write-Host "Para verificar status dos servicos:" -ForegroundColor Cyan
Write-Host "  docker-compose ps" -ForegroundColor White
Write-Host ""

Write-Host "Para ver logs de um servico especifico:" -ForegroundColor Cyan
Write-Host "  docker logs barbershop-auth" -ForegroundColor White
Write-Host "  docker logs barbershop-gateway" -ForegroundColor White
Write-Host "  docker logs barbershop-frontend" -ForegroundColor White
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
