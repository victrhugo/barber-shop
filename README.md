# 💈 BarberShop - Online Booking System

Complete booking system for barbershops developed with **microservices** in Java/Spring Boot and modern frontend in React + TypeScript.

## 🚀 Technologies

### Backend
- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Cloud Gateway** - API Gateway
- **Spring Security** - Authentication and authorization
- **JWT** - Authentication tokens
- **PostgreSQL** - Database
- **Redis** - Cache and sessions
- **JavaMailSender** - Email sending
- **Docker & Docker Compose** - Containerization

### Frontend
- **React 18** - UI Framework
- **TypeScript** - Static typing
- **Vite** - Build tool
- **Tailwind CSS** - Styling
- **React Query** - State management
- **React Router** - Routing
- **Zustand** - State management
- **Axios** - HTTP requests

## 📋 Features

### User
- ✅ User registration
- ✅ JWT login
- ✅ Email confirmation (JavaMailSender)
- ✅ Profile management
- ✅ View available services
- ✅ Create bookings
- ✅ View booking history
- ✅ Cancel bookings

### Admin
- ✅ Admin dashboard
- ✅ Manage barbers (create, edit, deactivate)
- ✅ View all bookings
- ✅ Create barbers with ADMIN role (shop owners)
- ✅ Full system access

### Barber
- ✅ Barber dashboard
- ✅ View assigned bookings
- ✅ Confirm/complete/cancel bookings
- ✅ Manage schedule

### Available Services
- Haircut
- Beard
- Hair + Beard
- Hair + Eyebrow
- Complete Package
- Eyebrow

## 🏗️ Architecture

```
┌─────────────┐
│   Frontend  │ (React + TypeScript + Tailwind)
│  Port: 3000 │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Gateway   │ (Spring Cloud Gateway)
│  Port: 8080 │ - JWT Authentication
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

## 🛠️ Installation and Setup

### Prerequisites
- **Docker** and **Docker Compose** installed
- **JDK 17+** (if running without Docker)
- **Node.js 18+** (if running frontend without Docker)
- **Maven 3.9+** (if running backend without Docker)

### 1️⃣ Clone the Repository

```bash
git clone <repository-url>
cd barber-shop
```

### 2️⃣ Configure Environment Variables

Copy the `.env.example` file to `.env` and configure:

```bash
cp env.example .env
```

Edit the `.env` file and configure your email:

```env
# Gmail Configuration (example)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# JWT Secret (change in production!)
JWT_SECRET=your-very-secure-secret-key-here

# URLs
FRONTEND_URL=http://localhost:3000
BACKEND_URL=http://localhost:8080
```

**⚠️ Important:** To use Gmail:
1. Enable 2-step verification
2. Generate an "App Password" at: https://myaccount.google.com/apppasswords
3. Use that app password in `MAIL_PASSWORD`

### 3️⃣ Run with Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Stop and remove volumes (data)
docker-compose down -v
```

### 4️⃣ Access the Application

- **Frontend:** http://localhost:3000
- **Gateway API:** http://localhost:8080
- **Auth Service:** http://localhost:8081
- **User Service:** http://localhost:8082
- **Booking Service:** http://localhost:8083
- **PostgreSQL:** localhost:5432
- **Redis:** localhost:6379

## 👤 Default Admin Account

When the application starts for the first time, a default admin account is automatically created:

- **Email:** `admin@barbershop.com`
- **Password:** `admin123`

**⚠️ IMPORTANT:** Change the default admin password in production!

The admin account is created automatically by the `DataInitializer` component in the auth-service. If the admin already exists, it won't be recreated.

## 🔐 User Roles

### ADMIN
- Full system access
- Can access admin dashboard
- Can access barber dashboard (if also registered as barber)
- Can create barbers with BARBER or ADMIN role
- Can manage all bookings
- Can manage all barbers

### BARBER
- Access to barber dashboard
- Can view assigned bookings
- Can confirm/complete/cancel bookings
- Can manage their schedule

### USER
- Can create bookings
- Can view their own bookings
- Can cancel their bookings
- Can manage their profile

## 🔧 Local Development (Without Docker)

### Backend

#### 1. Start PostgreSQL and Redis
```bash
docker run -d -p 5432:5432 -e POSTGRES_DB=barbershop -e POSTGRES_USER=barbershop -e POSTGRES_PASSWORD=barbershop123 postgres:15
docker run -d -p 6379:6379 redis:7-alpine
```

#### 2. Run each service

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

### Authentication (Public)
- `POST /api/auth/register` - Register user
- `POST /api/auth/login` - Login
- `GET /api/auth/verify/{token}` - Verify email
- `POST /api/auth/resend-verification` - Resend verification email

