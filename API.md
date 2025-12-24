# 📡 API Documentation - BarberShop

Documentação completa da API REST do BarberShop.

**Base URL:** `http://localhost:8080/api`

---

## 🔐 Autenticação

Todas as rotas protegidas requerem um token JWT no header:

```http
Authorization: Bearer {token}
```

O token é obtido após login ou registro bem-sucedido.

---

## 📚 Endpoints

### 🔑 Autenticação

#### Registrar Usuário

```http
POST /auth/register
```

**Body:**
```json
{
  "email": "joao@example.com",
  "password": "senha123",
  "fullName": "João Silva",
  "phone": "11987654321"  // opcional
}
```

**Response (201):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "joao@example.com",
  "fullName": "João Silva",
  "role": "USER",
  "emailVerified": false
}
```

**Errors:**
- `400`: Email já cadastrado
- `400`: Dados inválidos

---

#### Login

```http
POST /auth/login
```

**Body:**
```json
{
  "email": "joao@example.com",
  "password": "senha123"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "joao@example.com",
  "fullName": "João Silva",
  "role": "USER",
  "emailVerified": true
}
```

**Errors:**
- `401`: Email ou senha inválidos

---

#### Verificar Email

```http
GET /auth/verify/{token}
```

**Response (200):**
```json
{
  "message": "Email verificado com sucesso"
}
```

**Errors:**
- `400`: Token inválido ou expirado

---

#### Reenviar Email de Verificação

```http
POST /auth/resend-verification
```

**Body:**
```json
{
  "email": "joao@example.com"
}
```

**Response (200):**
```json
{
  "message": "Email de verificação reenviado"
}
```

**Errors:**
- `400`: Email já verificado
- `404`: Usuário não encontrado

---

### 👤 Usuários

#### Obter Perfil Atual 🔒

```http
GET /users/me
Authorization: Bearer {token}
```

**Response (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "joao@example.com",
  "fullName": "João Silva",
  "phone": "11987654321",
  "role": "USER",
  "emailVerified": true,
  "createdAt": "2024-01-15T10:30:00"
}
```

---

#### Atualizar Perfil 🔒

```http
PUT /users/me
Authorization: Bearer {token}
```

**Body:**
```json
{
  "fullName": "João Silva Santos",
  "phone": "11999887766"
}
```

