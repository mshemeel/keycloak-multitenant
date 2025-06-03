## Setup Instructions


1. **Start Keycloak:**
```bash
docker-compose up -d
```

2. **Wait for Startup:**
   Wait for Keycloak to fully start (check logs with `docker-compose logs -f keycloak`)

3. **Access Keycloak Admin Console:**
- URL: http://localhost:8081
- Username: admin
- Password: admin123

## Test Users and Credentials

### Tenant 1:
- **Regular User:**
    - Username: `tenant1user`
    - Password: `password123`
    - Roles: `user`

- **Admin User:**
    - Username: `tenant1admin`
    - Password: `admin123`
    - Roles: `user`, `admin`

### Tenant 2:
- **Regular User:**
    - Username: `tenant2user`
    - Password: `password123`
    - Roles: `user`

- **Admin User:**
    - Username: `tenant2admin`
    - Password: `admin123`
    - Roles: `user`, `admin`

## Getting JWT Tokens for Testing

### For Tenant 1:
```bash
# Get token for tenant1user
curl -X POST http://localhost:8081/realms/tenant1/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=tenant1-client" \
  -d "client_secret=tenant1-client-secret" \
  -d "username=tenant1user" \
  -d "password=password123"

# Get token for tenant1admin
curl -X POST http://localhost:8081/realms/tenant1/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=tenant1-client" \
  -d "client_secret=tenant1-client-secret" \
  -d "username=tenant1admin" \
  -d "password=admin123"
```

### For Tenant 2:
```bash
# Get token for tenant2user
curl -X POST http://localhost:8081/realms/tenant2/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=tenant2-client" \
  -d "client_secret=tenant2-client-secret" \
  -d "username=tenant2user" \
  -d "password=password123"

# Get token for tenant2admin
curl -X POST http://localhost:8081/realms/tenant2/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=tenant2-client" \
  -d "client_secret=tenant2-client-secret" \
  -d "username=tenant2admin" \
  -d "password=admin123"
```

## Testing Your Spring Boot Application

Once you have the JWT tokens, test your Spring Boot app:

```bash
# Test with tenant1 user
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer YOUR_TENANT1_JWT_TOKEN" \
  -H "X-Tenant-ID: tenant1"

# Test with tenant2 admin
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer YOUR_TENANT2_ADMIN_JWT_TOKEN" \
  -H "X-Tenant-ID: tenant2"
```

## Cleanup
```bash
docker-compose down -v
```