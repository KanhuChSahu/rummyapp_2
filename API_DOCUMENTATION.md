# Rummy App API Documentation

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
    "id": 1,
    "username": "john_doe_updated",
    "mobileNumber": "+1234567891",
    "avatar": "new_avatar_url_here",
    "verified": false
}
```

## Twilio Setup Guide

### 1. Create Twilio Account
1. Go to [Twilio's website](https://www.twilio.com) and sign up for a new account
2. Complete the verification process

### 2. Get Twilio Credentials
1. Log in to your Twilio Console
2. On the dashboard, you'll find:
   - Account SID
   - Auth Token
   - Click on "Show" to reveal the auth token

### 3. Get Twilio Phone Number
1. In the Twilio Console, go to "Phone Numbers" → "Buy a Number"
2. Choose a phone number that supports SMS
3. Purchase the number

### 4. Configure Application
Update your `application.properties` file with your Twilio credentials:

```properties
twilio.account.sid=your_account_sid_here
twilio.auth.token=your_auth_token_here
twilio.phone.number=your_twilio_phone_number
```

### Testing Tips
1. Use Twilio's test credentials in development
2. Test phone numbers format: Always use E.164 format (e.g., +1234567890)
3. Monitor SMS delivery status in Twilio Console
4. Use Twilio's test phone numbers for development

### Security Best Practices
1. Never commit Twilio credentials to version control
2. Use environment variables in production
3. Implement rate limiting for SMS endpoints
4. Monitor usage to prevent abuse

## API Testing Tools
- Postman
- cURL
- Any HTTP client that supports JSON

Remember to replace placeholder values with actual data when testing the API endpoints.