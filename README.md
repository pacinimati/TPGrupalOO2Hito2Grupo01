# TPGrupalOO2Hito2Grupo01

## Requisitos

- JDK 17
- Maven (o usar el wrapper `mvnw` incluido)
- MySQL (base de datos ya creada, vacía)

## Configuración de la base de datos

Los datos de conexión a la base (URL, usuario y contraseña) se leen de variables de entorno mediante un archivo `.env`, que **no se versiona** (está en `.gitignore`).

1. Copiar la plantilla:

   ```bash
   cp .env.example .env
   ```

2. Editar `.env` con los datos reales de tu base MySQL local:

   ```
   DB_URL=jdbc:mysql://localhost:3306/tpgrupaloo2hito2grupo01
   DB_USERNAME=tu_usuario
   DB_PASSWORD=tu_password
   ```

   El esquema (`tpgrupaloo2hito2grupo01` en el ejemplo) debe existir en MySQL; Hibernate crea/actualiza las tablas automáticamente (`ddl-auto: update`).

## Ejecutar el proyecto

```bash
./mvnw spring-boot:run
```

En Windows (PowerShell/cmd):

```bash
mvnw.cmd spring-boot:run
```

Al iniciar, `spring-dotenv` carga automáticamente las variables del archivo `.env` sin necesidad de exportarlas manualmente en la terminal ni configurarlas en el IDE.

