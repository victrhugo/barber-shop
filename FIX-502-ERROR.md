# 🔧 Correções para Erro 502

## Problemas Identificados e Corrigidos

### 1. ✅ Timeouts Aumentados
- **Antes**: 5s connect, 30s response
- **Agora**: 10s connect, 60s response
- **Localização**: `gateway-service/src/main/resources/application.yml`

### 2. ✅ Retry Configurado
- Adicionado retry com backoff exponencial
- 5 tentativas para rotas críticas
- Retry apenas em erros específicos (502, 503, 504)

### 3. ✅ Healthchecks Adicionados
- Todos os serviços agora têm healthchecks
- Gateway só inicia depois que os serviços estão saudáveis
- **Localização**: `docker-compose.yml`

### 4. ✅ Tratamento de Erros Melhorado
- GlobalExceptionHandler criado
- Mensagens de erro mais claras
- Retorno JSON estruturado

### 5. ✅ Connection Pool Configurado
- Pool tipo ELASTIC
- Max 500 conexões
- Max idle time: 30s

## Como Aplicar as Correções

### Passo 1: Parar tudo
```powershell
docker-compose down -v
```

### Passo 2: Reconstruir as imagens
```powershell
docker-compose build --no-cache
```

### Passo 3: Iniciar os serviços
```powershell
docker-compose up -d
```

### Passo 4: Aguardar todos os serviços ficarem saudáveis
```powershell
# Verificar status
docker-compose ps

# Ver logs
docker-compose logs -f
```

### Passo 5: Verificar se está funcionando
```powershell
# Testar gateway
curl http://localhost:8080/actuator/health

# Testar serviços
curl http://localhost:8080/api/services
```

## Verificações

### 1. Verificar se todos os serviços estão rodando
```powershell
docker-compose ps
```

Todos devem mostrar "Up" e healthcheck "healthy".

### 2. Verificar logs do gateway
```powershell
docker-compose logs gateway | Select-String -Pattern "error" -Context 3
```

### 3. Verificar logs do auth-service
```powershell
docker-compose logs auth-service | Select-String -Pattern "error" -Context 3
```

### 4. Verificar logs do booking-service
```powershell
docker-compose logs booking-service | Select-String -Pattern "error" -Context 3
```

## Se Ainda Der Erro 502

### Verificar conectividade entre containers
```powershell
# Dentro do container do gateway
docker exec -it barbershop-gateway sh
wget -O- http://auth-service:8081/actuator/health
wget -O- http://booking-service:8083/actuator/health
exit
```

### Verificar se os serviços estão escutando
```powershell
# Verificar auth-service
docker exec -it barbershop-auth sh
netstat -tuln | grep 8081
exit

# Verificar booking-service
docker exec -it barbershop-booking sh
netstat -tuln | grep 8083
exit
```

### Verificar variáveis de ambiente
```powershell
docker exec barbershop-gateway env | grep SERVICE_URL
```

Deve mostrar:
- AUTH_SERVICE_URL=http://auth-service:8081
- USER_SERVICE_URL=http://user-service:8082
- BOOKING_SERVICE_URL=http://booking-service:8083

## Troubleshooting Adicional

### Se os serviços não iniciam
1. Verificar logs completos:
   ```powershell
   docker-compose logs auth-service
   docker-compose logs booking-service
   ```

2. Verificar se o banco está acessível:
   ```powershell
   docker exec -it barbershop-postgres psql -U barbershop -d barbershop -c "SELECT 1;"
   ```

3. Verificar se Redis está acessível:
   ```powershell
   docker exec -it barbershop-redis redis-cli ping
   ```

### Se o gateway não consegue conectar
1. Verificar rede Docker:
   ```powershell
   docker network inspect barbershop_barbershop-network
   ```

2. Verificar se os containers estão na mesma rede:
   ```powershell
   docker network inspect barbershop_barbershop-network | Select-String "barbershop"
   ```

## Testes Finais

### Teste 1: Cadastro
```powershell
curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"teste@test.com\",\"password\":\"senha123\",\"fullName\":\"Teste\"}'
```

### Teste 2: Serviços
```powershell
curl http://localhost:8080/api/services
```

Ambos devem retornar sucesso (200 ou 201).


