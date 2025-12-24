# Script para verificar e diagnosticar problemas do auth-service

Write-Host "🔍 Verificando status do auth-service..." -ForegroundColor Cyan

# Verificar se o container está rodando
Write-Host "`n1. Status do Container:" -ForegroundColor Yellow
docker ps --filter "name=barbershop-auth" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Verificar logs recentes
Write-Host "`n2. Últimas 20 linhas dos logs:" -ForegroundColor Yellow
docker logs barbershop-auth --tail 20

# Verificar se o serviço está respondendo
Write-Host "`n3. Testando endpoint de health:" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/actuator/health" -UseBasicParsing -TimeoutSec 5
    Write-Host "  ✅ Health endpoint respondeu: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "  Resposta: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "  ❌ Health endpoint não respondeu: $($_.Exception.Message)" -ForegroundColor Red
}

# Verificar conectividade com banco
Write-Host "`n4. Verificando conectividade com PostgreSQL:" -ForegroundColor Yellow
docker exec barbershop-auth sh -c "nc -zv postgres 5432 2>&1" | Out-String

# Verificar conectividade com Redis
Write-Host "`n5. Verificando conectividade com Redis:" -ForegroundColor Yellow
docker exec barbershop-auth sh -c "nc -zv redis 6379 2>&1" | Out-String

# Verificar variáveis de ambiente
Write-Host "`n6. Variáveis de ambiente importantes:" -ForegroundColor Yellow
docker exec barbershop-auth env | Select-String -Pattern "SPRING_DATASOURCE|REDIS|JWT|MAIL" | ForEach-Object {
    $line = $_.Line
    if ($line -match "PASSWORD") {
        Write-Host "  $($line -replace '=.*', '=***')" -ForegroundColor Gray
    } else {
        Write-Host "  $line" -ForegroundColor Gray
    }
}

# Verificar se a porta está escutando
Write-Host "`n7. Verificando se a porta 8081 está escutando:" -ForegroundColor Yellow
docker exec barbershop-auth sh -c "netstat -tuln | grep 8081 || ss -tuln | grep 8081" 2>&1 | Out-String

Write-Host "`n✅ Diagnóstico completo!" -ForegroundColor Green
Write-Host "`n💡 Se o healthcheck está falhando, tente:" -ForegroundColor Cyan
Write-Host "   1. docker-compose restart auth-service" -ForegroundColor White
Write-Host "   2. Aguardar 2 minutos e verificar novamente" -ForegroundColor White
Write-Host "   3. Ver logs completos: docker-compose logs auth-service" -ForegroundColor White


