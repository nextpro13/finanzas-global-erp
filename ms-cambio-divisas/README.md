# ms-cambio-divisas — FINANZAS GLOBAL S.A.C.

Microservicio de tipos de cambio y operaciones de compra/venta de divisas (arquitectura hexagonal).

## Ejecutar
```bash
mvn spring-boot:run                 # perfil por defecto: H2 en memoria, clientes y eventos locales
mvn verify                          # pruebas + reporte JaCoCo en target/site/jacoco/index.html
bash pruebas/pruebas-api.sh         # pruebas funcionales y de seguridad con curl (servicio levantado)
docker compose --env-file .env up --build   # PostgreSQL + RabbitMQ + servicio (copiar .env.example a .env)
```

## Usuarios de demostración (password por defecto en dev: `Demo#2026`)
| Usuario | Rol | Oficina |
|---|---|---|
| cajero01 | CAJERO | LIMA-01 |
| supervisor01 / supervisor02 | SUPERVISOR | LIMA-01 / LIMA-02 |
| admin01 | ADMIN | CENTRAL |
| auditor01 | AUDITOR | CENTRAL |

## Endpoints
| Método | Ruta | Roles |
|---|---|---|
| POST | /api/v1/auth/login | público |
| GET | /api/v1/tipos-cambio/{moneda} | autenticado |
| PUT | /api/v1/tipos-cambio/{moneda} | ADMIN, SUPERVISOR |
| GET | /api/v1/tipos-cambio/{moneda}/historial | ADMIN, SUPERVISOR, AUDITOR |
| POST | /api/v1/operaciones (header `Idempotency-Key`) | CAJERO, SUPERVISOR |
| GET | /api/v1/operaciones/{id} | CAJERO, SUPERVISOR, ADMIN, AUDITOR |
| POST | /api/v1/operaciones/{id}/aprobar | SUPERVISOR |
Pipeline de CI activo con GitHub Actions.
