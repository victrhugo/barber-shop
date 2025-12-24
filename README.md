# 💈 BarberShop - Sistema de Agendamento Online

Sistema completo de agendamento para barbearias desenvolvido com **microserviços** em Java/Spring Boot e frontend moderno em React + TypeScript.

## 🚀 Tecnologias

### Backend
- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Cloud Gateway** - API Gateway
- **Spring Security** - Autenticação e autorização
- **JWT** - Tokens de autenticação
- **PostgreSQL** - Banco de dados
- **Redis** - Cache e sessões
- **JavaMailSender** - Envio de emails
- **Docker & Docker Compose** - Containerização

### Frontend
- **React 18** - Framework UI
- **TypeScript** - Tipagem estática
- **Vite** - Build tool
- **Tailwind CSS** - Estilização
- **React Query** - Gerenciamento de estado
- **React Router** - Roteamento
- **Zustand** - State management
- **Axios** - Requisições HTTP

## 📋 Funcionalidades

### Usuário
- ✅ Cadastro de usuário
- ✅ Login com JWT
- ✅ Confirmação de email (JavaMailSender)
- ✅ Gerenciamento de perfil
- ✅ Visualizar serviços disponíveis
- ✅ Criar agendamentos
- ✅ Visualizar histórico de agendamentos
- ✅ Cancelar agendamentos

### Serviços Disponíveis
- Corte de Cabelo
- Barba
- Cabelo + Barba
- Cabelo + Sobrancelha
- Pacote Completo
- Sobrancelha

## 🏗️ Arquitetura

```
┌─────────────┐
│   Frontend  │ (React + TypeScript + Tailwind)
│  Port: 3000 │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Gateway   │ (Spring Cloud Gateway)
│  Port: 8080 │ - Autenticação JWT
└──────┬──────┘ - Rate Limiting
       │         - CORS
       ├────────────────────┬────────────────┐
       ▼                    ▼                ▼
┌──────────┐        ┌──────────┐    ┌──────────┐
│   Auth   │        │   User   │    │ Booking  │
│ Service  │        │ Service  │    │ Service  │
│Port: 8081│        │Port: 8082│    │Port: 8083│
└────┬─────┘        └────┬─────┘    └────┬─────┘
     │                   │               │
     └───────────┬───────┴───────┬───────┘
                 ▼               ▼
         ┌─────────────┐   ┌─────────┐
         │ PostgreSQL  │   │  Redis  │
         │  Port: 5432 │   │Port: 6379│
         └─────────────┘   └─────────┘
```

## 🛠️ Instalação e Execução

### Pré-requisitos
- **Docker** e **Docker Compose** instalados
- **JDK 17+** (se for rodar sem Docker)
- **Node.js 18+** (se for rodar frontend sem Docker)
- **Maven 3.9+** (se for rodar backend sem Docker)

### 1️⃣ Clonar o Repositório

```bash
git clone <repository-url>
cd BarberShop
```

### 2️⃣ Configurar Variáveis de Ambiente

Copie o arquivo `.env.example` para `.env` e configure:

```bash
cp .env.example .env
```

Edite o arquivo `.env` e configure seu email:

```env
# Gmail Configuration (exemplo)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=seu-email@gmail.com
MAIL_PASSWORD=sua-senha-de-app

# JWT Secret (mude em produção!)
JWT_SECRET=sua-chave-secreta-muito-segura-aqui

# URLs
FRONTEND_URL=http://localhost:3000
BACKEND_URL=http://localhost:8080
```

**⚠️ Importante:** Para usar Gmail:
1. Ative a verificação em 2 etapas
2. Gere uma "Senha de App" em: https://myaccount.google.com/apppasswords
3. Use essa senha de app no `MAIL_PASSWORD`

### 3️⃣ Executar com Docker Compose

```bash
# Iniciar todos os serviços
docker-compose up -d

# Ver logs
docker-compose logs -f

# Parar todos os serviços
docker-compose down

# Parar e remover volumes (dados)
docker-compose down -v
```

### 4️⃣ Acessar a Aplicação

- **Frontend:** http://localhost:3000
- **Gateway API:** http://localhost:8080
- **Auth Service:** http://localhost:8081
- **User Service:** http://localhost:8082
- **Booking Service:** http://localhost:8083
- **PostgreSQL:** localhost:5432
- **Redis:** localhost:6379

## 🔧 Desenvolvimento Local (Sem Docker)

### Backend

#### 1. Iniciar PostgreSQL e Redis
```bash
docker run -d -p 5432:5432 -e POSTGRES_DB=barbershop -e POSTGRES_USER=barbershop -e POSTGRES_PASSWORD=barbershop123 postgres:15
docker run -d -p 6379:6379 redis:7-alpine
```

#### 2. Executar cada serviço

**Gateway Service:**
```bash
cd gateway-service
mvn spring-boot:run
```

**Auth Service:**
```bash
cd auth-service
mvn spring-boot:run
```

**User Service:**
```bash
cd user-service
mvn spring-boot:run
```

