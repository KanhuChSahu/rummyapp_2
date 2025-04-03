# Rummy App API Documentation

## Development Setup

### PostgreSQL Installation and Setup
1. Download PostgreSQL from [postgresql.org](https://www.postgresql.org/download/)
2. Run the installer and follow the setup wizard
3. Remember the password you set for the postgres user
4. Verify installation by opening pgAdmin or psql command line tool

### Database Setup
```sql
-- Create database
CREATE DATABASE rummydb;

-- Connect to database
\c rummydb

-- Create users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    mobile_number VARCHAR(15) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    verified BOOLEAN DEFAULT FALSE,
    otp VARCHAR(6),
    otp_expiry_time TIMESTAMP,
    avatar VARCHAR(255),
    last_login_ip VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Basic Git Commands
```bash
# Initialize repository
git init

# Clone repository
git clone <repository-url>

# Add files to staging
git add .

# Commit changes
git commit -m "commit message"

# Push changes
git push origin main

# Pull latest changes
git pull origin main

# Create and switch to new branch
git checkout -b feature-branch

# Switch branches
git checkout main
```

## Authentication Endpoints

### 1. User Registration
**Endpoint:** `POST /api/users/register`

**Request Payload:**
```json
{
    "username": "john_doe",
    "mobileNumber": "+1234567890",
    "password": "securePassword123",
    "confirmPassword": "securePassword123"
}
```

**Response:**
```json
{
    "message": "Registration successful. Please verify your mobile number.",
    "userId": 1,
    "mobileNumber": "+1234567890"
}
```

### 2. OTP Verification
**Endpoint:** `POST /api/users/verify-otp`

**Request Parameters:**
- `mobileNumber`: The registered mobile number
- `otp`: The 6-digit OTP received via SMS

**Response:**
```json
{
    "message": "Mobile number verified successfully"
}
```

### 3. Login OTP Request
**Endpoint:** `POST /api/users/login/request-otp`

**Request Parameters:**
- `mobileNumber`: The registered mobile number

**Response:**
```json
{
    "message": "OTP sent successfully"
}
```

### 4. User Login
**Endpoint:** `POST /api/users/login`

**Request Payload:**
```json
{
    "mobileNumber": "+1234567890",
    "password": "securePassword123"
}
```

**Response:**
```json
{
    "token": "jwt_token_here",
    "userId": 1,
    "username": "john_doe"
}
```

## User Profile Endpoints

### 1. Get User Profile
**Endpoint:** `GET /api/users/profile/{userId}`

**Response:**
```json
{
    "id": 1,
    "username": "john_doe",
    "mobileNumber": "+1234567890",
    "avatar": "avatar_url_here",
    "verified": true
}
```

### 2. Update User Profile
**Endpoint:** `PUT /api/users/profile/{userId}`

**Request Payload:**
```json
{
    "username": "john_doe_updated",
    "mobileNumber": "+1234567891",
    "avatar": "new_avatar_url_here"
}
```

**Response:**
```json
{
    "message": "Profile updated successfully",
    "user": {
        "id": 1,
        "username": "john_doe_updated",
        "mobileNumber": "+1234567891",
        "avatar": "new_avatar_url_here",
        "verified": false
    }
}
```