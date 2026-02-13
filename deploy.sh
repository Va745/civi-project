#!/bin/bash

# CivicPulse Deployment Script
# Run this script to deploy CivicPulse

echo "=========================================="
echo "🏙️  CivicPulse Deployment Script"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Step 1: Check if Maven is installed
echo -e "${YELLOW}Step 1: Checking Maven...${NC}"
if ! command -v mvn &> /dev/null
then
    echo -e "${RED}Maven is not installed. Installing...${NC}"
    sudo apt update
    sudo apt install maven -y
    echo -e "${GREEN}✓ Maven installed${NC}"
else
    echo -e "${GREEN}✓ Maven is already installed${NC}"
    mvn -version
fi
echo ""

# Step 2: Check MySQL
echo -e "${YELLOW}Step 2: Checking MySQL...${NC}"
if ! command -v mysql &> /dev/null
then
    echo -e "${RED}MySQL is not installed. Please install MySQL first.${NC}"
    exit 1
else
    echo -e "${GREEN}✓ MySQL is installed${NC}"
    mysql --version
fi
echo ""

# Step 3: Setup Database
echo -e "${YELLOW}Step 3: Setting up database...${NC}"
echo "Please enter your MySQL root password when prompted:"
mysql -u root -p < database/schema.sql
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Database created successfully${NC}"
else
    echo -e "${RED}✗ Database creation failed. Please check your MySQL password.${NC}"
    exit 1
fi
echo ""

# Step 4: Create upload directory
echo -e "${YELLOW}Step 4: Creating upload directory...${NC}"
sudo mkdir -p /tmp/civicpulse/uploads
sudo chmod 777 /tmp/civicpulse/uploads
echo -e "${GREEN}✓ Upload directory created${NC}"
echo ""

# Step 5: Build the project
echo -e "${YELLOW}Step 5: Building the project...${NC}"
echo "This may take a few minutes on first run..."
mvn clean package
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Build successful!${NC}"
    echo -e "${GREEN}✓ WAR file created: target/civicpulse.war${NC}"
else
    echo -e "${RED}✗ Build failed. Please check the errors above.${NC}"
    exit 1
fi
echo ""

# Step 6: Check if Tomcat is installed
echo -e "${YELLOW}Step 6: Checking Tomcat...${NC}"
if [ -d "/var/lib/tomcat9" ]; then
    TOMCAT_DIR="/var/lib/tomcat9"
    echo -e "${GREEN}✓ Tomcat9 found${NC}"
elif [ -d "/opt/tomcat" ]; then
    TOMCAT_DIR="/opt/tomcat"
    echo -e "${GREEN}✓ Tomcat found in /opt/tomcat${NC}"
else
    echo -e "${YELLOW}Tomcat not found. Installing Tomcat9...${NC}"
    sudo apt install tomcat9 -y
    TOMCAT_DIR="/var/lib/tomcat9"
    echo -e "${GREEN}✓ Tomcat9 installed${NC}"
fi
echo ""

# Step 7: Deploy to Tomcat
echo -e "${YELLOW}Step 7: Deploying to Tomcat...${NC}"
sudo cp target/civicpulse.war ${TOMCAT_DIR}/webapps/
echo -e "${GREEN}✓ WAR file copied to Tomcat${NC}"
echo ""

# Step 8: Start Tomcat
echo -e "${YELLOW}Step 8: Starting Tomcat...${NC}"
if [ -d "/var/lib/tomcat9" ]; then
    sudo systemctl start tomcat9
    sudo systemctl enable tomcat9
    echo -e "${GREEN}✓ Tomcat9 started${NC}"
else
    sudo ${TOMCAT_DIR}/bin/startup.sh
    echo -e "${GREEN}✓ Tomcat started${NC}"
fi
echo ""

# Wait for deployment
echo -e "${YELLOW}Waiting for application to deploy (30 seconds)...${NC}"
sleep 30

# Final message
echo ""
echo "=========================================="
echo -e "${GREEN}🎉 Deployment Complete!${NC}"
echo "=========================================="
echo ""
echo "📱 Access your application at:"
echo -e "${GREEN}http://localhost:8080/civicpulse${NC}"
echo ""
echo "🔐 Demo Login Credentials:"
echo ""
echo "Admin:"
echo "  Email: admin@civicpulse.com"
echo "  Password: admin123"
echo ""
echo "Road Department:"
echo "  Email: road@civicpulse.com"
echo "  Password: dept123"
echo ""
echo "📚 Documentation:"
echo "  - QUICK_START.md"
echo "  - BUILD_GUIDE.md"
echo "  - TECHNICAL_SPECS.md"
echo ""
echo "🔍 Check logs if needed:"
if [ -d "/var/lib/tomcat9" ]; then
    echo "  sudo tail -f /var/lib/tomcat9/logs/catalina.out"
else
    echo "  tail -f ${TOMCAT_DIR}/logs/catalina.out"
fi
echo ""
echo "=========================================="
