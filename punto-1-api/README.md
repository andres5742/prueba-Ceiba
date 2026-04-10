# API fondos BTG (Spring Boot)

Backend del punto 1: API REST con Java 21, Spring Boot 3 y MongoDB. Autenticación JWT, reglas de negocio de suscripciones y cancelaciones, idempotencia opcional con cabeceras `Idempotency-Key` / `Idempotency-Version`, y plantilla CloudFormation para desplegar en AWS (ECS + ALB, etc.).

## Qué hay en esta carpeta

- `src/` — código
- `docs/API-ENDPOINTS.md` — cómo probar cada endpoint (Postman, cuerpos, ejemplos)
- `http/api-pruebas.http` — requests para el plugin REST Client de VS Code / Cursor
- `infrastructure/` — `cloudformation.yaml` y `DEPLOY.md` para AWS
- `docker-compose.yml` y `Dockerfile` — entorno local con Mongo + API

## Requisitos

- JDK 21 y Maven 3.9+
- MongoDB accesible (local, Docker o Atlas)
- Docker si querés levantar todo con compose

## Arrancar en local

1. Variables: copiá `.env.example` a `.env` o exportá al menos `MONGODB_URI` y `JWT_SECRET_KEY` (el secreto conviene que sea largo; el default del `application.yml` es solo para desarrollo).

2. Opcional: solo Mongo con Docker:

   ```bash
   docker compose up -d mongo
   ```

3. API con Maven (puerto **8080** por defecto):

   ```bash
   mvn spring-boot:run
   ```

   Swagger: http://localhost:8080/docs

4. O todo con Docker (API en **8000**):

   ```bash
   docker compose up --build
   ```

   Swagger: http://localhost:8000/docs

## Comprobar que responde

```bash
curl http://localhost:8080/health
```

Debería devolver `ok`. Los tests unitarios:

```bash
mvn test
```

## Flujo mínimo en Swagger

Registro → login → copiar `access_token` → Authorize con `Bearer <token>` → listar fondos → suscribirse a un fondo. El JSON va en **snake_case**, como en el enunciado.

## Modelo en MongoDB

Colecciones principales: `funds`, `users`, `subscriptions`, `transactions`, `idempotency_keys`. Saldo inicial del cliente: COP 500.000 (configurable con `INITIAL_BALANCE_COP`).

## Seguridad (resumen)

- Login devuelve JWT; el resto de rutas protegidas lo piden en `Authorization: Bearer ...`
- Contraseñas con BCrypt; rol en el token
- Teléfono opcional cifrado en reposo si configurás `FIELD_ENCRYPTION_KEY`

## Despliegue AWS (CloudFormation)

- **Plantilla:** `infrastructure/cloudformation.yaml`
- **Documentación:** `infrastructure/DEPLOY.md` (stack, ECR, imagen, parámetros, validación)

La API en contenedor usa puerto **8000**; el health check del balanceador es **`/health`**.

## Punto 2 (SQL)

La consulta relacional del enunciado BTG está fuera de este módulo: `../punto-2-sql/part2_clientes_productos.sql`.
