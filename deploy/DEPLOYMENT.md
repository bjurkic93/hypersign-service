# Hypersign Deployment Guide

## Architecture

- **hypersign.hyperbluex.com** - Frontend Angular app
- **hypersign-api.hyperbluex.com** - Backend Spring Boot API

## Prerequisites

### GitHub Secrets

Configure these secrets in your GitHub repository settings:

| Secret | Description |
|--------|-------------|
| `DROPLET_HOST` | Server IP address |
| `DROPLET_SSH_KEY` | SSH private key for server access |
| `CR_PAT` | GitHub Personal Access Token (for container registry) |

### Server Setup

1. **Install Docker & Docker Compose**

```bash
curl -fsSL https://get.docker.com | sh
```

2. **Create application directory**

```bash
mkdir -p /opt/hypersign
cd /opt/hypersign
```

3. **Create environment file**

```bash
cp .env.example .env
nano .env  # Edit with your values
```

4. **Configure DNS**

Point these domains to your server IP:
- `hypersign.hyperbluex.com`
- `hypersign-api.hyperbluex.com`

## Deployment

### Automatic (via GitHub Actions)

Push to `master` branch triggers automatic deployment:

```bash
git push origin master
```

### Manual Deployment

```bash
# On server
cd /opt/hypersign
docker login ghcr.io -u YOUR_GITHUB_USER
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
```

### Manual Trigger

Go to GitHub Actions → "Build and Deploy Hypersign" → "Run workflow"

## Local Development

### Option 1: Database only (recommended)

```bash
docker-compose -f docker-compose.dev.yml up -d

# Then run services locally:
# Backend: ./mvnw spring-boot:run (in hypersign-service)
# Frontend: ng serve (in hypersign-frontend)
```

### Option 2: Full stack

```bash
docker-compose up --build
```

## Monitoring

### View logs

```bash
# All services
docker compose -f docker-compose.prod.yml logs -f

# Specific service
docker compose -f docker-compose.prod.yml logs -f hypersign-service
```

### Health checks

```bash
# API health
curl https://hypersign-api.hyperbluex.com/actuator/health

# Frontend
curl https://hypersign.hyperbluex.com
```

## Rollback

```bash
# Pull specific version
docker pull ghcr.io/bjurkic93/hypersign-service:COMMIT_SHA
docker pull ghcr.io/bjurkic93/hypersign-frontend:COMMIT_SHA

# Update and restart
docker compose -f docker-compose.prod.yml up -d
```

## SSL Certificates

Caddy automatically manages Let's Encrypt certificates. No manual configuration needed.
