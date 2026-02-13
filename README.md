# CivicPulse - Civic Issue Reporting Platform

## Project Structure

```
Civic-Project/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── civicpulse/
│   │   │           ├── model/          # Data models
│   │   │           ├── dao/            # Database access
│   │   │           ├── service/        # Business logic
│   │   │           ├── servlet/        # API endpoints
│   │   │           ├── filter/         # Security filters
│   │   │           └── util/           # Utilities
│   │   ├── webapp/
│   │   │   ├── WEB-INF/
│   │   │   │   └── web.xml
│   │   │   ├── css/
│   │   │   ├── js/
│   │   │   ├── images/
│   │   │   ├── index.html
│   │   │   ├── login.html
│   │   │   ├── register.html
│   │   │   ├── citizen-dashboard.html
│   │   │   ├── report-issue.html
│   │   │   ├── track-issue.html
│   │   │   ├── admin-dashboard.html
│   │   │   └── department-dashboard.html
│   │   └── resources/
│   │       └── config.properties
│   └── test/
├── database/
│   └── schema.sql
├── pom.xml
└── README.md
```

## Setup Instructions

### 1. Database Setup
```bash
mysql -u root -p < database/schema.sql
```

### 2. Configure Database Connection
Edit `src/main/resources/config.properties`:
```
db.url=jdbc:mysql://localhost:3306/civicpulse
db.username=civicpulse_user
db.password=your_password
```

### 3. Build and Deploy
```bash
mvn clean package
cp target/civicpulse.war /opt/tomcat/webapps/
```

### 4. Access Application
- URL: http://localhost:8080/civicpulse
- Admin Login: admin@civicpulse.com / admin123
- Department Login: road@civicpulse.com / dept123

## Features

✅ Citizen Registration & Login
✅ Issue Reporting with GPS Location
✅ Intelligent Duplicate Detection & Merging
✅ Public Issue Tracking (No Login Required)
✅ Department Management Dashboard
✅ Admin Analytics & Assignment
✅ Real-time Status Updates
✅ Image Upload Support
✅ Interactive Map Integration

## Technology Stack

- **Backend**: Java, JSP, Servlets
- **Database**: MySQL 8.0+
- **Frontend**: HTML5, CSS3, JavaScript
- **Maps**: OpenStreetMap/Leaflet.js
- **Security**: JWT, BCrypt
- **Server**: Apache Tomcat 9.0+

## API Documentation

See `TECHNICAL_SPECS.md` for complete API documentation.

## License

MIT License
