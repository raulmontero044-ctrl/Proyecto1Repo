# Proyecto1Repo

## Arranque con Docker Compose

1. Opcional: copia `.env.example` como `.env` y cambia los valores. Docker Compose lee `.env` automáticamente y git lo ignora. Sin `.env`, `compose.yaml` usa valores por defecto solo válidos para desarrollo local.
2. Arranca la base de datos y la aplicación:

   ```powershell
   docker compose up --build
   ```

La aplicación queda en `http://localhost:8080` y MariaDB en el puerto `3307` del host.

## Primer administrador

Al arrancar, si la tabla `usuario` está vacía, la aplicación crea un administrador con `ADMIN_EMAIL` y `ADMIN_PASSWORD`. Si ya hay usuarios, esas variables se ignoran.

Si no las defines en `.env`, `compose.yaml` usa por defecto `admin@facturacion.local` / `admin1234`. Cámbialas fuera del entorno local.

Inicia sesión con él para obtener el token:

```powershell
$login = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/auth/login `
  -ContentType "application/json" `
  -Body '{"email":"admin@facturacion.local","password":"<ADMIN_PASSWORD>"}'
$h = @{ Authorization = "Bearer $($login.accessToken)" }
```

Con `-Headers $h` ya puedes crear empresas (`POST /api/empresas`) y usuarios (`POST /api/usuarios`). El access token caduca a los 5 minutos: renuévalo con `POST /api/auth/refresh` y el `refreshToken`.
