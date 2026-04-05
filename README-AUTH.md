# logistica-auth

Microservicio de autenticación con **arquitectura hexagonal**, **JWT (HS256)**, contraseñas con **BCrypt** y esquema alineado a `tablas_bases_de_datos.txt` (BD Autenticación).

## Base de datos (PostgreSQL local + pgAdmin)

Valores que deben coincidir con `src/main/resources/application.properties`:

| Campo | Valor |
|--------|--------|
| Host | `localhost` |
| Puerto | `5432` |
| Base de datos | `logistica_auth` |
| Usuario | `logistica` |
| Contraseña | `logistica` |

### Paso a paso

1. **Asegúrate de que PostgreSQL está corriendo** en tu Mac (app Postgres, servicio Homebrew, etc.) y escucha en el **puerto 5432** (o cambia la URL en `application.properties` si usas otro).

2. **Crea usuario y base** (Terminal → `psql` con un superusuario que *sí* exista en tu instalación; en Mac con Homebrew suele ser tu usuario de macOS, no `postgres`):

```bash
psql -d postgres
```

Dentro de `psql`:

```sql
CREATE USER logistica WITH PASSWORD 'logistica';
CREATE DATABASE logistica_auth OWNER logistica;
GRANT ALL PRIVILEGES ON DATABASE logistica_auth TO logistica;
\c logistica_auth
GRANT ALL ON SCHEMA public TO logistica;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO logistica;
\q
```

3. **pgAdmin 4** (aplicación en tu Mac):  
   - Clic derecho **Servers** → **Register** → **Server…**  
   - **General → Name:** por ejemplo `Local logistica_auth`  
   - **Connection → Host:** `localhost`  
   - **Port:** `5432`  
   - **Maintenance database:** `logistica_auth`  
   - **Username:** `logistica`  
   - **Password:** `logistica` (puedes marcar *Save password*)  
   - **Save**

4. **Tablas y datos iniciales:** no las crees a mano. Arranca el microservicio una vez (`./mvnw spring-boot:run` o Run en el IDE). **Flyway** aplica las migraciones en `src/main/resources/db/migration`:

- **V1**: tablas `roles`, `modulos`, `permisos`, `roles_permisos`, `usuarios`, `auditoria_autenticacion`, `sesiones_activa`, `refresh_token`, `password_reset_token`.
- **V2**: roles `ADMIN`, `OPERADOR`, `CLIENTE` y usuario de desarrollo `admin@logistica.local` / contraseña **`password`**.

### Nota sobre `refresh_token.token_hash`

En BD el campo cumple el contrato `token_hash`. El valor almacenado para el JWT de refresh es **SHA-256 en hexadecimal** del token (los JWT suelen superar el límite de 72 bytes de BCrypt). Las **contraseñas de usuario** siguen usando **BCrypt**.

## Configuración

Variables útiles:

| Propiedad / env | Descripción |
|-----------------|-------------|
| `spring.datasource.*` | URL, usuario y clave PostgreSQL |
| `JWT_SECRET` o `app.jwt.secret` | Secreto HS256 (≥ 32 bytes) |
| `app.jwt.access-token-minutes` | Vigencia access token (default 15) |
| `app.jwt.refresh-token-days` | Vigencia refresh token (default 7) |

## API

| Método | Ruta | Auth |
|--------|------|------|
| POST | `/api/auth/login` | No |
| POST | `/api/auth/refresh` | No |
| GET | `/api/auth/me` | Bearer access token |

### Ejemplos

Login:

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@logistica.local","password":"password"}'
```

Refresh:

```bash
curl -s -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh_token_jwt>"}'
```

Perfil:

```bash
curl -s http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <access_token>"
```

## Claims del access token

- `sub`: UUID usuario  
- `typ`: `access`  
- `email`  
- `role`: código de rol (ej. `ADMIN`) → Spring Security como `ROLE_ADMIN`  
- `sede`: opcional, `codigo_sede_asignada`

## Estructura hexagonal (resumen)

- `domain`: modelos y puertos (in/out)  
- `application`: casos de uso (`AuthenticateUserService`)  
- `adapter.in.web`: REST, DTOs, seguridad filtro JWT  
- `adapter.out.persistence`: JPA, adaptadores de repositorio y auditoría  
- `adapter.out.security`: BCrypt, firma JWT, refresh token  
