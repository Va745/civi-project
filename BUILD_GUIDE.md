# CivicPulse - Build and Deployment Guide

## Prerequisites

1. **Java Development Kit (JDK) 11 or higher**
   ```bash
   java -version
   ```

2. **Apache Maven 3.6+**
   ```bash
   mvn -version
   ```

3. **MySQL 8.0+**
   ```bash
   mysql --version
   ```

4. **Apache Tomcat 9.0+**
   - Download from: https://tomcat.apache.org/download-90.cgi

---

## Step 1: Database Setup

### 1.1 Start MySQL Server
```bash
sudo systemctl start mysql
# OR
sudo service mysql start
```

### 1.2 Create Database and Tables
```bash
cd /home/ranjith/Desktop/Civic-Project
mysql -u root -p < database/schema.sql
```

This will:
- Create `civicpulse` database
- Create all required tables
- Insert default departments
- Insert default admin and department users

### 1.3 Verify Database
```bash
mysql -u root -p
```
```sql
USE civicpulse;
SHOW TABLES;
SELECT * FROM users;
SELECT * FROM departments;
EXIT;
```

---

## Step 2: Configure Application

### 2.1 Update Database Credentials
Edit `src/main/resources/config.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/civicpulse?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=root
db.password=YOUR_MYSQL_PASSWORD
```

### 2.2 Create Upload Directory
```bash
sudo mkdir -p /tmp/civicpulse/uploads
sudo chmod 777 /tmp/civicpulse/uploads
```

---

## Step 3: Build the Application

### 3.1 Clean and Build
```bash
cd /home/ranjith/Desktop/Civic-Project
mvn clean package
```

This will:
- Compile all Java source files
- Run tests (if any)
- Create `target/civicpulse.war`

### 3.2 Verify Build
```bash
ls -lh target/civicpulse.war
```

---

## Step 4: Deploy to Tomcat

### 4.1 Option A: Manual Deployment
```bash
# Copy WAR file to Tomcat webapps directory
sudo cp target/civicpulse.war /opt/tomcat/webapps/

# OR if Tomcat is in different location
sudo cp target/civicpulse.war $CATALINA_HOME/webapps/
```

### 4.2 Option B: Using Maven Tomcat Plugin
Add to `pom.xml` (already configured):
```bash
mvn tomcat7:deploy
```

### 4.3 Start Tomcat
```bash
# If using systemd
sudo systemctl start tomcat

# OR manual start
cd /opt/tomcat/bin
./startup.sh

# Check Tomcat logs
tail -f /opt/tomcat/logs/catalina.out
```

---

## Step 5: Access the Application

### 5.1 Open Browser
Navigate to: **http://localhost:8080/civicpulse**

### 5.2 Test Login Credentials

**Admin Account:**
- Email: `admin@civicpulse.com`
- Password: `admin123`

**Road Department:**
- Email: `road@civicpulse.com`
- Password: `dept123`

**Water Department:**
- Email: `water@civicpulse.com`
- Password: `dept123`

**Sanitation Department:**
- Email: `sanitation@civicpulse.com`
- Password: `dept123`

**Electricity Department:**
- Email: `electricity@civicpulse.com`
- Password: `dept123`

---

## Step 6: Testing the Application

### 6.1 Register as Citizen
1. Go to http://localhost:8080/civicpulse/register.html
2. Fill in the registration form
3. Login with your credentials

### 6.2 Report an Issue
1. Login as citizen
2. Go to "Report Issue"
3. Select category, location, add description
4. Submit the issue
5. Note the Issue ID

### 6.3 Track Issue (Public)
1. Go to "Track Issue" (no login required)
2. Enter the Issue ID
3. View status and timeline

### 6.4 Admin Dashboard
1. Login as admin
2. View all issues
3. Assign issues to departments
4. View analytics

### 6.5 Department Dashboard
1. Login as department user
2. View assigned issues
3. Update issue status
4. Add remarks and proof images

---

## Troubleshooting