**Response (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "joao@example.com",
  "fullName": "João Silva Santos",
  "phone": "11999887766",
  "role": "USER",
  "emailVerified": true,
  "createdAt": "2024-01-15T10:30:00"
}
```

---

#### Deletar Conta 🔒

```http
DELETE /users/me
Authorization: Bearer {token}
```

**Response (200):**
```json
{
  "message": "Usuário deletado com sucesso"
}
```

---

### 💈 Serviços

#### Listar Serviços

```http
GET /services
```

**Response (200):**
```json
[
  {
    "id": "650e8400-e29b-41d4-a716-446655440000",
    "name": "Corte de Cabelo",
    "description": "Corte de cabelo masculino tradicional",
    "durationMinutes": 30,
    "price": 35.00,
    "active": true
  },
  {
    "id": "750e8400-e29b-41d4-a716-446655440000",
    "name": "Barba",
    "description": "Fazer a barba completa com navalha",
    "durationMinutes": 20,
    "price": 25.00,
    "active": true
  }
]
```

---

#### Obter Serviço por ID

```http
GET /services/{serviceId}
```

**Response (200):**
```json
{
  "id": "650e8400-e29b-41d4-a716-446655440000",
  "name": "Corte de Cabelo",
  "description": "Corte de cabelo masculino tradicional",
  "durationMinutes": 30,
  "price": 35.00,
  "active": true
}
```

**Errors:**
- `404`: Serviço não encontrado

---

### 📅 Agendamentos

#### Criar Agendamento 🔒

```http
POST /bookings
Authorization: Bearer {token}
```

**Body:**
```json
{
  "serviceId": "650e8400-e29b-41d4-a716-446655440000",
  "bookingDate": "2024-12-30",
  "bookingTime": "10:00",
  "notes": "Preferência por tesoura"  // opcional
}
```

**Response (201):**
```json
{
  "id": "850e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "service": {
    "id": "650e8400-e29b-41d4-a716-446655440000",
    "name": "Corte de Cabelo",
    "description": "Corte de cabelo masculino tradicional",
    "durationMinutes": 30,
    "price": 35.00,
    "active": true
  },
  "bookingDate": "2024-12-30",
  "bookingTime": "10:00",
  "status": "PENDING",
  "notes": "Preferência por tesoura",
  "createdAt": "2024-12-25T15:30:00"
}
```

**Errors:**
- `400`: Data no passado
- `400`: Horário já agendado
- `400`: Dados inválidos
- `404`: Serviço não encontrado

---

#### Listar Meus Agendamentos 🔒

```http
GET /bookings/my-bookings
Authorization: Bearer {token}
```

**Response (200):**
```json
[
  {
    "id": "850e8400-e29b-41d4-a716-446655440000",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "service": {
      "id": "650e8400-e29b-41d4-a716-446655440000",
      "name": "Corte de Cabelo",
      "description": "Corte de cabelo masculino tradicional",
      "durationMinutes": 30,
      "price": 35.00,
      "active": true
    },
    "bookingDate": "2024-12-30",
    "bookingTime": "10:00",
    "status": "CONFIRMED",
    "notes": "Preferência por tesoura",
    "createdAt": "2024-12-25T15:30:00"
  }
]
```

---

#### Listar Próximos Agendamentos 🔒

```http
GET /bookings/upcoming
Authorization: Bearer {token}
```

Retorna apenas agendamentos com status `PENDING` ou `CONFIRMED`, ordenados por data/hora.

**Response (200):**
```json
[
  {
    "id": "850e8400-e29b-41d4-a716-446655440000",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "service": {
      "id": "650e8400-e29b-41d4-a716-446655440000",
      "name": "Corte de Cabelo",
      "description": "Corte de cabelo masculino tradicional",
      "durationMinutes": 30,
      "price": 35.00,
      "active": true
    },
    "bookingDate": "2024-12-30",
    "bookingTime": "10:00",
    "status": "PENDING",
    "notes": null,
    "createdAt": "2024-12-25T15:30:00"
  }
]
```

---

#### Obter Agendamento por ID 🔒

```http
GET /bookings/{bookingId}
Authorization: Bearer {token}
```

**Response (200):**
```json
{
  "id": "850e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "service": {
    "id": "650e8400-e29b-41d4-a716-446655440000",
    "name": "Corte de Cabelo",
    "description": "Corte de cabelo masculino tradicional",
    "durationMinutes": 30,
    "price": 35.00,
    "active": true
  },
  "bookingDate": "2024-12-30",
  "bookingTime": "10:00",
  "status": "PENDING",
  "notes": "Preferência por tesoura",
  "createdAt": "2024-12-25T15:30:00"
}
```

**Errors:**
- `404`: Agendamento não encontrado

---

#### Cancelar Agendamento 🔒

```http
PUT /bookings/{bookingId}/cancel
Authorization: Bearer {token}
```

**Response (200):**
```json
{
  "id": "850e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "service": {
    "id": "650e8400-e29b-41d4-a716-446655440000",
    "name": "Corte de Cabelo",
    "description": "Corte de cabelo masculino tradicional",
    "durationMinutes": 30,
    "price": 35.00,
    "active": true
  },
  "bookingDate": "2024-12-30",
  "bookingTime": "10:00",
  "status": "CANCELLED",
  "notes": "Preferência por tesoura",
  "createdAt": "2024-12-25T15:30:00"
}
```

**Errors:**
- `400`: Agendamento já cancelado
- `400`: Não é possível cancelar agendamento concluído
- `403`: Você não tem permissão para cancelar este agendamento
- `404`: Agendamento não encontrado

---

#### Deletar Agendamento 🔒

```http
DELETE /bookings/{bookingId}
Authorization: Bearer {token}
```

**Response (200):**
```json
{
  "message": "Agendamento deletado com sucesso"
}
```

**Errors:**
- `403`: Você não tem permissão para deletar este agendamento
- `404`: Agendamento não encontrado

---

## 📊 Status Codes

| Code | Descrição |
|------|-----------|
| 200  | OK - Requisição bem-sucedida |
| 201  | Created - Recurso criado com sucesso |
| 400  | Bad Request - Dados inválidos |
| 401  | Unauthorized - Token ausente ou inválido |
| 403  | Forbidden - Sem permissão |
| 404  | Not Found - Recurso não encontrado |
| 500  | Internal Server Error - Erro no servidor |

---

## 🔑 Enums

### BookingStatus
- `PENDING` - Aguardando confirmação
- `CONFIRMED` - Confirmado
- `CANCELLED` - Cancelado
- `COMPLETED` - Concluído

### UserRole
- `USER` - Usuário comum
- `BARBER` - Barbeiro
- `ADMIN` - Administrador

---

## 🧪 Exemplos de Uso

### Exemplo Completo: Criar uma conta e agendar

```bash
# 1. Registrar
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teste@example.com",
    "password": "senha123",
    "fullName": "João Teste"
  }' | jq -r '.token')

echo "Token: $TOKEN"

# 2. Listar serviços
SERVICE_ID=$(curl -s http://localhost:8080/api/services | jq -r '.[0].id')
echo "Service ID: $SERVICE_ID"

# 3. Criar agendamento
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"serviceId\": \"$SERVICE_ID\",
    \"bookingDate\": \"2024-12-30\",
    \"bookingTime\": \"10:00\"
  }" | jq

# 4. Listar meus agendamentos
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/bookings/my-bookings | jq
```

---

## 📦 Postman Collection

Importe a collection do Postman para testes mais fáceis:

```json
{
  "info": {
    "name": "BarberShop API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080/api"
    },
    {
      "key": "token",
      "value": ""
    }
  ]
}
```

---

## 🔒 Segurança

### Headers de Segurança
- `X-User-Id`: Adicionado automaticamente pelo Gateway
- `X-User-Email`: Adicionado automaticamente pelo Gateway
- `X-User-Role`: Adicionado automaticamente pelo Gateway

### Rate Limiting
- Por IP: 100 requisições/minuto
- Por usuário: 50 requisições/minuto

---

## 📚 Mais Informações

- [README Principal](./README.md)
- [Arquitetura](./ARCHITECTURE.md)
- [Quick Start](./QUICKSTART.md)

**Happy API Testing! 🚀**



