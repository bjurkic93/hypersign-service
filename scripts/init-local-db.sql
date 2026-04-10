-- Local Postgres: create DB + app user (defaults match application.yml).
-- Run as superuser, e.g. native install:
--   $env:PGPASSWORD='...'; & "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -h localhost -d postgres -f scripts/init-local-db.sql
-- Docker (example):
--   Get-Content scripts/init-local-db.sql | docker exec -i hypeproductionx-postgres-1 psql -U hyperai -d postgres

CREATE ROLE hypersign_user WITH LOGIN PASSWORD 'Passw0rd';
CREATE DATABASE hypersign OWNER hypersign_user;
GRANT ALL PRIVILEGES ON DATABASE hypersign TO hypersign_user;
