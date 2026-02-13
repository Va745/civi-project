# 🚀 CivicPulse - Quick Start Checklist

Follow these steps in order to get your application running!

---

## ✅ Step 1: Install Prerequisites (if not already installed)

### Check what you have:
```bash
# Check Java
java -version
# Need: Java 11 or higher

# Check Maven
mvn -version
# Need: Maven 3.6+

# Check MySQL
mysql --version
# Need: MySQL 8.0+
```

### Install if missing:
```bash
# Install Java 11
sudo apt update
sudo apt install openjdk-11-jdk -y

# Install Maven
sudo apt install maven -y

# Install MySQL
sudo apt install mysql-server -y

# Install Tomcat (if not installed)
sudo apt install tomcat9 -y
```

---

## ✅ Step 2: Setup MySQL Database

### Start MySQL:
```bash
sudo systemctl start mysql
sudo systemctl enable mysql
```

### Create the database:
```bash
cd /home/ranjith/Desktop/Civic-Project
mysql -u root -p < database/schema.sql
```

**Note:** If you don't have a MySQL root password, you might need to set one first:
```bash
sudo mysql
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'your_password';
FLUSH PRIVILEGES;
EXIT;
```

### Verify database creation:
```bash
mysql -u root -p
```
```sql
USE civicpulse;
SHOW TABLES;
-- You should see: users, departments, issues, citizen_issue_map, issue_timeline
SELECT * FROM departments;
-- You should see 4 departments
EXIT;
```

---

## ✅ Step 3: Configure Application

### Update database password:
```bash
nano src/main/resources/config.properties
```

Change this line to match your MySQL password:
```properties
db.password=your_mysql_password
```

Save and exit (Ctrl+X, then Y, then Enter)

### Create upload directory:
```bash
sudo mkdir -p /tmp/civicpulse/uploads
sudo chmod 777 /tmp/civicpulse/uploads
```

---

## ✅ Step 4: Build the Application

```bash
cd /home/ranjith/Desktop/Civic-Project
mvn clean package
```

**Expected output:**
- Should download dependencies (first time only)
- Should compile all Java files
- Should create `target/civicpulse.war`

**Check if WAR file was created:**
```bash
ls -lh target/civicpulse.war
```

---

## ✅ Step 5: Deploy to Tomcat

### Option A: Using system Tomcat
```bash
# Copy WAR file
sudo cp target/civicpulse.war /var/lib/tomcat9/webapps/

# Start Tomcat
sudo systemctl start tomcat9
sudo systemctl enable tomcat9

# Check status
sudo systemctl status tomcat9
```

### Option B: Using standalone Tomcat (if installed in /opt)
```bash
# Copy WAR file
sudo cp target/civicpulse.war /opt/tomcat/webapps/

# Start Tomcat
sudo /opt/tomcat/bin/startup.sh

# Check logs
tail -f /opt/tomcat/logs/catalina.out
```

### Wait for deployment (30-60 seconds)
The WAR file will automatically extract and deploy.

---

## ✅ Step 6: Access the Application

### Open your browser and go to:
```
http://localhost:8080/civicpulse
```

**You should see the CivicPulse landing page!** 🎉

---

## ✅ Step 7: Test the Application

### 1. Test Public Tracking (No Login Required)
- Click "Track Issue" in the navbar
- Enter any issue ID (you'll create one after login)

### 2. Test Admin Login
- Click "Login"
- Email: `admin@civicpulse.com`
- Password: `admin123`
- You should be redirected to admin dashboard

### 3. Test Citizen Registration
- Click "Register"
- Fill in the form:
  - Name: Your Name
  - Email: your@email.com
  - Phone: 9876543210
  - Password: test123
- Click "Create Account"
- Login with your credentials

### 4. Test Department Login
- Login with:
  - Email: `road@civicpulse.com`
  - Password: `dept123`

---

## 🔧 Troubleshooting

### Problem: Port 8080 already in use
```bash
# Find what's using port 8080
sudo lsof -i :8080

# Kill the process
sudo kill -9 <PID>

# Or change Tomcat port
sudo nano /etc/tomcat9/server.xml
# Change 8080 to 8081
```

### Problem: Database connection error
```bash
# Check MySQL is running
sudo systemctl status mysql

# Restart MySQL
sudo systemctl restart mysql

# Check credentials in config.properties
cat src/main/resources/config.properties
```

### Problem: 404 Not Found
```bash
# Check if WAR deployed
ls -la /var/lib/tomcat9/webapps/civicpulse/

# Check Tomcat logs
sudo tail -f /var/lib/tomcat9/logs/catalina.out

# Restart Tomcat
sudo systemctl restart tomcat9
```

### Problem: Build fails
```bash
# Clean and rebuild
mvn clean
mvn package

# Check Java version
java -version
# Must be 11 or higher
```

---

## 📱 What to Do After It's Running

### 1. **Create Your First Issue**
- Register as a citizen
- Login
- Go to "Report Issue" (you'll need to create this page or use API directly)
- Report a civic issue

### 2. **Test Duplicate Detection**
- Report another issue at the same location
- Same category
- It should merge with the first issue!

### 3. **Test Admin Features**
- Login as admin
- View all issues
- Assign issue to a department

### 4. **Test Department Features**
- Login as department user
- View assigned issues
- Update status to "IN_PROGRESS"
- Add remarks

### 5. **Track the Issue**
- Logout
- Go to "Track Issue"
- Enter the Issue ID
- See the complete timeline!

---

## 🎯 Next Development Steps

The core backend is complete! Here's what you can add next:

### 1. **Complete the Dashboards** (Priority)
Create these pages (templates provided in code):
- `citizen-dashboard.html` - Show citizen's reported issues
- `report-issue.html` - Form with map to report issues
- `admin-dashboard.html` - Admin panel with analytics
- `department-dashboard.html` - Department work panel

### 2. **Add Features**
- Email notifications
- SMS alerts
- Image upload for proof
- Advanced analytics charts
- Export reports to PDF

### 3. **Improve UI**
- Add more animations
- Improve mobile responsiveness
- Add dark mode
- Add loading indicators

### 4. **Deploy to Production**
- Get a domain name
- Setup HTTPS
- Use production database
- Configure email service

---

## 📚 Important Files to Review

1. **BUILD_GUIDE.md** - Detailed deployment instructions
2. **TECHNICAL_SPECS.md** - Complete API documentation
3. **IMPLEMENTATION_SUMMARY.md** - What's been built
4. **database/schema.sql** - Database structure

---

## 🆘 Need Help?

### Check Logs:
```bash
# Tomcat logs
sudo tail -f /var/lib/tomcat9/logs/catalina.out

# MySQL logs
sudo tail -f /var/log/mysql/error.log
```

### Test Database Connection:
```bash
mysql -u root -p civicpulse
```

### Rebuild and Redeploy:
```bash
mvn clean package
sudo rm -rf /var/lib/tomcat9/webapps/civicpulse*
sudo cp target/civicpulse.war /var/lib/tomcat9/webapps/
sudo systemctl restart tomcat9
```

---

## ✨ Success Checklist

- [ ] MySQL database created
- [ ] Application built (WAR file exists)
- [ ] Tomcat running
- [ ] Application deployed
- [ ] Can access http://localhost:8080/civicpulse
- [ ] Can login as admin
- [ ] Can register as citizen
- [ ] Can track issues

---

**🎉 Once all checkboxes are ticked, you're ready to start using CivicPulse!**

**Good luck! 🚀**
