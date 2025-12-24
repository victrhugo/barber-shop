# Script PowerShell para inserir serviços manualmente no banco de dados

Write-Host "Inserindo serviços no banco de dados..." -ForegroundColor Green

$sql = @"
-- Verificar se já existem serviços
SELECT COUNT(*) as total FROM services;

-- Inserir serviços se não existirem
INSERT INTO services (name, description, duration_minutes, price)
SELECT * FROM (VALUES
    ('Corte de Cabelo', 'Corte de cabelo masculino tradicional', 30, 35.00),
    ('Barba', 'Fazer a barba completa com navalha', 20, 25.00),
    ('Cabelo + Barba', 'Corte de cabelo e barba', 45, 55.00),
    ('Cabelo + Sobrancelha', 'Corte de cabelo e design de sobrancelha', 40, 45.00),
    ('Pacote Completo', 'Cabelo, barba e sobrancelha', 60, 70.00),
    ('Sobrancelha', 'Design de sobrancelha', 15, 15.00)
) AS v(name, description, duration_minutes, price)
WHERE NOT EXISTS (SELECT 1 FROM services WHERE services.name = v.name);

-- Verificar serviços inseridos
SELECT id, name, price FROM services ORDER BY name;
"@

docker exec -i barbershop-postgres psql -U barbershop -d barbershop -c $sql

Write-Host "Serviços inseridos com sucesso!" -ForegroundColor Green



