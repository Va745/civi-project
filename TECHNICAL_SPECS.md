# CivicPulse - Technical Specifications

## 1. System Architecture

### 1.1 Architecture Pattern
- **Type**: Three-tier Client-Server Architecture
- **Communication**: RESTful APIs
- **Security**: JWT-based authentication + RBAC

### 1.2 Technology Stack

```
Frontend:  HTML5, CSS3, JavaScript (ES6+)
Backend:   Java (JSP, Servlets)
Database:  MySQL 8.0+
Maps:      OpenStreetMap API / Mapbox
Server:    Apache Tomcat 9.0+
```

---

## 2. Database Schema

### 2.1 Users Table
```sql
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(15) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('CITIZEN', 'ADMIN', 'DEPARTMENT') DEFAULT 'CITIZEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    INDEX idx_email (email),
    INDEX idx_phone (phone)
);
```

### 2.2 Departments Table
```sql
CREATE TABLE departments (
    dept_id INT PRIMARY KEY AUTO_INCREMENT,
    dept_name VARCHAR(50) NOT NULL,
    dept_type ENUM('ROAD', 'WATER', 'SANITATION', 'ELECTRICITY') NOT NULL,
    contact_email VARCHAR(100),
    contact_phone VARCHAR(15),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_dept_type (dept_type)
);
```

### 2.3 Issues Table
```sql
CREATE TABLE issues (
    issue_id VARCHAR(20) PRIMARY KEY,
    category ENUM('ROAD', 'WATER', 'SANITATION', 'ELECTRICITY') NOT NULL,
    location_lat DECIMAL(10, 8) NOT NULL,
    location_lng DECIMAL(11, 8) NOT NULL,
    address TEXT NOT NULL,
    description TEXT NOT NULL,
    status ENUM('REPORTED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED') DEFAULT 'REPORTED',
    report_count INT DEFAULT 1,
    dept_id INT,
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    FOREIGN KEY (dept_id) REFERENCES departments(dept_id),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_location (location_lat, location_lng),
    INDEX idx_created_at (created_at)
);
```

### 2.4 Citizen_Issue_Map Table
```sql
CREATE TABLE citizen_issue_map (
    map_id INT PRIMARY KEY AUTO_INCREMENT,
    citizen_id INT NOT NULL,
    issue_id VARCHAR(20) NOT NULL,
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (citizen_id) REFERENCES users(user_id),
    FOREIGN KEY (issue_id) REFERENCES issues(issue_id),
    UNIQUE KEY unique_citizen_issue (citizen_id, issue_id),
    INDEX idx_citizen (citizen_id),
    INDEX idx_issue (issue_id)
);
```

### 2.5 Issue_Timeline Table
```sql
CREATE TABLE issue_timeline (
    timeline_id INT PRIMARY KEY AUTO_INCREMENT,
    issue_id VARCHAR(20) NOT NULL,
    status ENUM('REPORTED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED') NOT NULL,
    updated_by INT NOT NULL,
    remarks TEXT,
    proof_image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (issue_id) REFERENCES issues(issue_id),
    FOREIGN KEY (updated_by) REFERENCES users(user_id),
    INDEX idx_issue (issue_id),
    INDEX idx_created_at (created_at)
);
```

### 2.6 Issue ID Generation
```
Format: CIVIC-{CATEGORY_CODE}-{YYYYMMDD}-{SEQUENCE}
Example: CIVIC-RD-20260210-0001
```

---

## 3. API Specifications

### 3.1 Authentication APIs

