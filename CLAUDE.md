# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Contexto

TP grupal de Orientación a Objetos II (UNLa, 2026), Hito 2: sistema de gestión "Epicentro Gourmet" (festivales, unidades de venta, personal, pedidos). El enunciado completo está en `Trabajo Practico OO2 Hito 2 ENUNCIADO.pdf`; el diagrama de clases en `docs/diagrama-clases.mmd`. El código y los nombres del dominio están en español; el README lista los casos de uso ya entregados (cada uno vía PR).

Stack: Java 17, Spring Boot (Web, Thymeleaf, Data JPA, Security, Validation), MySQL, Lombok, ModelMapper, spring-dotenv, Maven.

## Comandos

```bash
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
./mvnw test
./mvnw test -Dtest=NombreTest#metodo   # un solo test
./mvnw clean package
```

- Requiere MySQL con el esquema ya creado (vacío). Copiar `.env.example` a `.env` (no versionado) y completar `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`; `spring-dotenv` lo carga solo. Opcionales: `ADMIN_USERNAME` (default `admin`) y `ADMIN_PASSWORD` (sin default).
- `ddl-auto: update`: Hibernate crea/actualiza las tablas pero **nunca borra columnas**: si una entidad pierde un campo, la columna vieja `NOT NULL` queda y los inserts fallan con 500 (pasó con `contrasenia`/`usuario` en `responsable`/`administrador`). Solución: `ALTER TABLE ... DROP COLUMN`, o recrear la base. No hay linter configurado.
- Compilar con JDK 17–21: con JDK 26 Lombok falla (errores "cannot find symbol" en getters/setters). Si `java -version` da otra versión, definir `JAVA_HOME`.
- Los tests actuales son mínimos (`contextLoads` necesita la DB accesible).

## Arquitectura

Paquete base `com.unla.tpgrupaloo2hito2grupo01`, capas clásicas: `controllers` (MVC con Thymeleaf en `templates/`, REST en `controllers/api/v1`) → `services` (interfaz `IXService` + `services/implementation`) → `repositories` (`IXRepository`, Spring Data) → `entities`. `dtos` para formularios/respuestas; `ModelMapperConfig` para mapear.

- **Restos de plantilla**: `Person`, `Degree`, `Client` y sus controllers/servicios/vistas (`person/`, `degree/`) vienen de un proyecto base de la cátedra, no del dominio del TP. El dominio real es Festival / UnidadVenta / Personal / Plato / Pedido.
- **Herencia JPA JOINED** en dos jerarquías: `UnidadVenta` (abstracta) → `FoodTruck`, `PuestoDesarmable`; `Personal` (abstracta) → `Responsable`, `Cocinero`, `Cajero`, etc. Los enunciados piden cálculos polimórficos (canon por tipo de unidad, sueldo por rol).
- **Baja lógica**: `Personal.eliminado`; las consultas de empleados deben filtrar los eliminados. `Personal` valida mayoría de edad (18) en `@PrePersist/@PreUpdate`.
- **Responsable antes que unidad**: `Personal.unidadVenta` es nullable porque el Responsable se crea antes que su unidad. El registro público (`RegistroController`, `/registro/**`, `RegistroUnidadDTO`, `TipoUnidad`) crea usuario + Responsable + UnidadVenta juntos. `UnidadVenta.codigoUnico` tiene 10 caracteres, único, con validación propia.
- **Autenticación**: `User` + `UserRole` (roles como `ROLE_ADMIN`; Responsable y Empleado también); `Personal.user` enlaza al usuario. `UserService` (interfaz `IUserService`, que extiende `UserDetailsService`) carga usuario y roles y lanza `UsernameNotFoundException` si no existe; `SecurityConfiguration` usa form login (`/login`, `/loginprocess`, `/loginsuccess`), BCrypt, CSRF deshabilitado, `@EnableMethodSecurity`. Públicos: `/`, `/index`, `/registro/**`, `/api/v1/**`, estáticos; el resto requiere autenticación.
- **Datos precargados**: `components/DataInitializer` (CommandLineRunner) crea festivales y el usuario Administrador si no existen (necesita `app.admin.password`). Los pedidos también deben estar precargados según el enunciado.

## Convenciones de capas (criterio de la cátedra)

- **Controller**: solo recibe la petición, llama al service y devuelve la vista (`ModelAndView` / `String`). Sin reglas de negocio, sin cálculos, sin repositories. Vuelca al `BindingResult` los errores que le devuelve el service; no los decide.
- **Service**: toda la lógica y las reglas de negocio (validaciones de negocio, cálculos, coordinación de repositories). Cada service es interfaz `IXService` + clase en `services/implementation`, inyectada por constructor y por interfaz. Solo los métodos públicos van en la interfaz; los `private` son detalle interno. `@Transactional` acá (`readOnly = true` en consultas).
- **Repository**: solo acceso a datos (`IXRepository`, Spring Data; `@Query` con `JOIN FETCH` para evitar N+1 en relaciones LAZY).
- **Validación**: formato del formulario con Bean Validation en el DTO; reglas de negocio en el service, que devuelve `List<ErrorCampoDTO>` (ver `UnidadVentaService.validarReglasDeNegocio`).
- **DTOs**: para datos que combinan relaciones o subtipos (ej. `UnidadVentaDTO`) y para reportes. Pasar una entidad simple a la vista (ej. `Festival`, sin relaciones LAZY que la vista lea) es aceptable; si la vista necesita relaciones, usar DTO.
- **Reglas de negocio**: no hardcodearlas dentro de la lógica de cálculo; van en una constante única o config (ej. la edad mínima 18 hoy está duplicada en `Personal` y `UnidadVentaService`: unificarla al tocar esas clases). Cargar datos iniciales (festivales con sus montos, Administrador) en `DataInitializer` es válido: los montos son campos de `Festival`.
- `Person`/`Degree`/`Client` son plantilla: no usarlos como modelo de estilo para código nuevo del dominio.

## Reglas del enunciado a respetar

- No hardcodear valores de negocio (sueldo base $100.000, $5.000 por año de antigüedad, plus por categoría de cocina, $500/m², $2.000 por electricidad, $10/min de montaje): encapsularlos en una clase/config.
- Canon: FoodTruck = superficie*500 (+2000 si usa electricidad); Puesto = superficie*500 − minutosMontaje*10.
- Sueldo: multiplicador `(int(díasFestival/15)+1) * 50%` sobre (base + plus categoría) para cocina o (base + antigüedad) para cajero.
- Reportes (`ReporteVentaDTO`, `ReporteMayoresCanonDTO`, `PlatoDTO`) son DTOs no persistentes.
- Alta de empleado envía mail con usuario (su email) y contraseña aleatoria; el recibo de sueldo se descarga en PDF; el cajero cierra caja. Las acciones se habilitan por rol (Administrador / Responsable / Empleado).
