# Probar la API (Postman, curl o Swagger)

Base URL: **`http://localhost:8080`** si corrés con `mvn spring-boot:run`. Con Docker Compose suele ser **`http://localhost:8000`** — solo cambiá el puerto en las URLs.

Tené Mongo accesible y la API levantada (log `Started FondosApiApplication`). Documentación interactiva: **`/docs`**.

---

## Cabeceras habituales

- JSON: `Content-Type: application/json`
- Rutas protegidas: `Authorization: Bearer <access_token>` (un espacio después de `Bearer`)

En Postman conviene un environment con `base_url` y, después del login, variable `token` para `Bearer {{token}}`.

---

## Endpoints

### `GET /health`

Sin auth. Respuesta **200**, cuerpo texto: `ok`.

---

### `POST /auth/register`

Body ejemplo:

```json
{
  "email": "demo@test.com",
  "password": "clave12345",
  "notification_preference": "email"
}
```

`notification_preference` solo puede ser **`email`** o **`sms`** (minúsculas). El mail va en `email`, no en la preferencia.

Para SMS hace falta teléfono:

```json
{
  "email": "demo@test.com",
  "password": "clave12345",
  "notification_preference": "sms",
  "phone": "+573001234567"
}
```

**201** si sale bien. **409** si el correo ya existe (podés hacer login en vez de registrar de nuevo).

---

### `POST /auth/login`

```json
{
  "email": "demo@test.com",
  "password": "clave12345"
}
```

**200** con `access_token` y `token_type`. Copiá el valor de `access_token` para el header Bearer en todo lo que sigue.

---

### `GET /auth/me`

Con Bearer. **200**: email, `balance_cop`, preferencia, rol. Cuenta nueva: saldo típico **500000**.

---

### `GET /funds`

Con Bearer. Lista el catálogo (5 fondos). Para probar barato: **`fund_id` 3** (`DEUDAPRIVADA`, mínimo 50.000 COP).

---

### `POST /funds/{fund_id}/subscribe`

Con Bearer. Sin body.

Ejemplo: `POST /funds/3/subscribe`

Opcional, idempotencia:

- `Idempotency-Key`: string único por “intento de negocio”
- `Idempotency-Version`: entero ≥ 1 (si no mandás, vale 1)

Misma clave + misma versión en un reintento → misma respuesta sin volver a ejecutar. Clave repetida con versión distinta a la ya guardada → **409** (evita efectos raros en reintentos).

**200** con `subscription_id` — guardalo para cancelar.

Errores frecuentes: **409** ya suscripto a ese fondo; **409** saldo insuficiente; **404** fondo inexistente.

---

### `GET /subscriptions`

Con Bearer. Tus suscripciones activas (y las que ya cancelaste si aplica).

---

### `GET /transactions`

Con Bearer. Historial de `subscribe` / `cancel`.

---

### `DELETE /subscriptions/{subscription_id}`

Con Bearer. Sin body. Reemplazá `{subscription_id}` por el UUID que devolvió el subscribe (o el que ves en la lista).

Opcional: mismas cabeceras `Idempotency-Key` / `Idempotency-Version` que en subscribe.

**200** al cancelar; el saldo vuelve a subir el monto bloqueado.

---

## Orden mínimo sugerido

1. `GET /health`
2. `POST /auth/register` (o login si ya existís)
3. `POST /auth/login` → token
4. `GET /auth/me`
5. `GET /funds`
6. `POST /funds/3/subscribe` → `subscription_id`
7. `GET /subscriptions` / `GET /transactions`
8. `DELETE /subscriptions/{subscription_id}`

Para ver el conflicto de doble suscripción: repetí el mismo `POST .../subscribe` al mismo fondo con usuario ya suscripto → **409**.

---

## OpenAPI (importar en Postman)

`GET http://localhost:8080/v3/api-docs` (sin auth) — JSON de la spec.

---

## Más ayuda en el repo

- `http/api-pruebas.http` — requests para REST Client (VS Code / Cursor); definí `@token` después del login.
- `README.md` en la carpeta del API — cómo levantar Mongo y la app.

Si el registro devuelve **400**, revisá que `notification_preference` sea exactamente `email` o `sms`, no un correo.