#### POST /api/auth/register
**Request:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "9876543210",
  "password": "securePassword123"
}
```
**Response (201):**
```json
{
  "success": true,
  "message": "Registration successful",
  "userId": 123
}
```

#### POST /api/auth/login
**Request:**
```json
{
  "identifier": "john@example.com",
  "password": "securePassword123"
}
```
**Response (200):**
```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "userId": 123,
    "name": "John Doe",
    "role": "CITIZEN"
  }
}
```

### 3.2 Issue Reporting APIs

#### POST /api/issues/report
**Headers:** `Authorization: Bearer {token}`
**Request:**
```json
{
  "category": "ROAD",
  "latitude": 12.9716,
  "longitude": 77.5946,
  "address": "MG Road, Bangalore",
  "description": "Large pothole causing traffic issues",
  "image": "base64_encoded_image_data"
}
```
**Response (201):**
```json
{
  "success": true,
  "merged": false,
  "issueId": "CIVIC-RD-20260210-0001",
  "message": "Issue reported successfully"
}
```

**Response (200) - Merged:**
```json
{
  "success": true,
  "merged": true,
  "issueId": "CIVIC-RD-20260210-0001",
  "reportCount": 5,
  "message": "Your issue has been merged with an existing report"
}
```

#### GET /api/issues/track/{issueId}
**Public Access (No Auth Required)**
**Response (200):**
```json
{
  "success": true,
  "issue": {
    "issueId": "CIVIC-RD-20260210-0001",
    "category": "ROAD",
    "location": {
      "latitude": 12.9716,
      "longitude": 77.5946,
      "address": "MG Road, Bangalore"
    },
    "description": "Large pothole causing traffic issues",
    "status": "IN_PROGRESS",
    "reportCount": 5,
    "department": "Road Department",
    "createdAt": "2026-02-10T10:30:00Z",
    "updatedAt": "2026-02-10T16:00:00Z",
    "timeline": [
      {
        "status": "REPORTED",
        "updatedBy": "Citizen",
        "timestamp": "2026-02-10T10:30:00Z"
      },
      {
        "status": "ASSIGNED",
        "updatedBy": "Admin",
        "remarks": "Assigned to Road Department",
        "timestamp": "2026-02-10T12:00:00Z"
      },
      {
        "status": "IN_PROGRESS",
        "updatedBy": "Road Department",
        "remarks": "Work started, crew deployed",
        "timestamp": "2026-02-10T16:00:00Z"
      }
    ]
  }
}
```

#### GET /api/issues/my-issues
**Headers:** `Authorization: Bearer {token}`
**Response (200):**
```json
{
  "success": true,
  "issues": [
    {
      "issueId": "CIVIC-RD-20260210-0001",
      "category": "ROAD",
      "status": "IN_PROGRESS",
      "reportCount": 5,
      "reportedAt": "2026-02-10T10:30:00Z"
    }
  ]
}
```

### 3.3 Admin APIs

#### GET /api/admin/issues
**Headers:** `Authorization: Bearer {token}` (Admin only)
**Query Params:** `?status=REPORTED&category=ROAD&page=1&limit=20`
**Response (200):**
```json
{
  "success": true,
  "total": 150,
  "page": 1,
  "issues": [...]
}
```

#### PUT /api/admin/issues/{issueId}/assign
**Headers:** `Authorization: Bearer {token}` (Admin only)
**Request:**
```json
{
  "deptId": 1
}
```
**Response (200):**
```json
{
  "success": true,
  "message": "Issue assigned to Road Department"
}
```

#### GET /api/admin/analytics
**Headers:** `Authorization: Bearer {token}` (Admin only)
**Response (200):**
```json
{
  "success": true,
  "analytics": {
    "totalIssues": 1250,
    "pending": 45,
    "inProgress": 120,
    "resolved": 1085,
    "avgResolutionTime": "48 hours",
    "byCategory": {
      "ROAD": 450,
      "WATER": 320,
      "SANITATION": 280,
      "ELECTRICITY": 200
    },
    "hotspots": [
      {
        "location": "MG Road",
        "issueCount": 25
      }
    ]
  }
}
```

### 3.4 Department APIs

#### GET /api/department/issues
**Headers:** `Authorization: Bearer {token}` (Department only)
**Response (200):**
```json
{
  "success": true,
  "issues": [...]
}
```

#### PUT /api/department/issues/{issueId}/update
**Headers:** `Authorization: Bearer {token}` (Department only)
**Request:**
```json
{
  "status": "IN_PROGRESS",
  "remarks": "Work crew deployed to location",
  "proofImage": "base64_encoded_image_data"
}
```
**Response (200):**
```json
{
  "success": true,
  "message": "Issue updated successfully"
}
```

---

## 4. Duplicate Detection Algorithm

### 4.1 Matching Logic
```java
public boolean isDuplicate(Issue newIssue, List<Issue> existingIssues) {
    for (Issue existing : existingIssues) {
        // Check category match
        if (!existing.getCategory().equals(newIssue.getCategory())) {
            continue;
        }
        
        // Check if not resolved
        if (existing.getStatus().equals("RESOLVED")) {
            continue;
        }
        
        // Check geo-proximity (within 100 meters)
        double distance = calculateDistance(
            existing.getLatitude(), existing.getLongitude(),
            newIssue.getLatitude(), newIssue.getLongitude()
        );
        
        if (distance <= 0.1) { // 100 meters in km
            return true;
        }
    }
    return false;
}

