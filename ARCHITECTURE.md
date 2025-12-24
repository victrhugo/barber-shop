# 🏗️ Arquitetura do Sistema BarberShop

## Visão Geral

O BarberShop é uma aplicação de agendamento online construída com arquitetura de **microserviços**, garantindo escalabilidade, manutenibilidade e isolamento de responsabilidades.

## Princípios Arquiteturais

### 1. Microserviços
Cada serviço é independente e responsável por um domínio específico:
- **Auth Service**: Autenticação e autorização
- **User Service**: Gerenciamento de usuários
- **Booking Service**: Gerenciamento de agendamentos e serviços

### 2. API Gateway Pattern
- Ponto único de entrada para todos os clientes
- Validação de JWT
- Roteamento inteligente
- Rate limiting
- CORS handling

### 3. Database per Service
- Cada microserviço tem acesso ao mesmo banco PostgreSQL
- Poderia ser separado em bancos diferentes para maior isolamento
- Compartilhamento da tabela `users` entre serviços

### 4. Stateless Services
- Serviços não mantêm estado entre requisições
- Estado armazenado em Redis (sessões, cache)
- JWT para autenticação stateless

## Componentes

### Frontend (React + TypeScript)

```
frontend/
├── src/
│   ├── components/      # Componentes reutilizáveis
│   ├── pages/           # Páginas da aplicação
│   ├── services/        # Camada de serviços (API calls)
│   ├── store/           # Estado global (Zustand)
│   ├── lib/             # Utilitários e configurações
│   └── App.tsx          # Componente principal
├── public/              # Assets estáticos
└── Dockerfile           # Container para produção
```

**Responsabilidades:**
- Interface do usuário
- Validação de formulários
- Gerenciamento de estado local
- Comunicação com API Gateway

**Stack:**
- React 18 + TypeScript
- Tailwind CSS
- React Query (cache e sincronização)
- Zustand (state management)
- React Router (navegação)

### API Gateway (Spring Cloud Gateway)

```
gateway-service/
├── src/
│   └── main/
│       └── java/
│           └── com/barbershop/gateway/
│               ├── config/         # Configurações
│               ├── filter/         # Filtros customizados
│               └── util/           # Utilitários (JWT)
└── application.yml
```

**Responsabilidades:**
- Roteamento de requisições
- Validação JWT
- Injeção de headers (X-User-Id, X-User-Email, X-User-Role)
- CORS
- Rate limiting (com Redis)

**Fluxo de Requisição:**
1. Cliente envia requisição com JWT
2. Gateway valida token
3. Extrai informações do usuário
4. Adiciona headers para microserviços
5. Roteia para serviço apropriado

### Auth Service

```
auth-service/
├── src/
│   └── main/
│       └── java/
│           └── com/barbershop/auth/
│               ├── controller/     # REST Controllers
│               ├── service/        # Lógica de negócio
│               ├── repository/     # Acesso a dados
│               ├── entity/         # Entidades JPA
│               ├── dto/            # Data Transfer Objects
│               ├── config/         # Configurações
│               └── util/           # Utilitários (JWT)
└── application.yml
```

**Responsabilidades:**
- Registro de usuários
- Login/Logout
- Geração de tokens JWT
- Verificação de email
- Gestão de tokens de verificação

**Endpoints:**
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/verify/{token}`
- `POST /api/auth/resend-verification`

### User Service

```
user-service/
├── src/
│   └── main/
│       └── java/
│           └── com/barbershop/user/
│               ├── controller/
│               ├── service/
│               ├── repository/
│               ├── entity/
│               └── dto/
└── application.yml
```

**Responsabilidades:**
- Gerenciamento de perfil
- Atualização de informações
- Deleção de conta
- Consulta de usuários

**Endpoints:**
- `GET /api/users/me`
- `PUT /api/users/me`
- `DELETE /api/users/me`
- `GET /api/users/{id}`

### Booking Service

```
booking-service/
├── src/
│   └── main/
│       └── java/
│           └── com/barbershop/booking/
│               ├── controller/
│               ├── service/
│               ├── repository/
│               ├── entity/
│               └── dto/
└── application.yml
```

**Responsabilidades:**
- Gerenciamento de serviços (corte, barba, etc)
- Criação de agendamentos
- Listagem de agendamentos
- Cancelamento
- Validação de conflitos de horário

**Endpoints:**
- `GET /api/services`
- `POST /api/bookings`
- `GET /api/bookings/my-bookings`
- `PUT /api/bookings/{id}/cancel`
- `DELETE /api/bookings/{id}`

## Fluxos Principais

### 1. Registro de Usuário

```
Cliente → Gateway → Auth Service → PostgreSQL
                                  ↓
                             Email Service
                                  ↓
                            Usuário recebe email
