# 🚀 Quick Start - BarberShop

Guia rápido para começar a usar o BarberShop em 5 minutos!

## ⚡ Início Rápido (Docker)

### 1. Clone e Configure

```bash
# Clone o repositório
git clone <repository-url>
cd BarberShop

# Configure o email
cp env.example .env
# Edite o arquivo .env com suas credenciais de email
```

### 2. Inicie os Serviços

```bash
# Inicie tudo com Docker Compose
docker-compose up -d

# Veja os logs
docker-compose logs -f
```

### 3. Acesse a Aplicação

Abra seu navegador em: **http://localhost:3000**

**Pronto!** 🎉

---

## 📧 Configuração Rápida do Email (Gmail)

### Passo 1: Ative a Verificação em 2 Etapas
1. Vá para: https://myaccount.google.com/security
2. Ative "Verificação em duas etapas"

### Passo 2: Gere uma Senha de App
1. Vá para: https://myaccount.google.com/apppasswords
2. Selecione "Email" e "Outro"
3. Copie a senha gerada (16 caracteres)

### Passo 3: Configure no .env
```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=seu-email@gmail.com
MAIL_PASSWORD=xxxx xxxx xxxx xxxx  # Cole a senha de app aqui
```

---

## 🎯 Primeiros Passos na Aplicação

### 1. Crie uma Conta
- Acesse http://localhost:3000
- Clique em "Cadastrar"
- Preencha seus dados
- **Importante**: Verifique seu email!

### 2. Faça seu Primeiro Agendamento
1. Faça login
2. Vá em "Serviços"
3. Escolha um serviço
4. Clique em "Agendar Agora"
5. Selecione data e horário
6. Confirme!

### 3. Gerencie seus Agendamentos
- Dashboard: Veja seus próximos agendamentos
- Meus Agendamentos: Histórico completo
- Cancele ou reagende quando necessário

---

## 🔍 Verificando se Está Tudo Funcionando

### Health Checks

```bash
# Frontend
curl http://localhost:3000

# Gateway
curl http://localhost:8080/actuator/health

# Auth Service
curl http://localhost:8081/actuator/health

# User Service
curl http://localhost:8082/actuator/health

# Booking Service
curl http://localhost:8083/actuator/health

# PostgreSQL
docker exec -it barbershop-postgres psql -U barbershop -d barbershop -c "SELECT 1;"

# Redis
docker exec -it barbershop-redis redis-cli ping
```

Todos devem responder com sucesso!

---

## 🧪 Testando a API

### 1. Registrar Usuário

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teste@example.com",
    "password": "senha123",
    "fullName": "João Silva",
    "phone": "11987654321"
  }'
```

**Resposta esperada:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "uuid-aqui",
  "email": "teste@example.com",
  "fullName": "João Silva",
  "role": "USER",
  "emailVerified": false
}
```

### 2. Fazer Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teste@example.com",
    "password": "senha123"
  }'
```

### 3. Listar Serviços (sem autenticação)

```bash
curl http://localhost:8080/api/services
```

### 4. Criar Agendamento (com autenticação)

```bash
# Substitua SEU_TOKEN pelo token recebido no login
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN" \
  -d '{
    "serviceId": "id-do-servico",
    "bookingDate": "2024-12-30",
    "bookingTime": "10:00",
    "notes": "Preferência por tesoura"
  }'
```

---

## 🛑 Parar e Limpar

### Parar os Serviços
```bash
docker-compose down
```

### Parar e Limpar Tudo (incluindo dados)
```bash
docker-compose down -v
```

### Reiniciar um Serviço Específico
```bash
docker-compose restart auth-service
```

---

## 🐛 Problemas Comuns

### Email não está sendo enviado
**Solução:**
1. Verifique as credenciais no `.env`
2. Use Senha de App, não sua senha normal
3. Veja os logs: `docker-compose logs auth-service`

### Erro de conexão ao banco
**Solução:**
```bash
# Aguarde o banco inicializar
docker-compose logs postgres

# Recrie os containers
docker-compose down -v
docker-compose up -d
```

### Frontend não carrega
**Solução:**
```bash
# Verifique se todos os serviços estão rodando
docker-compose ps

# Reconstrua o frontend
docker-compose up -d --build frontend
```

### Porta já em uso
**Solução:**
```bash
# Mude as portas no docker-compose.yml
# Exemplo: mudar 3000:3000 para 3001:3000
```

---

## 📊 Monitoramento

### Ver logs em tempo real

```bash
# Todos os serviços
docker-compose logs -f

# Serviço específico
docker-compose logs -f auth-service

# Últimas 100 linhas
docker-compose logs --tail=100
```

### Ver uso de recursos

```bash
docker stats
```

---

## 🎓 Próximos Passos

1. **Leia a documentação completa**: [README.md](./README.md)
2. **Entenda a arquitetura**: [ARCHITECTURE.md](./ARCHITECTURE.md)
3. **Deploy em produção**: [DEPLOYMENT.md](./DEPLOYMENT.md)
4. **Customize**: Adapte às suas necessidades!

---

## 💡 Dicas Úteis

### Desenvolvimento Local (sem Docker)

Se preferir rodar localmente:

```bash
# 1. Inicie apenas banco e redis
docker-compose up -d postgres redis

# 2. Execute cada microserviço
cd auth-service && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd booking-service && mvn spring-boot:run
cd gateway-service && mvn spring-boot:run

# 3. Execute o frontend
cd frontend && npm install && npm run dev
```

### Hot Reload no Frontend

```bash
cd frontend
npm run dev
# O Vite fará hot reload automático!
```

### Limpar cache do Maven

```bash
cd <service-directory>
mvn clean
```

---

## 📞 Precisa de Ajuda?

- 📖 [Documentação Completa](./README.md)
- 🏗️ [Arquitetura](./ARCHITECTURE.md)
- 🚀 [Deploy](./DEPLOYMENT.md)
- 🐛 Issues: Abra uma issue no GitHub

---

**Happy Coding! 💻✨**