### Users (Protected)
- `GET /api/users/me` - Get current profile
- `PUT /api/users/me` - Update profile
- `DELETE /api/users/me` - Delete account

### Services (Public)
- `GET /api/services` - List services
- `GET /api/services/{id}` - Get service by ID

### Bookings (Protected)
- `POST /api/bookings` - Create booking
- `GET /api/bookings/my-bookings` - List my bookings
- `GET /api/bookings/upcoming` - List upcoming bookings
- `GET /api/bookings/{id}` - Get booking by ID
- `PUT /api/bookings/{id}/cancel` - Cancel booking
- `DELETE /api/bookings/{id}` - Delete booking

### Admin (Protected - ADMIN role required)
- `GET /api/bookings/admin/all` - Get all bookings
- `POST /api/auth/admin/barbers` - Create barber (can create with ADMIN role)
- `GET /api/barbers/admin/all` - Get all barbers (including inactive)
- `PUT /api/barbers/{id}` - Update barber
- `DELETE /api/barbers/{id}` - Deactivate barber

### Barber (Protected - BARBER role required)
- `GET /api/bookings/barber/my-bookings` - Get barber bookings
- `GET /api/bookings/barber/upcoming` - Get upcoming bookings
- `PUT /api/bookings/barber/{id}/confirm` - Confirm booking
- `PUT /api/bookings/barber/{id}/complete` - Complete booking
- `PUT /api/bookings/barber/{id}/cancel` - Cancel booking

## 🔐 Security

### JWT Authentication
All protected endpoints require a JWT token in the header:

```
Authorization: Bearer {token}
```

The Gateway validates the token and adds headers for microservices:
- `X-User-Id`: User ID
- `X-User-Email`: User email
- `X-User-Role`: User role

### CORS
Configured to accept requests from:
- http://localhost:3000 (development)
- http://localhost:80 (Docker production)

## 📧 Email Configuration

### Gmail
```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=app-password
```

### Outlook/Hotmail
```env
MAIL_HOST=smtp-mail.outlook.com
MAIL_PORT=587
MAIL_USERNAME=your-email@outlook.com
MAIL_PASSWORD=your-password
```

### Other Providers
Consult your email provider's documentation for SMTP configuration.

## 🗄️ Database

### Main Schema

**users** - System users
- id (UUID)
- email (VARCHAR)
- password (VARCHAR)
- full_name (VARCHAR)
- phone (VARCHAR)
- role (VARCHAR) - USER, BARBER, or ADMIN
- email_verified (BOOLEAN)
- verification_token (VARCHAR)

**services** - Offered services
- id (UUID)
- name (VARCHAR)
- description (TEXT)
- duration_minutes (INTEGER)
- price (DECIMAL)
- active (BOOLEAN)

**bookings** - Bookings
- id (UUID)
- user_id (UUID)
- service_id (UUID)
- barber_id (UUID)
- booking_date (DATE)
- booking_time (TIME)
- status (VARCHAR)
- notes (TEXT)

**barbers** - Barbers
- id (UUID)
- user_id (UUID)
- specialties (TEXT[])
- bio (TEXT)
- rating (DECIMAL)
- active (BOOLEAN)

## 🎨 User Interface

### Pages
- **Home** - Landing page
- **Login** - User authentication
- **Register** - New user registration
- **Dashboard** - User dashboard
- **Services** - Service listing
- **Bookings** - Booking management
- **New Booking** - Create new booking
- **Profile** - User profile management
- **Admin Dashboard** - Admin panel (ADMIN only)
- **Barber Dashboard** - Barber panel (BARBER/ADMIN only)

### Design
- Modern and responsive interface
- Tailwind CSS for styling
- Lucide React for icons
- Toast notifications
- Loading states
- Error handling

## 🧪 Testing

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

## 📦 Production Build

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

### Docker (All Services)
```bash
docker-compose build
docker-compose up -d
```

## 🚀 Deployment

### Quick Start with Docker

The easiest way to deploy is using Docker Compose on a VPS (Virtual Private Server).