```

1. Cliente envia dados de registro
2. Gateway roteia para Auth Service
3. Auth Service:
   - Valida dados
   - Hash da senha (BCrypt)
   - Cria usuário no banco
   - Gera token de verificação
   - Envia email de verificação
4. Retorna JWT para login imediato

### 2. Login

```
Cliente → Gateway → Auth Service → PostgreSQL
                          ↓
                    Valida credenciais
                          ↓
                    Gera JWT Token
                          ↓
                    Retorna token
```

### 3. Criar Agendamento

```
Cliente → Gateway (valida JWT) → Booking Service → PostgreSQL
              ↓                          ↓
       Injeta headers            Valida conflitos
       (X-User-Id)                      ↓
                                 Cria agendamento
```

### 4. Verificação de Email

```
Cliente clica no link → Gateway → Auth Service → PostgreSQL
                                        ↓
                                  Valida token
                                        ↓
                                 Marca como verificado
```

## Segurança

### JWT (JSON Web Token)

**Estrutura do Token:**
```json
{
  "sub": "user-uuid",
  "email": "user@example.com",
  "role": "USER",
  "fullName": "John Doe",
  "emailVerified": true,
  "iat": 1234567890,
  "exp": 1234654290
}
```

**Fluxo:**
1. Usuário faz login
2. Auth Service gera JWT com claims
3. Cliente armazena token (localStorage)
4. Cliente envia token em cada requisição
5. Gateway valida e extrai informações
6. Microserviços recebem info via headers

### Criptografia
- **Senhas**: BCrypt com salt
- **JWT**: HMAC-SHA256
- **HTTPS**: Recomendado em produção

### Proteção de Rotas
- Rotas públicas: /auth/register, /auth/login, /services
- Rotas protegidas: Todas as outras requerem JWT válido

## Comunicação

### Cliente ↔ Gateway
- **Protocolo**: HTTP/HTTPS
- **Formato**: JSON
- **Autenticação**: Bearer Token (JWT)

### Gateway ↔ Microserviços
- **Protocolo**: HTTP (pode ser gRPC no futuro)
- **Formato**: JSON
- **Headers customizados**:
  - `X-User-Id`: Identificador do usuário
  - `X-User-Email`: Email do usuário
  - `X-User-Role`: Papel do usuário

### Microserviços ↔ Banco de Dados
- **Protocolo**: JDBC
- **ORM**: Hibernate/JPA
- **Connection Pool**: HikariCP

## Escalabilidade

### Horizontal Scaling
Cada microserviço pode ser escalado independentemente:

```bash
# Escalar Booking Service
docker-compose up -d --scale booking-service=3

# Escalar User Service
docker-compose up -d --scale user-service=2
```

### Caching (Redis)
- Cache de sessões
- Cache de queries frequentes
- Rate limiting
- Blacklist de tokens

### Database Optimization
- Índices em colunas frequentemente consultadas
- Connection pooling
- Query optimization
- Possibilidade de read replicas

## Monitoramento e Observabilidade

### Health Checks
Cada serviço expõe endpoint de health:
- `GET /actuator/health`

### Logs
- Logs estruturados (JSON)
- Níveis: DEBUG, INFO, WARN, ERROR
- Centralização recomendada (ELK Stack, Splunk)

### Metrics (Futuro)
- Prometheus
- Grafana
- Micrometer

## Resiliência

### Circuit Breaker (Futuro)
- Resilience4j
- Proteção contra falhas em cascata

### Retry Logic
- Retry automático em falhas temporárias
- Backoff exponencial

### Timeouts
- Timeouts configurados em cada serviço
- Previne bloqueio infinito

## Performance

### Frontend
- Code splitting
- Lazy loading de rotas
- Otimização de imagens
- Caching de assets
- Gzip compression

### Backend
- Connection pooling
- Query optimization
- Índices de banco
- Cache (Redis)
- Async processing

## Evolução Futura

### Possíveis Melhorias

1. **Event-Driven Architecture**
   - Kafka/RabbitMQ para comunicação assíncrona
   - Event sourcing para histórico

2. **Service Mesh**
   - Istio/Linkerd
   - Observabilidade avançada
   - Traffic management

3. **API Versioning**
   - Versionamento de APIs
   - Backward compatibility

4. **Distributed Tracing**
   - Jaeger/Zipkin
   - Rastreamento end-to-end

5. **CQRS Pattern**
   - Separação de leitura e escrita
   - Melhor performance

6. **GraphQL**
   - Gateway GraphQL
   - Queries mais flexíveis

## Conclusão

A arquitetura de microserviços do BarberShop foi projetada para:
- ✅ Escalabilidade
- ✅ Manutenibilidade
- ✅ Resiliência
- ✅ Performance
- ✅ Segurança

Esta base sólida permite crescimento e evolução contínua do sistema.



