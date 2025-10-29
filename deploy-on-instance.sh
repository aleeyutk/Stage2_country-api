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