### Issue: Port 8080 already in use
```bash
# Find process using port 8080
sudo lsof -i :8080

# Kill the process
sudo kill -9 <PID>

# OR change Tomcat port in server.xml
```

### Issue: Database connection failed
```bash
# Check MySQL is running
sudo systemctl status mysql

# Verify credentials in config.properties
# Check MySQL user permissions
mysql -u root -p
GRANT ALL PRIVILEGES ON civicpulse.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

### Issue: WAR file not deploying
```bash
# Check Tomcat logs
tail -f /opt/tomcat/logs/catalina.out

# Verify WAR file exists
ls -lh target/civicpulse.war

# Check Tomcat permissions
sudo chown -R tomcat:tomcat /opt/tomcat/webapps/
```

### Issue: 404 Not Found
- Ensure WAR file is deployed: Check `/opt/tomcat/webapps/civicpulse/`
- Wait for deployment (may take 30-60 seconds)
- Check Tomcat logs for errors

### Issue: Upload directory permission denied
```bash
sudo mkdir -p /tmp/civicpulse/uploads
sudo chmod 777 /tmp/civicpulse/uploads
```

---

## Development Mode

### Run with Maven Jetty Plugin (Alternative)
```bash
mvn jetty:run
```
Access at: http://localhost:8080/

### Hot Reload
For development, use IDE with hot reload:
- IntelliJ IDEA: Built-in Tomcat integration
- Eclipse: WTP (Web Tools Platform)

---

## Production Deployment

### 1. Update Configuration
```properties
# Use production database
db.url=jdbc:mysql://production-server:3306/civicpulse
db.username=civicpulse_prod
db.password=STRONG_PASSWORD

# Use secure JWT secret
jwt.secret=GENERATE_RANDOM_256_BIT_SECRET

# Configure proper upload directory
upload.directory=/var/www/civicpulse/uploads
```

### 2. Enable HTTPS
Configure SSL in Tomcat `server.xml`

### 3. Set Environment Variables
```bash
export CATALINA_OPTS="-Xms512m -Xmx2048m"
```

### 4. Enable Logging
Configure log4j or SLF4J for production logging

---

## API Endpoints Reference

### Authentication
- `POST /api/auth/register` - Register new citizen
- `POST /api/auth/login` - Login user

### Issues (Citizen)
- `POST /api/issues/report` - Report new issue
- `GET /api/issues/my-issues` - Get citizen's issues
- `GET /api/issues/track/{issueId}` - Track issue (public)

### Admin
- `GET /api/admin/issues` - Get all issues
- `PUT /api/admin/issues/{issueId}/assign` - Assign to department
- `GET /api/admin/analytics` - Get analytics

### Department
- `GET /api/department/issues` - Get department issues
- `PUT /api/department/issues/{issueId}/update` - Update issue status

---

## Project Structure

```
Civic-Project/
├── database/
│   └── schema.sql                 # Database schema
├── src/
│   ├── main/
│   │   ├── java/com/civicpulse/
│   │   │   ├── model/             # Data models
│   │   │   ├── dao/               # Database access
│   │   │   ├── service/           # Business logic
│   │   │   ├── servlet/           # API endpoints
│   │   │   └── util/              # Utilities
│   │   ├── resources/
│   │   │   └── config.properties  # Configuration
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml        # Servlet config
│   │       ├── css/
│   │       │   └── styles.css     # Styles
│   │       ├── js/
│   │       │   └── api.js         # Frontend API
│   │       └── *.html             # HTML pages
├── pom.xml                        # Maven config
├── README.md                      # Project overview
└── TECHNICAL_SPECS.md             # Technical specs
```

---

## Next Steps

1. ✅ Database setup complete
2. ✅ Application built
3. ✅ Deployed to Tomcat
4. ✅ Test all features
5. 📝 Customize for your city
6. 🚀 Deploy to production

---

## Support

For issues or questions:
1. Check Tomcat logs: `/opt/tomcat/logs/catalina.out`
2. Check MySQL logs: `/var/log/mysql/error.log`
3. Review TECHNICAL_SPECS.md for API details

---

**Built with ❤️ for better civic governance**
