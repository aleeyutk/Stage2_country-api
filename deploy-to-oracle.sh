#!/bin/bash
# deploy-to-oracle.sh
# Complete automation script for deploying to Oracle Cloud

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Oracle Cloud Deployment Script${NC}"
echo -e "${GREEN}========================================${NC}"

# Configuration
echo -e "\n${YELLOW}Step 1: Configuration${NC}"
read -p "Enter your OCI Region (e.g., us-ashburn-1): " REGION
read -p "Enter your Tenancy Namespace: " TENANCY_NAMESPACE
read -p "Enter your Compute Instance Public IP (if exists, or press Enter): " INSTANCE_IP
REPO_NAME="country-currency-api"
IMAGE_TAG="latest"

# Build Docker Image
echo -e "\n${YELLOW}Step 2: Building Docker Image${NC}"
echo "Building image: ${REPO_NAME}:${IMAGE_TAG}"
docker build -t ${REPO_NAME}:${IMAGE_TAG} .

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Docker image built successfully${NC}"
else
    echo -e "${RED}✗ Failed to build Docker image${NC}"
    exit 1
fi

# Tag for Oracle Container Registry
echo -e "\n${YELLOW}Step 3: Tagging Image for OCIR${NC}"
OCIR_IMAGE="${REGION}.ocir.io/${TENANCY_NAMESPACE}/${REPO_NAME}:${IMAGE_TAG}"
docker tag ${REPO_NAME}:${IMAGE_TAG} ${OCIR_IMAGE}
echo "Tagged as: ${OCIR_IMAGE}"

# Login to OCIR
echo -e "\n${YELLOW}Step 4: Login to Oracle Container Registry${NC}"
echo "Please enter your OCIR credentials:"
echo "Username format: ${TENANCY_NAMESPACE}/your-username"
read -p "OCIR Username: " OCIR_USERNAME
read -sp "OCIR Auth Token: " OCIR_PASSWORD
echo

docker login ${REGION}.ocir.io -u "${OCIR_USERNAME}" -p "${OCIR_PASSWORD}"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Successfully logged in to OCIR${NC}"
else
    echo -e "${RED}✗ Failed to login to OCIR${NC}"
    exit 1
fi

# Push to OCIR
echo -e "\n${YELLOW}Step 5: Pushing Image to OCIR${NC}"
docker push ${OCIR_IMAGE}

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Image pushed successfully${NC}"
else
    echo -e "${RED}✗ Failed to push image${NC}"
    exit 1
fi

# Generate deployment script for compute instance
echo -e "\n${YELLOW}Step 6: Generating Deployment Scripts${NC}"

cat > deploy-on-instance.sh << 'EOF'
#!/bin/bash
# This script runs on the Oracle Cloud Compute Instance

set -e

echo "Installing Docker..."
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker opc

echo "Configuring firewall..."
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload

echo "Creating application directories..."
mkdir -p ~/data ~/cache ~/logs

echo "Docker installed successfully!"
echo "Please logout and login again, then run: ./run-container.sh"
EOF

cat > run-container.sh << EOF
#!/bin/bash
# This script runs the container on the Oracle Cloud Compute Instance

set -e

# Configuration
OCIR_IMAGE="${OCIR_IMAGE}"

echo "Logging in to OCIR..."
docker login ${REGION}.ocir.io

echo "Pulling latest image..."
docker pull \${OCIR_IMAGE}

echo "Stopping existing container (if any)..."
docker stop country-api 2>/dev/null || true
docker rm country-api 2>/dev/null || true

echo "Starting container..."
docker run -d \\
  --name country-api \\
  --restart unless-stopped \\
  -p 8080:8080 \\
  -v ~/data:/app/data \\
  -v ~/cache:/app/cache \\
  -v ~/logs:/app/logs \\
  -e SPRING_DATASOURCE_URL=jdbc:h2:file:/app/data/countrydb \\
  -e SPRING_DATASOURCE_USERNAME=sa \\
  -e SPRING_DATASOURCE_PASSWORD=password \\
  -e SERVER_PORT=8080 \\
  -e LOGGING_LEVEL_COM_HAIDARA=INFO \\
  \${OCIR_IMAGE}

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
EOF

chmod +x deploy-on-instance.sh
chmod +x run-container.sh

echo -e "${GREEN}✓ Deployment scripts generated${NC}"
echo "  - deploy-on-instance.sh (initial setup)"
echo "  - run-container.sh (run container)"

# If instance IP is provided, offer to deploy
if [ ! -z "$INSTANCE_IP" ]; then
    echo -e "\n${YELLOW}Step 7: Deploy to Instance${NC}"
    read -p "Do you want to deploy to ${INSTANCE_IP} now? (y/n): " DEPLOY_NOW
    
    if [ "$DEPLOY_NOW" = "y" ] || [ "$DEPLOY_NOW" = "Y" ]; then
        echo "Copying scripts to instance..."
        scp deploy-on-instance.sh run-container.sh opc@${INSTANCE_IP}:~/
        
        echo "Connecting to instance..."
        echo "Run these commands on the instance:"
        echo "  1. chmod +x deploy-on-instance.sh run-container.sh"
        echo "  2. ./deploy-on-instance.sh"
        echo "  3. logout and login"
        echo "  4. ./run-container.sh"
        
        read -p "Press Enter to SSH into the instance..."
        ssh opc@${INSTANCE_IP}
    fi
else
    echo -e "\n${YELLOW}Next Steps:${NC}"
    echo "1. Create a Compute Instance in Oracle Cloud Console"
    echo "2. Copy deploy-on-instance.sh and run-container.sh to the instance"
    echo "3. SSH into the instance and run: ./deploy-on-instance.sh"
    echo "4. Logout and login again"
    echo "5. Run: ./run-container.sh"
fi

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}Deployment Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "\nImage pushed to: ${OCIR_IMAGE}"
echo -e "Deployment scripts created in current directory"
