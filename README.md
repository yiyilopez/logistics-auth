# logistica-auth

Autenticación con **JWT (HS256)**, **BCrypt**, **PostgreSQL** y **Flyway**. Arquitectura hexagonal (`domain` → `application` → `adapter.in` / `adapter.out`).

## Requisitos

- JDK 17+
- PostgreSQL accesible desde la URL de `application.properties`

## Arranque

```bash
./mvnw spring-boot:run
```

API en `http://localhost:8080`.

## API

| Método | Ruta | Auth |
|--------|------|------|
| POST | `/api/auth/login` | No |
| POST | `/api/auth/refresh` | No |
| GET | `/api/auth/me` | `Authorization: Bearer <access>` |

```bash
# Login
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@logistica.local","password":"password"}'

# Refresh (pega el JWT de refresh del login)
curl -s -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh_token>"}'

# Perfil
curl -s http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <access_token>"
```

Respuesta login/refresh incluye `accessToken`, `refreshToken`, `tokenType` (`Bearer`), `expiresIn` (segundos). En BD, `refresh_token.token_hash` guarda **SHA-256 hex** del JWT de refresh (no BCrypt del JWT).

## Paquetes

- `domain` — modelos y puertos  
- `application` — casos de uso  
- `adapter.in.web` — REST, DTOs, Spring Security / filtro JWT  
- `adapter.out.persistence` — JPA  
- `adapter.out.security` — JWT, BCrypt, refresh  
