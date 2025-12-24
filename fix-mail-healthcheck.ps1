# Script para corrigir problema do MailHealthIndicator

Write-Host "🔧 Corrigindo problema do MailHealthIndicator..." -ForegroundColor Cyan

Write-Host "`n📋 Problema identificado:" -ForegroundColor Yellow
Write-Host "   O MailHealthIndicator está falhando porque as credenciais de email" -ForegroundColor White
Write-Host "   não estão configuradas ou estão incorretas." -ForegroundColor White
Write-Host "   Isso faz o healthcheck falhar e o container ficar unhealthy." -ForegroundColor White

Write-Host "`n✅ Solução aplicada:" -ForegroundColor Green
Write-Host "   - MailHealthIndicator desabilitado" -ForegroundColor White
Write-Host "   - Healthcheck agora verifica apenas DB e Redis" -ForegroundColor White

Write-Host "`n🔄 Reconstruindo auth-service..." -ForegroundColor Cyan
docker-compose build auth-service

Write-Host "`n🔄 Reiniciando auth-service..." -ForegroundColor Cyan
docker-compose up -d auth-service

Write-Host "`n⏳ Aguardando inicialização (30 segundos)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

Write-Host "`n🔍 Verificando status..." -ForegroundColor Cyan
$status = docker inspect barbershop-auth --format='{{.State.Health.Status}}' 2>&1
Write-Host "   Status do healthcheck: $status" -ForegroundColor $(if ($status -eq "healthy") { "Green" } else { "Yellow" })

Write-Host "`n🧪 Testando endpoint de health..." -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/actuator/health" -UseBasicParsing -TimeoutSec 10
    $health = $response.Content | ConvertFrom-Json
    Write-Host "   ✅ Serviço está respondendo!" -ForegroundColor Green
    Write-Host "   Status geral: $($health.status)" -ForegroundColor $(if ($health.status -eq "UP") { "Green" } else { "Yellow" })
    
    if ($health.components) {
        Write-Host "`n   Componentes:" -ForegroundColor Cyan
        foreach ($component in $health.components.PSObject.Properties) {
            $compStatus = $component.Value.status
            $color = if ($compStatus -eq "UP") { "Green" } else { "Yellow" }
            Write-Host "     - $($component.Name): $compStatus" -ForegroundColor $color
        }
    }
} catch {
    Write-Host "   ⚠️  Serviço ainda não está respondendo completamente" -ForegroundColor Yellow
    Write-Host "   Aguarde mais alguns segundos e tente novamente" -ForegroundColor White
}

Write-Host "`n✅ Correção aplicada!" -ForegroundColor Green
Write-Host "`n💡 Nota: Para habilitar email no futuro, configure as variáveis:" -ForegroundColor Cyan
Write-Host "   MAIL_USERNAME e MAIL_PASSWORD no arquivo .env" -ForegroundColor White

