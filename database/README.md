# Database Schema

## Hướng dẫn setup database

### 1. PostgreSQL Setup
```sql
-- Tạo database
CREATE DATABASE EVServiceCenter;

-- Sử dụng database
\c EVServiceCenter;
```

### 2. Import schema
```bash
psql -U postgres -d EVServiceCenter -f schema.sql
```

### 3. Import data (nếu có)
```bash
psql -U postgres -d EVServiceCenter -f data.sql
```

## Files trong thư mục này:
- `schema.sql` - Database schema và structure
- `data.sql` - Sample data (optional)
- `README.md` - File hướng dẫn này

## Kết nối từ ứng dụng
Cấu hình trong `application.yaml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/EVServiceCenter
    username: postgres
    password: your_password
```
