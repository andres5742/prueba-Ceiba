# Prueba técnica BTG

Repositorio con la solución separada por partes del enunciado.

## Dónde está cada cosa

| Parte | Carpeta | Qué hay |
|--------|---------|---------|
| Punto 1 (API, modelo NoSQL, despliegue, etc.) | `punto-1-api/` | Proyecto Spring Boot, tests, Docker, CloudFormation, guía de endpoints |
| Punto 2 (SQL relacional BTG) | `punto-2-sql/` | Script `part2_clientes_productos.sql` |

Para levantar o revisar el backend, entrá en **`punto-1-api/`** y seguí su `README.md` (`mvn spring-boot:run`, Swagger, etc.).

**Despliegue en AWS (CloudFormation):** plantilla `punto-1-api/infrastructure/cloudformation.yaml` y pasos en `punto-1-api/infrastructure/DEPLOY.md`.

La consulta del punto 2 está en **`punto-2-sql/part2_clientes_productos.sql`**.

## Git (listo para subir)

El proyecto ya tiene **Git inicializado**, rama **`main`** y un **commit** con todo el código (sin `target/` ni `.env`).

1. Creá un repositorio **público** en GitHub (sin README inicial, o eliminá el README que te genere GitHub si choca con el tuyo).
2. Configurá tu nombre y correo **en esta carpeta** (reemplazá con los tuyos):

   ```bash
   git config user.name "Tu nombre"
   git config user.email "tu-correo@ejemplo.com"
   ```

3. Conectá el remoto y subí:

   ```bash
   git remote add origin https://github.com/TU_USUARIO/TU_REPO.git
   git push -u origin main
   ```

Si GitHub te pide autenticación, usá un **Personal Access Token** como contraseña o el cliente GitHub.
