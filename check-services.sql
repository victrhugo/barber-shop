-- Script para verificar se os serviços foram criados corretamente
-- Execute este script no PostgreSQL para verificar

SELECT COUNT(*) as total_services FROM services;

SELECT id, name, description, duration_minutes, price, active 
FROM services 
ORDER BY name;

-- Se não houver serviços, execute manualmente:
-- INSERT INTO services (name, description, duration_minutes, price) VALUES
--     ('Corte de Cabelo', 'Corte de cabelo masculino tradicional', 30, 35.00),
--     ('Barba', 'Fazer a barba completa com navalha', 20, 25.00),
--     ('Cabelo + Barba', 'Corte de cabelo e barba', 45, 55.00),
--     ('Cabelo + Sobrancelha', 'Corte de cabelo e design de sobrancelha', 40, 45.00),
--     ('Pacote Completo', 'Cabelo, barba e sobrancelha', 60, 70.00),
--     ('Sobrancelha', 'Design de sobrancelha', 15, 15.00);