// Haversine formula for distance calculation
private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    final int R = 6371; // Earth radius in km
    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);
    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}
```

### 4.2 Merging Process
```java
public String reportIssue(Issue newIssue, int citizenId) {
    // Find potential duplicates
    List<Issue> candidates = issueDAO.findByCategoryAndStatus(
        newIssue.getCategory(), 
        Arrays.asList("REPORTED", "ASSIGNED", "IN_PROGRESS")
    );
    
    Issue duplicate = findDuplicate(newIssue, candidates);
    
    if (duplicate != null) {
        // Merge with existing issue
        duplicate.incrementReportCount();
        issueDAO.update(duplicate);
        
        // Map citizen to existing issue
        citizenIssueMapDAO.create(citizenId, duplicate.getIssueId());
        
        return duplicate.getIssueId();
    } else {
        // Create new issue
        String issueId = generateIssueId(newIssue.getCategory());
        newIssue.setIssueId(issueId);
        issueDAO.create(newIssue);
        
        // Map citizen to new issue
        citizenIssueMapDAO.create(citizenId, issueId);
        
        // Create timeline entry
        timelineDAO.create(issueId, "REPORTED", citizenId, null);
        
        return issueId;
    }
}
```

---

## 5. Security Implementation

### 5.1 Password Hashing
```java
import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }
    
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
```

### 5.2 JWT Token Generation
```java
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JWTUtil {
    private static final String SECRET_KEY = "your-secret-key-here";
    private static final long EXPIRATION_TIME = 86400000; // 24 hours
    
    public static String generateToken(User user) {
        return Jwts.builder()
            .setSubject(String.valueOf(user.getUserId()))
            .claim("role", user.getRole())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
            .compact();
    }
    
    public static Claims validateToken(String token) {
        return Jwts.parser()
            .setSigningKey(SECRET_KEY)
            .parseClaimsJws(token)
            .getBody();
    }
}
```

### 5.3 RBAC Filter
```java
@WebFilter("/*")
public class AuthorizationFilter implements Filter {
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getRequestURI();
        
        // Public endpoints
        if (path.startsWith("/api/auth/") || path.startsWith("/api/issues/track/")) {
            chain.doFilter(request, response);
            return;
        }
        
        // Protected endpoints
        String token = req.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            Claims claims = JWTUtil.validateToken(token);
            String role = claims.get("role", String.class);
            
            // Check role-based access
            if (path.startsWith("/api/admin/") && !role.equals("ADMIN")) {
                ((HttpServletResponse) response).sendError(403, "Forbidden");
                return;
            }
            
            if (path.startsWith("/api/department/") && !role.equals("DEPARTMENT")) {
                ((HttpServletResponse) response).sendError(403, "Forbidden");
                return;
            }
            
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).sendError(401, "Unauthorized");
        }
    }
}
```

---

## 6. File Upload Handling

### 6.1 Image Upload Configuration
```java
@WebServlet("/api/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class FileUploadServlet extends HttpServlet {
    private static final String UPLOAD_DIR = "/var/www/civicpulse/uploads";
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        Part filePart = request.getPart("file");
        String fileName = System.currentTimeMillis() + "_" + getFileName(filePart);
        String filePath = UPLOAD_DIR + File.separator + fileName;
        
        // Save file
        filePart.write(filePath);
        
        // Return URL
        String fileUrl = "/uploads/" + fileName;
        response.getWriter().write("{\"url\": \"" + fileUrl + "\"}");
    }
}
```

---

## 7. Frontend Structure

### 7.1 Page Structure
```
/public
  /index.html           - Landing page
  /login.html           - Login page
  /register.html        - Registration page
  /citizen-dashboard.html
  /report-issue.html
  /track-issue.html
  /admin-dashboard.html
  /department-dashboard.html
  
