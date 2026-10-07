# TPGrupalOO2Hito1Grupo01
Número de grupo: 
01

## 💻 Integrantes del grupo (nombre, apellido y usuario GitHub):
-Matias Pacini - pacinimati
-Alejo Almirón - alejoalmiron
-Rodrigo Mercado - RodriMercado
-Rodrigo Fernandez - Roi-123

## CASOS DE USO
CARGA DE DATOS - MATIAS PACINI: https://github.com/pacinimati/TPGrupalOO2Hito2Grupo01/pull/7

CASO DE USO 1: REGISTRO DE RESPONSABLE Y CREACION UNIDAD DE VENTA - MATIAS PACINI - https://github.com/pacinimati/TPGrupalOO2Hito2Grupo01/pull/1  https://github.com/pacinimati/TPGrupalOO2Hito2Grupo01/pull/3

CASO DE USO 2: VER FESTIVALES CON SUS UNIDADES DE VENTA (ADMIN) - MATIAS PACINI - https://github.com/pacinimati/TPGrupalOO2Hito2Grupo01/pull/2

CASO DE USO 3: RANKING DE UNIDADES(ADMIN) - MATIAS PACINI - https://github.com/pacinimati/TPGrupalOO2Hito2Grupo01/pull/4

CASO DE USO 4: ALTA DE PEDIDOS - MATIAS PACINI - https://github.com/pacinimati/TPGrupalOO2Hito2Grupo01/pull/5

CASO DE USO 5: UNIDADES CON MAYOR CANON (ADMIN) - MATIAS PACINI - https://github.com/pacinimati/TPGrupalOO2Hito2Grupo01/pull/6


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

