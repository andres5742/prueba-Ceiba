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