/css
  /styles.css           - Global styles
  /dashboard.css
  
/js
  /auth.js              - Authentication logic
  /api.js               - API calls
  /map.js               - Map integration
  /dashboard.js
```

### 7.2 Map Integration (OpenStreetMap)
```javascript
// Initialize map
const map = L.map('map').setView([12.9716, 77.5946], 13);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors'
}).addTo(map);

// Get current location
map.locate({setView: true, maxZoom: 16});

map.on('locationfound', function(e) {
    const marker = L.marker(e.latlng).addTo(map);
    document.getElementById('latitude').value = e.latlng.lat;
    document.getElementById('longitude').value = e.latlng.lng;
});

// Click to select location
map.on('click', function(e) {
    document.getElementById('latitude').value = e.latlng.lat;
    document.getElementById('longitude').value = e.latlng.lng;
});
```

---

## 8. Deployment Architecture

### 8.1 Server Configuration
```
Apache Tomcat 9.0+
- Port: 8080
- Max Threads: 200
- Connection Timeout: 20000ms
```

### 8.2 MySQL Configuration
```sql
-- Create database
CREATE DATABASE civicpulse CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER 'civicpulse_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON civicpulse.* TO 'civicpulse_user'@'localhost';
FLUSH PRIVILEGES;
```

### 8.3 Directory Structure
```
/var/www/civicpulse/
  /uploads/           - User uploaded images
  /logs/              - Application logs
  
/opt/tomcat/webapps/
  /civicpulse.war     - Deployed application
```

---

## 9. Performance Optimization

### 9.1 Database Indexing
- Composite index on (category, status, location)
- Index on issue_id for fast lookups
- Index on citizen_id for dashboard queries

### 9.2 Caching Strategy
- Cache department list (rarely changes)
- Cache resolved issues (immutable)
- Session caching for authenticated users

### 9.3 Query Optimization
```sql
-- Optimized duplicate search query
SELECT issue_id, location_lat, location_lng, report_count
FROM issues
WHERE category = ?
  AND status IN ('REPORTED', 'ASSIGNED', 'IN_PROGRESS')
  AND location_lat BETWEEN ? AND ?
  AND location_lng BETWEEN ? AND ?
LIMIT 50;
```

---

## 10. Testing Strategy

### 10.1 Unit Tests
- Authentication logic
- Duplicate detection algorithm
- Distance calculation accuracy

### 10.2 Integration Tests
- API endpoint testing
- Database transactions
- File upload functionality

### 10.3 Load Testing
- Concurrent user simulation (1000+ users)
- Database query performance
- API response times

---

## 11. Monitoring & Logging

### 11.1 Application Logs
```java
import org.apache.log4j.Logger;

public class IssueService {
    private static final Logger logger = Logger.getLogger(IssueService.class);
    
    public String reportIssue(Issue issue) {
        logger.info("New issue reported: " + issue.getCategory());
        // ... logic
        logger.info("Issue created with ID: " + issueId);
    }
}
```

### 11.2 Metrics to Track
- Total issues reported (daily/weekly/monthly)
- Average resolution time
- Department response time
- Duplicate detection accuracy
- API response times
- User registration trends

---

## 12. Future Enhancements

1. **SMS Notifications** - Alert citizens on status updates
2. **Mobile App** - Native Android/iOS applications
3. **AI-based categorization** - Auto-categorize issues from images
4. **Citizen voting** - Upvote critical issues
5. **Gamification** - Reward active citizens
6. **Multi-language support** - Hindi, regional languages
7. **Offline mode** - Report issues without internet

---

**Document Version:** 1.0  
**Last Updated:** 2026-02-10  
**Author:** CivicPulse Development Team