**Booking Service:**
```bash
cd booking-service
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## 📚 API Endpoints

### Autenticação (Públicos)
- `POST /api/auth/register` - Cadastrar usuário
- `POST /api/auth/login` - Login
- `GET /api/auth/verify/{token}` - Verificar email
- `POST /api/auth/resend-verification` - Reenviar email de verificação

### Usuários (Protegidos)
- `GET /api/users/me` - Obter perfil atual
- `PUT /api/users/me` - Atualizar perfil
- `DELETE /api/users/me` - Deletar conta

### Serviços (Públicos)
- `GET /api/services` - Listar serviços
- `GET /api/services/{id}` - Obter serviço por ID

### Agendamentos (Protegidos)
- `POST /api/bookings` - Criar agendamento
- `GET /api/bookings/my-bookings` - Listar meus agendamentos
- `GET /api/bookings/upcoming` - Listar próximos agendamentos
- `GET /api/bookings/{id}` - Obter agendamento por ID
- `PUT /api/bookings/{id}/cancel` - Cancelar agendamento
- `DELETE /api/bookings/{id}` - Deletar agendamento

## 🔐 Segurança

### JWT Authentication
Todos os endpoints protegidos requerem um token JWT no header:

```
Authorization: Bearer {token}
```

O Gateway valida o token e adiciona headers para os microserviços:
- `X-User-Id`: ID do usuário
- `X-User-Email`: Email do usuário
- `X-User-Role`: Role do usuário

### CORS
Configurado para aceitar requisições de:
- http://localhost:3000 (desenvolvimento)
- http://localhost:80 (produção Docker)

## 📧 Configuração de Email

### Gmail
```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=seu-email@gmail.com
MAIL_PASSWORD=senha-de-app
```

### Outlook/Hotmail
```env
MAIL_HOST=smtp-mail.outlook.com
MAIL_PORT=587
MAIL_USERNAME=seu-email@outlook.com
MAIL_PASSWORD=sua-senha
```

### Outros Provedores
Consulte a documentação do seu provedor de email para configurar SMTP.

## 🗄️ Banco de Dados

### Schema Principal

**users** - Usuários do sistema
- id (UUID)
- email (VARCHAR)
- password (VARCHAR)
- full_name (VARCHAR)
- phone (VARCHAR)
- role (VARCHAR)
- email_verified (BOOLEAN)
- verification_token (VARCHAR)

**services** - Serviços oferecidos
- id (UUID)
- name (VARCHAR)
- description (TEXT)
- duration_minutes (INTEGER)
- price (DECIMAL)
- active (BOOLEAN)

**bookings** - Agendamentos
- id (UUID)
- user_id (UUID)
- service_id (UUID)
- booking_date (DATE)
- booking_time (TIME)
- status (VARCHAR)
- notes (TEXT)

## 🎨 Interface do Usuário

### Páginas
- **Home** - Página inicial com apresentação
- **Login** - Autenticação de usuários
- **Registro** - Cadastro de novos usuários
- **Dashboard** - Painel do usuário
- **Serviços** - Listagem de serviços
- **Agendamentos** - Gerenciamento de agendamentos
- **Novo Agendamento** - Criar novo agendamento
- **Perfil** - Gerenciar perfil do usuário

### Design
- Interface moderna e responsiva
- Tailwind CSS para estilização
- Lucide React para ícones
- Toast notifications
- Loading states
- Error handling

## 🧪 Testes

### Backend
```bash
cd <service-name>
mvn test
```

### Frontend
```bash
cd frontend
npm test
```

## 📦 Build para Produção

### Backend
```bash
cd <service-name>
mvn clean package
```

### Frontend
```bash
cd frontend
npm run build
```

### Docker (Todos os Serviços)
```bash
docker-compose build
docker-compose up -d
```

## 🚀 Deploy

### Recomendações de Deploy

**Backend:**
- AWS ECS/EKS
- Google Cloud Run
- Azure Container Instances
- Heroku

**Frontend:**
- Vercel
- Netlify
- AWS S3 + CloudFront
- GitHub Pages

**Banco de Dados:**
- AWS RDS PostgreSQL
- Google Cloud SQL
- Azure Database for PostgreSQL
- Supabase

## 🔧 Troubleshooting

### Erro de conexão com banco de dados
```bash
# Verificar se PostgreSQL está rodando
docker ps | grep postgres

# Ver logs do PostgreSQL
docker logs barbershop-postgres
```

### Erro de autenticação JWT
- Verifique se o `JWT_SECRET` é o mesmo em todos os serviços
- Confirme que o token está sendo enviado no formato correto

### Email não está sendo enviado
- Verifique as credenciais SMTP no arquivo `.env`
- Para Gmail, use Senha de App, não a senha normal
- Verifique os logs do auth-service

### Frontend não conecta com backend
- Verifique se todos os serviços estão rodando
- Confirme a configuração do proxy no `vite.config.ts`
- Verifique configuração de CORS no Gateway

## 📝 Melhorias Futuras

- [ ] Sistema de notificações push
- [ ] Integração com calendário (Google Calendar)
- [ ] Sistema de avaliações e comentários
- [ ] Chat em tempo real
- [ ] Painel administrativo
- [ ] Relatórios e analytics
- [ ] Integração com pagamentos
- [ ] App mobile (React Native)
- [ ] Sistema de fidelidade/pontos
- [ ] Gerenciamento de barbeiros

## 👥 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT.

## 👨‍💻 Autor

Desenvolvido com ❤️ para demonstrar uma arquitetura moderna de microserviços.

## 📞 Suporte

Para dúvidas e suporte, abra uma issue no GitHub.

---

**Made with ☕ and ❤️ in Brazil**