**Recommended VPS Providers:**
- **Digital Ocean**: $24/month (4GB RAM) - [www.digitalocean.com](https://www.digitalocean.com)
- **Linode**: $24/month (4GB RAM) - [www.linode.com](https://www.linode.com)
- **Vultr**: $24/month (4GB RAM) - [www.vultr.com](https://www.vultr.com)
- **Hostinger VPS**: R$ 79,90/month (4GB RAM) - [www.hostinger.com.br](https://www.hostinger.com.br)
- **KingHost VPS**: R$ 149,90/month (4GB RAM) - [www.kinghost.com.br](https://www.kinghost.com.br)

**Minimum Requirements:**
- 4GB RAM (2GB minimum, but may be slow)
- 2 vCPUs
- 40GB SSD storage
- Ubuntu 22.04 LTS

**Deployment Steps:**

1. **Connect to your VPS via SSH:**
   ```bash
   ssh root@YOUR_SERVER_IP
   ```

2. **Install Docker and Docker Compose:**
   ```bash
   curl -fsSL https://get.docker.com -o get-docker.sh
   sh get-docker.sh
   curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
   chmod +x /usr/local/bin/docker-compose
   ```

3. **Clone or upload the project:**
   ```bash
   mkdir -p /opt/barbershop
   cd /opt/barbershop
   # Upload your project files here
   ```

4. **Configure environment variables:**
   ```bash
   cp env.example .env
   nano .env  # Edit with your settings
   ```

5. **Build and start services:**
   ```bash
   docker-compose build
   docker-compose up -d
   ```

6. **Check status:**
   ```bash
   docker-compose ps
   ```

**For detailed deployment instructions, see:** `GUIA_COMPLETO_HOSPEDAGEM_E_VENDA.txt`

### Other Deployment Options

**Cloud Platforms:**
- AWS ECS/EKS
- Google Cloud Run
- Azure Container Instances

**Frontend Hosting:**
- Vercel
- Netlify
- AWS S3 + CloudFront

**Database:**
- AWS RDS PostgreSQL
- Google Cloud SQL
- Azure Database for PostgreSQL
- Supabase

## 🔧 Troubleshooting

### Check Service Status
```bash
# View all running containers
docker-compose ps

# View logs for all services
docker-compose logs -f

# View logs for specific service
docker logs barbershop-auth
docker logs barbershop-gateway
docker logs barbershop-frontend
```

### Database Connection Error
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# View PostgreSQL logs
docker logs barbershop-postgres

# Restart PostgreSQL
docker-compose restart postgres
```

### JWT Authentication Error
- Verify that `JWT_SECRET` is the same in all services (gateway, auth-service)
- Check `.env` file has correct `JWT_SECRET`
- Confirm the token is being sent in the correct format: `Authorization: Bearer {token}`
- Restart all services: `docker-compose restart`

### Email Not Being Sent
- Check SMTP credentials in `.env` file
- For Gmail:
  1. Enable 2-step verification
  2. Generate App Password at: https://myaccount.google.com/apppasswords
  3. Use App Password (not normal password) in `MAIL_PASSWORD`
- Check auth-service logs: `docker logs barbershop-auth | grep -i mail`
- Test email configuration manually

### Frontend Not Connecting to Backend
- Check if all services are running: `docker-compose ps`
- Verify gateway is accessible: `curl http://localhost:8080/api/services`
- Check CORS configuration in `GatewayConfig.java`
- Verify `FRONTEND_URL` in `.env` matches your frontend URL
- Check browser console for CORS errors

### Services Not Starting
```bash
# Rebuild all services
docker-compose build --no-cache

# Start services
docker-compose up -d

# Check logs
docker-compose logs -f
```

### Out of Memory
- Check memory usage: `docker stats`
- Consider upgrading VPS to 4GB+ RAM
- Restart services: `docker-compose restart`

### Port Already in Use
```bash
# Check what's using the port
sudo lsof -i :8080

# Stop conflicting service or change port in docker-compose.yml
```

## 📝 Future Improvements

- [ ] Push notification system
- [ ] Calendar integration (Google Calendar)
- [ ] Rating and review system
- [ ] Real-time chat
- [ ] Reports and analytics
- [ ] Payment integration (Stripe, PayPal)
- [ ] Mobile app (React Native)
- [ ] Loyalty/points system
- [ ] SMS notifications
- [ ] Multi-language support
- [ ] Advanced scheduling rules
- [ ] Recurring appointments

## 💼 Commercial Use

This system can be sold, installed, and used commercially. See `GUIA_COMPLETO_HOSPEDAGEM_E_VENDA.txt` for detailed information on:
- How to sell the system
- Hosting options and costs
- Step-by-step deployment guide
- Support and maintenance
- FAQ for business use

## 👥 Contributing

1. Fork the project
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👨‍💻 Author

Developed with ❤️ to demonstrate a modern microservices architecture.

## 📞 Support

For questions and support:
- Open an issue on GitHub
- Check `GUIA_COMPLETO_HOSPEDAGEM_E_VENDA.txt` for deployment help
- Review troubleshooting section above

## 📚 Additional Resources

- **Complete Deployment Guide**: See `GUIA_COMPLETO_HOSPEDAGEM_E_VENDA.txt` for detailed instructions on hosting, selling, and maintaining the system
- **Environment Variables**: See `env.example` for all configuration options
- **Database Schema**: See `init-db.sql` for database structure

---

**Made with ☕ and ❤️**

**License**: MIT - Free to use, modify, and sell
