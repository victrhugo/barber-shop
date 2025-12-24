-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(50) DEFAULT 'USER',
    email_verified BOOLEAN DEFAULT FALSE,
    verification_token VARCHAR(255),
    verification_token_expiry TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Services table
CREATE TABLE IF NOT EXISTS services (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    duration_minutes INTEGER NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bookings table
CREATE TABLE IF NOT EXISTS bookings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    service_id UUID NOT NULL REFERENCES services(id),
    barber_id UUID REFERENCES users(id),
    booking_date DATE NOT NULL,
    booking_time TIME NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(barber_id, booking_date, booking_time)
);

-- Barbers table
CREATE TABLE IF NOT EXISTS barbers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE REFERENCES users(id),
    specialties TEXT[],
    bio TEXT,
    rating DECIMAL(3, 2) DEFAULT 0.00,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default services (only if they don't exist)
INSERT INTO services (name, description, duration_minutes, price)
SELECT * FROM (VALUES
    ('Corte de Cabelo', 'Corte de cabelo masculino tradicional com técnicas modernas', 30, 35.00),
    ('Barba Completa', 'Fazer a barba completa com navalha e acabamento profissional', 25, 30.00),
    ('Corte de Cabelo com Sobrancelha', 'Corte de cabelo e design de sobrancelha para um visual completo', 40, 45.00),
    ('Cabelo + Barba', 'Corte de cabelo e barba completa', 50, 60.00),
    ('Pacote Completo', 'Cabelo, barba e sobrancelha - o melhor cuidado masculino', 65, 75.00),
    ('Sobrancelha', 'Design de sobrancelha profissional', 15, 15.00),
    ('Corte + Barba + Sobrancelha', 'Serviço completo: corte, barba e sobrancelha', 70, 80.00)
) AS v(name, description, duration_minutes, price)
WHERE NOT EXISTS (SELECT 1 FROM services WHERE services.name = v.name);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_bookings_user_id ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_barber_id ON bookings(barber_id);
CREATE INDEX IF NOT EXISTS idx_bookings_date ON bookings(booking_date);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);

-- Insert default barber user (password: barber123)
INSERT INTO users (id, email, password, full_name, role, email_verified)
SELECT uuid_generate_v4(), 'barbeiro@barbershop.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Barbeiro Padrão', 'BARBER', true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'barbeiro@barbershop.com');

-- Insert default admin user (password: admin123)
INSERT INTO users (id, email, password, full_name, role, email_verified)
SELECT uuid_generate_v4(), 'admin@barbershop.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Administrador', 'ADMIN', true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@barbershop.com');

-- Link barber user to barbers table
INSERT INTO barbers (user_id, active)
SELECT u.id, true
FROM users u
WHERE u.email = 'barbeiro@barbershop.com' AND u.role = 'BARBER'
AND NOT EXISTS (SELECT 1 FROM barbers WHERE user_id = u.id);

