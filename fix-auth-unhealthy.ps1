# Script para corrigir problema de auth-service unhealthy

Write-Host "🔧 Corrigindo problema do auth-service..." -ForegroundColor Cyan

# Parar o auth-service
Write-Host "`n1. Parando auth-service..." -ForegroundColor Yellow
docker-compose stop auth-service

# Verificar logs para identificar o problema
Write-Host "`n2. Analisando logs..." -ForegroundColor Yellow
$logs = docker logs barbershop-auth --tail 50 2>&1 | Out-String

if ($logs -match "Connection refused|Connection timed out|Unable to connect") {
    Write-Host "  ⚠️  Problema de conexão detectado" -ForegroundColor Red
    Write-Host "  Verificando dependências..." -ForegroundColor Yellow
    
    # Verificar PostgreSQL
    Write-Host "`n3. Verificando PostgreSQL..." -ForegroundColor Yellow
    $pgStatus = docker exec barbershop-postgres pg_isready -U barbershop 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ PostgreSQL está pronto" -ForegroundColor Green
    } else {
        Write-Host "  ❌ PostgreSQL não está pronto" -ForegroundColor Red
        Write-Host "  Reiniciando PostgreSQL..." -ForegroundColor Yellow
        docker-compose restart postgres
        Start-Sleep -Seconds 10
    }
    
    # Verificar Redis
    Write-Host "`n4. Verificando Redis..." -ForegroundColor Yellow
    $redisStatus = docker exec barbershop-redis redis-cli ping 2>&1
    if ($redisStatus -match "PONG") {
        Write-Host "  ✅ Redis está pronto" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Redis não está pronto" -ForegroundColor Red
        Write-Host "  Reiniciando Redis..." -ForegroundColor Yellow
        docker-compose restart redis
        Start-Sleep -Seconds 5
    }
}

# Reiniciar auth-service
Write-Host "`n5. Reiniciando auth-service..." -ForegroundColor Yellow
docker-compose up -d auth-service

# Aguardar inicialização
Write-Host "`n6. Aguardando inicialização (30 segundos)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Verificar status
Write-Host "`n7. Verificando status..." -ForegroundColor Yellow
$status = docker inspect barbershop-auth --format='{{.State.Health.Status}}' 2>&1
Write-Host "  Status do healthcheck: $status" -ForegroundColor $(if ($status -eq "healthy") { "Green" } else { "Red" })

# Testar endpoint
Write-Host "`n8. Testando endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/actuator/health" -UseBasicParsing -TimeoutSec 10
    Write-Host "  ✅ Serviço está respondendo!" -ForegroundColor Green
} catch {
    Write-Host "  ❌ Serviço ainda não está respondendo" -ForegroundColor Red
    Write-Host "`n📋 Próximos passos:" -ForegroundColor Cyan
    Write-Host "   1. Execute: docker-compose logs auth-service" -ForegroundColor White
    Write-Host "   2. Verifique se há erros de conexão" -ForegroundColor White
    Write-Host "   3. Execute: .\check-auth-health.ps1" -ForegroundColor White
}

Write-Host "`n✅ Processo concluído!" -ForegroundColor Green


