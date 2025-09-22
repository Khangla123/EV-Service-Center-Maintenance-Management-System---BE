# Test Backend API Health

## 1. Kiểm tra server có chạy không
curl -X GET http://localhost:8080
# Hoặc
Invoke-RestMethod -Uri "http://localhost:8080" -Method Get

## 2. Test Login API
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@evservice.vn\",\"password\":\"123456\"}"

# PowerShell version:
$body = @{
    email = "admin@evservice.vn"
    password = "123456"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $body -ContentType "application/json"

## 3. Test Signup API
curl -X POST http://localhost:8080/api/auth/signup ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@example.com\",\"password\":\"123456\",\"firstName\":\"Test\",\"lastName\":\"User\",\"phone\":\"0123456789\"}"

## Expected Success Response:
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "type": "Bearer",  
  "user": {
    "id": 1,
    "email": "admin@evservice.vn",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN"
  }
}