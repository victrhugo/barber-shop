# 🔧 Troubleshooting - BarberShop

## Problemas Comuns e Soluções

### ❌ Cadastro dando erro mesmo preenchendo os campos

#### Possíveis Causas:

1. **Email já cadastrado**
   - **Solução**: Use um email diferente ou faça login com o email existente

2. **Senha muito curta**
   - **Solução**: A senha deve ter no mínimo 6 caracteres

3. **Erro de conexão com backend**
   - **Verificar**: 
     ```bash
     # Verifique se o gateway está rodando
     curl http://localhost:8080/actuator/health
     
     # Verifique se o auth-service está rodando
     curl http://localhost:8081/actuator/health
     ```

4. **Erro de validação no backend**
   - **Verificar logs**:
     ```bash
     docker-compose logs auth-service
     ```

#### Como debugar:

1. **Abra o Console do Navegador (F12)**
   - Vá em "Console" ou "Network"
   - Tente cadastrar novamente
   - Veja a mensagem de erro exata

2. **Verifique os logs do backend**:
   ```bash
   docker-compose logs -f auth-service
   ```

3. **Teste a API diretamente**:
   ```bash
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "email": "teste@example.com",
       "password": "senha123",
       "fullName": "João Teste"
     }'
   ```

---

### ❌ Página de Serviços não carrega ou aparece vazia

#### Possíveis Causas:

1. **Serviços não foram criados no banco**
   - **Solução**: Execute o script SQL manualmente
   ```bash
   # Conecte ao banco
   docker exec -it barbershop-postgres psql -U barbershop -d barbershop
   
   # Execute o INSERT
   INSERT INTO services (name, description, duration_minutes, price) VALUES
       ('Corte de Cabelo', 'Corte de cabelo masculino tradicional', 30, 35.00),
       ('Barba', 'Fazer a barba completa com navalha', 20, 25.00),
       ('Cabelo + Barba', 'Corte de cabelo e barba', 45, 55.00),
       ('Cabelo + Sobrancelha', 'Corte de cabelo e design de sobrancelha', 40, 45.00),
       ('Pacote Completo', 'Cabelo, barba e sobrancelha', 60, 70.00),
       ('Sobrancelha', 'Design de sobrancelha', 15, 15.00);
   ```

2. **Erro de conexão com booking-service**
   - **Verificar**:
     ```bash
     curl http://localhost:8083/actuator/health
     curl http://localhost:8080/api/services
     ```

3. **Problema de CORS**
   - **Verificar**: Abra o Console do navegador (F12) e veja se há erros de CORS

4. **Gateway não está roteando corretamente**
   - **Verificar logs**:
     ```bash
     docker-compose logs gateway
     ```

#### Como debugar:

1. **Teste a API diretamente**:
   ```bash
   # Teste sem passar pelo gateway
   curl http://localhost:8083/api/services
   
   # Teste pelo gateway
   curl http://localhost:8080/api/services
   ```

2. **Verifique se os serviços existem no banco**:
   ```bash
   docker exec -it barbershop-postgres psql -U barbershop -d barbershop -c "SELECT * FROM services;"
   ```

3. **Verifique os logs**:
   ```bash
   docker-compose logs booking-service
   docker-compose logs gateway
   ```

4. **Abra o Console do Navegador (F12)**
   - Vá em "Network"
   - Recarregue a página de serviços
   - Veja a requisição para `/api/services`
   - Verifique o status code e a resposta

---

### 🔍 Verificações Gerais

#### 1. Todos os serviços estão rodando?

```bash
docker-compose ps
```

Todos devem estar com status "Up".

#### 2. Banco de dados está acessível?

```bash
docker exec -it barbershop-postgres psql -U barbershop -d barbershop -c "SELECT 1;"
```

#### 3. Redis está funcionando?

```bash
docker exec -it barbershop-redis redis-cli ping
```

Deve retornar "PONG".

#### 4. Gateway está roteando corretamente?

```bash
# Teste uma rota pública
curl http://localhost:8080/api/services

# Teste uma rota de autenticação
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"teste@test.com","password":"senha"}'
```

#### 5. Frontend está conectando ao backend?

- Abra o Console do navegador (F12)
- Vá em "Network"
- Faça uma requisição
- Verifique se está indo para `http://localhost:8080/api`

---

### 🛠️ Soluções Rápidas

#### Reiniciar todos os serviços:

```bash
docker-compose down
docker-compose up -d
```

#### Recriar o banco de dados:

```bash
docker-compose down -v
docker-compose up -d postgres
# Aguarde alguns segundos
docker-compose up -d
```

#### Ver logs em tempo real:

```bash
# Todos os serviços
docker-compose logs -f

# Serviço específico
docker-compose logs -f auth-service
docker-compose logs -f booking-service
docker-compose logs -f gateway
```

#### Limpar e reconstruir:

```bash
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

---

### 📝 Mensagens de Erro Comuns

#### "Email já cadastrado"
- **Causa**: Tentando cadastrar com email que já existe
- **Solução**: Use outro email ou faça login

#### "Email ou senha inválidos"
- **Causa**: Credenciais incorretas
- **Solução**: Verifique email e senha

#### "Token inválido ou expirado"
- **Causa**: Token JWT expirado ou inválido
- **Solução**: Faça login novamente

#### "Serviço não encontrado"
- **Causa**: ID do serviço inválido ou serviço não existe
- **Solução**: Verifique se o serviço existe no banco

#### "Já existe um agendamento para este horário"
- **Causa**: Tentando agendar em horário já ocupado
- **Solução**: Escolha outro horário

---

### 🆘 Ainda com problemas?

1. **Verifique os logs completos**:
   ```bash
   docker-compose logs > logs.txt
   ```

2. **Verifique a versão do Docker**:
   ```bash
   docker --version
   docker-compose --version
   ```

3. **Verifique se as portas estão livres**:
   ```bash
   # Windows PowerShell
   netstat -ano | findstr :8080
   netstat -ano | findstr :3000
   ```

4. **Abra uma issue no GitHub** com:
   - Mensagem de erro completa
   - Logs dos serviços
   - Passos para reproduzir

---

**Boa sorte! 🍀**



