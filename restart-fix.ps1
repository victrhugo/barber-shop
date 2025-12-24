# Script para reiniciar e corrigir problemas do BarberShop

Write-Host "🛑 Parando todos os serviços..." -ForegroundColor Yellow
docker-compose down -v

Write-Host "`n🔨 Reconstruindo imagens (isso pode demorar alguns minutos)..." -ForegroundColor Cyan
docker-compose build --no-cache

Write-Host "`n🚀 Iniciando serviços..." -ForegroundColor Green
docker-compose up -d

Write-Host "`n⏳ Aguardando serviços iniciarem (30 segundos)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

Write-Host "`n📊 Verificando status dos serviços..." -ForegroundColor Cyan
docker-compose ps

Write-Host "`n🔍 Verificando health checks..." -ForegroundColor Cyan

# Verificar Gateway
Write-Host "`nGateway:" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 5
    Write-Host "  ✅ Gateway está saudável" -ForegroundColor Green
} catch {
    Write-Host "  ❌ Gateway não está respondendo" -ForegroundColor Red
}

# Verificar Auth Service
Write-Host "`nAuth Service:" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/actuator/health" -UseBasicParsing -TimeoutSec 5
    Write-Host "  ✅ Auth Service está saudável" -ForegroundColor Green
} catch {
    Write-Host "  ❌ Auth Service não está respondendo" -ForegroundColor Red
}

# Verificar Booking Service
Write-Host "`nBooking Service:" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8083/actuator/health" -UseBasicParsing -TimeoutSec 5
    Write-Host "  ✅ Booking Service está saudável" -ForegroundColor Green
} catch {
    Write-Host "  ❌ Booking Service não está respondendo" -ForegroundColor Red
}

# Verificar API de Serviços
Write-Host "`nAPI de Serviços:" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/services" -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        $services = $response.Content | ConvertFrom-Json
        Write-Host "  ✅ API funcionando - $($services.Count) serviços encontrados" -ForegroundColor Green
    }
} catch {
    Write-Host "  ❌ API não está respondendo: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n📝 Para ver os logs em tempo real, execute:" -ForegroundColor Cyan
Write-Host "   docker-compose logs -f" -ForegroundColor White

Write-Host "`n✅ Processo concluído!" -ForegroundColor Green


