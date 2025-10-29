#!/bin/bash
# This script runs the container on the Oracle Cloud Compute Instance

set -e

# Configuration
OCIR_IMAGE="us-ashburn-1.ocir.io/idxoh4gynbfg/country-currency-api:latest"

echo "Logging in to OCIR..."
docker login us-ashburn-1.ocir.io

echo "Pulling latest image..."
docker pull ${OCIR_IMAGE}

echo "Stopping existing container (if any)..."
docker stop country-api 2>/dev/null || true
docker rm country-api 2>/dev/null || true

echo "Starting container..."
docker run -d \
  --name country-api \
  --restart unless-stopped \
  -p 8080:8080 \
  -v ~/data:/app/data \
  -v ~/cache:/app/cache \
  -v ~/logs:/app/logs \
  -e SPRING_DATASOURCE_URL=jdbc:h2:file:/app/data/countrydb \
  -e SPRING_DATASOURCE_USERNAME=sa \
  -e SPRING_DATASOURCE_PASSWORD=password \
  -e SERVER_PORT=8080 \
  -e LOGGING_LEVEL_COM_HAIDARA=INFO \
  ${OCIR_IMAGE}

echo "Waiting for container to start..."
sleep 10

echo "Checking container status..."
docker ps | grep country-api

echo "Testing API..."
curl -f http://localhost:8080/api/status || echo "API not ready yet, give it a moment..."

echo ""
echo "Container started successfully!"
echo "View logs: docker logs -f country-api"
echo "API Status: curl http://localhost:8080/api/status"
echo "Refresh Data: curl -X POST http://localhost:8080/api/status/refresh"
