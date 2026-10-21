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

CASO DE USO 6: ALTAS, BAJAS Y MODIFICACIONES DE EMPLEADOS (RESPONSABLE) - MATIAS PACINI - https://github.com/pacinimati/TPGrupalOO2Hito2Grupo01/pull/8

CASO DE USO 7: LIQUIDACION DE HABERES - RODRIGO MERCADO - https://github.com/pacinimati/TPGrupalOO2Hito2Grupo01/pull/11

CASO DE USO 8: REPORTES DE RECAUDACION - RODRIGO MERCADO - https://github.com/pacinimati/TPGrupalOO2Hito2Grupo01/pull/9

CASO DE USO 9: CALCULO DE RENTABILIDAD NETA - RODRIGO MERCADO - MATIAS PACINI https://github.com/pacinimati/TPGrupalOO2Hito2Grupo01/pull/10 https://github.com/pacinimati/TPGrupalOO2Hito2Grupo01/pull/12

CASO DE USO 10: CALCULO DE RENTABILIDAD NETA POR FECHAS - RODRIGO MERCADO https://github.com/pacinimati/TPGrupalOO2Hito2Grupo01/pull/14

CASO DE USO 11: CONSULTA DE FESTIVAL ACTIVO Y UNIDAD DE TRABAJO (EMPLEADO) - ALEJO ALMIRON https://github.com/pacinimati/TPGrupalOO2Hito2Grupo01/pull/15

CASO DE USO 12: CIERRE DE CAJA DIARIO (CAJERO) - ALEJO ALMIRON https://github.com/pacinimati/TPGrupalOO2Hito2Grupo01/pull/16

CASO DE USO 13: PLATO ESTRELLA (RESPONSABLE) - MATIAS PACINI https://github.com/pacinimati/TPGrupalOO2Hito2Grupo01/pull/17

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

## Configuración del envío de mails

El alta de un empleado (y la reactivación de uno eliminado) envía un mail con su usuario (su email) y una contraseña generada aleatoriamente. Para que el envío funcione hay que configurar en el `.env` una cuenta de Gmail desde la cual se manda el mail. Si el mail no se puede enviar, el alta se revierte y el formulario avisa el error.

### Cómo configurar la cuenta de Gmail en el `.env`

1. Usar una cuenta de Gmail que tenga activada la **verificación en 2 pasos** (se activa en https://myaccount.google.com/security).
2. Generar una **contraseña de aplicación** (no es la contraseña de la cuenta): entrar a https://myaccount.google.com/apppasswords, ponerle un nombre (por ejemplo `Epicentro`) y crear. Google muestra una clave de 16 letras, una sola vez.
3. Completar en el `.env` las variables de mail. `MAIL_PASSWORD` va **sin espacios**:

   ```
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=tu.casilla@gmail.com
   MAIL_PASSWORD=abcdefghijklmnop
   ```

4. Reiniciar la aplicación para que tome los cambios.

Los mails salen desde esa casilla (`MAIL_USERNAME`), así que el empleado los recibe de esa cuenta; si no aparece en la bandeja de entrada hay que revisar spam. El `.env` no se versiona, por lo que la clave no se sube al repositorio.

Al iniciar, `spring-dotenv` carga automáticamente las variables del archivo `.env` sin necesidad de exportarlas manualmente en la terminal ni configurarlas en el IDE.

## EJEMPLO DE VALIDACIONES DE CARGA

Matias Pacini:
Registro de unidad de venta + Responsable:
El usuario es obligatorio y no puede estar ya en uso.
La contraseña debe tener al menos 6 caracteres.
El Responsable debe ser mayor de edad (18).

Alta de pedido:
Hay que elegir la unidad de venta, y el usuario solo puede cargar pedidos en la suya (el Administrador, en cualquiera).
La fecha debe estar dentro de las fechas del festival de esa unidad.
Debe haber al menos un plato con cantidad mayor a 0.

Alejo Almirón:
Caso de uso 12: cierre de caja diario (Cajero).
El efectivo contado es obligatorio, no puede ser negativo y admite hasta dos decimales.
Solo se permite un cierre por unidad y fecha, y únicamente durante las fechas del festival activo.


## EJEMPLO DE DTO DE REPORTE
1. Unidades con mayor canon (ReporteMayoresCanonDTO):
   Muestra el nombre comercial, el código único, el tipo de unidad y el canon de cada unidad del festival.
   El canon no se guarda en la base: se calcula al armar el reporte según el tipo de unidad.

2. Ranking de unidades por recaudación (RankingUnidadDTO):
   Muestra el nombre comercial, el código único, el tipo de unidad, el responsable, la cantidad de pedidos y la recaudación.
   La cantidad de pedidos y la recaudación se calculan a partir de los pedidos de la unidad.

3. El responsable se muestra como texto (nombre y apellido), no como la entidad completa.

   Los DTOs de reporte no se guardan en la base de datos: solo llevan los datos que se muestran en pantalla, ya calculados por el service.
