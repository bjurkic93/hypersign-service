-- Hypersign Database Initialization Script
-- This script runs on first container startup

-- Create hypersign database (if not exists via POSTGRES_DB)
SELECT 'CREATE DATABASE hypersign_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hypersign_db')\gexec

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE hypersign_db TO hypersign;

-- Connect to hypersign_db and set up extensions
\c hypersign_db

-- Enable useful extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
