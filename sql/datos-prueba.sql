-- =====================================================================
-- Datos de prueba para la defensa - Epicentro Gourmet (TP Grupal OO2 Hito 2)
-- =====================================================================
-- Uso:
--   1. Crear el esquema vacío en MySQL y levantar la app UNA vez (ddl-auto: update
--      crea las tablas y DataInitializer precarga festivales, platos y admin).
--   2. Ejecutar este script sobre ese esquema:  mysql -u <user> -p <esquema> < sql/datos-prueba.sql
--
-- Es compatible con DataInitializer: los festivales, platos y el Administrador
-- solo se insertan si todavía no existen. El resto (usuarios, unidades, personal,
-- pedidos) se inserta siempre, por lo que el script se ejecuta UNA sola vez
-- (una segunda ejecución falla por usuarios/códigos únicos duplicados).
--
-- Contraseña de TODOS los usuarios creados por este script: Test1234
-- (hash BCrypt). El Administrador conserva la contraseña de ADMIN_PASSWORD si ya
-- existía; si lo crea este script, también es Test1234.
--
-- Usuarios:
--   admin                       ROLE_ADMIN
--   carlos.gomez                ROLE_RESPONSABLE  -> La Parrilla Rodante (Food Truck, Quilmes Rock)
--   lucia.fernandez             ROLE_RESPONSABLE  -> Cervecería del Sur (Puesto, Quilmes Rock)
--   martin.rossi                ROLE_RESPONSABLE  -> Tacos del Sol (Food Truck, Cosquín Rock)
--   sofia.herrera               ROLE_RESPONSABLE  -> Dulces Sierras (Puesto, Cosquín Rock)
--   julian.pereyra              ROLE_RESPONSABLE  -> Burger Bros (Food Truck, Lollapalooza)
--   valeria.nunez               ROLE_RESPONSABLE  -> Sabores de Mar (Puesto, Epicentro Gourmet Fest)
--   diego.suarez                ROLE_RESPONSABLE  -> Pizza Móvil (Food Truck, Epicentro Gourmet Fest)
--   <email del empleado>        ROLE_EMPLEADO     (cocineros y cajeros, ver sección de personal)
-- =====================================================================

START TRANSACTION;

-- ---------------------------------------------------------------------
-- Festivales (los 3 de DataInitializer + uno de 31 días para probar el
-- multiplicador de sueldo por duración del evento)
-- ---------------------------------------------------------------------

INSERT INTO festival (nombre, temporada, fecha_inicio, fecha_fin, costo_por_superficie, costo_por_montaje, plus_uso_electricidad, sueldo_base)
SELECT 'Lollapalooza', 'Otoño', '2027-03-12', '2027-03-14', 500, 10, 2000, 100000 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festival WHERE nombre = 'Lollapalooza');

INSERT INTO festival (nombre, temporada, fecha_inicio, fecha_fin, costo_por_superficie, costo_por_montaje, plus_uso_electricidad, sueldo_base)
SELECT 'Cosquín Rock', 'Verano', '2027-02-13', '2027-02-14', 500, 10, 2000, 100000 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festival WHERE nombre = 'Cosquín Rock');

INSERT INTO festival (nombre, temporada, fecha_inicio, fecha_fin, costo_por_superficie, costo_por_montaje, plus_uso_electricidad, sueldo_base)
SELECT 'Quilmes Rock', 'Primavera', '2026-11-20', '2026-11-22', 500, 10, 2000, 100000 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festival WHERE nombre = 'Quilmes Rock');

INSERT INTO festival (nombre, temporada, fecha_inicio, fecha_fin, costo_por_superficie, costo_por_montaje, plus_uso_electricidad, sueldo_base)
SELECT 'Epicentro Gourmet Fest', 'Verano', '2026-12-01', '2026-12-31', 500, 10, 2000, 100000 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festival WHERE nombre = 'Epicentro Gourmet Fest');

SET @f_lolla = (SELECT MIN(id) FROM festival WHERE nombre = 'Lollapalooza');
SET @f_cosquin = (SELECT MIN(id) FROM festival WHERE nombre = 'Cosquín Rock');
SET @f_quilmes = (SELECT MIN(id) FROM festival WHERE nombre = 'Quilmes Rock');
SET @f_epicentro = (SELECT MIN(id) FROM festival WHERE nombre = 'Epicentro Gourmet Fest');

-- ---------------------------------------------------------------------
-- Platos (los 8 de DataInitializer + 2 extra) con precio de venta y costo
-- ---------------------------------------------------------------------

INSERT INTO plato (nombre, precio_venta, costo_produccion)
SELECT 'Hamburguesa completa', 8500, 3500 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM plato WHERE nombre = 'Hamburguesa completa');
INSERT INTO plato (nombre, precio_venta, costo_produccion)
SELECT 'Pizza muzzarella', 9000, 3200 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM plato WHERE nombre = 'Pizza muzzarella');
INSERT INTO plato (nombre, precio_venta, costo_produccion)
SELECT 'Tacos x3', 7000, 2800 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM plato WHERE nombre = 'Tacos x3');
INSERT INTO plato (nombre, precio_venta, costo_produccion)
SELECT 'Choripán', 5000, 1800 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM plato WHERE nombre = 'Choripán');
INSERT INTO plato (nombre, precio_venta, costo_produccion)
SELECT 'Empanadas x3', 4500, 1500 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM plato WHERE nombre = 'Empanadas x3');
INSERT INTO plato (nombre, precio_venta, costo_produccion)
SELECT 'Papas fritas', 4000, 1200 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM plato WHERE nombre = 'Papas fritas');
INSERT INTO plato (nombre, precio_venta, costo_produccion)
SELECT 'Cerveza artesanal', 4000, 1500 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM plato WHERE nombre = 'Cerveza artesanal');
INSERT INTO plato (nombre, precio_venta, costo_produccion)
SELECT 'Limonada', 2500, 600 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM plato WHERE nombre = 'Limonada');
INSERT INTO plato (nombre, precio_venta, costo_produccion)
SELECT 'Brownie con helado', 5500, 1900 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM plato WHERE nombre = 'Brownie con helado');
INSERT INTO plato (nombre, precio_venta, costo_produccion)
SELECT 'Gaseosa', 2000, 700 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM plato WHERE nombre = 'Gaseosa');
SET @pl_hamb = (SELECT MIN(id) FROM plato WHERE nombre = 'Hamburguesa completa');
SET @pl_pizza = (SELECT MIN(id) FROM plato WHERE nombre = 'Pizza muzzarella');
SET @pl_tacos = (SELECT MIN(id) FROM plato WHERE nombre = 'Tacos x3');
SET @pl_chori = (SELECT MIN(id) FROM plato WHERE nombre = 'Choripán');
SET @pl_emp = (SELECT MIN(id) FROM plato WHERE nombre = 'Empanadas x3');
SET @pl_papas = (SELECT MIN(id) FROM plato WHERE nombre = 'Papas fritas');
SET @pl_cerv = (SELECT MIN(id) FROM plato WHERE nombre = 'Cerveza artesanal');
SET @pl_limo = (SELECT MIN(id) FROM plato WHERE nombre = 'Limonada');
SET @pl_brow = (SELECT MIN(id) FROM plato WHERE nombre = 'Brownie con helado');
SET @pl_gas = (SELECT MIN(id) FROM plato WHERE nombre = 'Gaseosa');

-- ---------------------------------------------------------------------
-- Usuario y personal Administrador (solo si no existe)
-- ---------------------------------------------------------------------
INSERT INTO `user` (username, password, enabled, created_at, updated_at)
SELECT 'admin', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE username = 'admin');
SET @u_admin = (SELECT id FROM `user` WHERE username = 'admin');

INSERT INTO user_role (user_id, role, created_at, updated_at)
SELECT @u_admin, 'ROLE_ADMIN', NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user_role WHERE user_id = @u_admin AND role = 'ROLE_ADMIN');

INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
SELECT 'Administrador', 'Sistema', 0, '1990-01-01', CURDATE(), 0, 0, NULL, @u_admin FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM personal WHERE user_id = @u_admin);

INSERT INTO administrador (id)
SELECT p.id FROM personal p
WHERE p.user_id = @u_admin AND NOT EXISTS (SELECT 1 FROM administrador a WHERE a.id = p.id);

-- ---------------------------------------------------------------------
-- Responsables (usuario + personal) y sus unidades de venta
-- El Responsable se crea antes que su unidad: personal.unidad_venta_id se
-- completa con un UPDATE una vez creada la unidad.
-- ---------------------------------------------------------------------

-- La Parrilla Rodante
INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('carlos.gomez', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @user_u1 = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@user_u1, 'ROLE_RESPONSABLE', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Carlos', 'Gómez', 30111222, '1985-04-12', '2024-03-01', 100000, 0, NULL, @user_u1);
SET @resp_u1 = LAST_INSERT_ID();
INSERT INTO responsable (id) VALUES (@resp_u1);
INSERT INTO unidad_venta (nombre_comercial, responsable_id, superficie_m2, codigo_unico, festival_id)
VALUES ('La Parrilla Rodante', @resp_u1, 18, 'FT-QR-0001', @f_quilmes);
SET @u1 = LAST_INSERT_ID();
INSERT INTO food_truck (id, patente, conexion_electrica) VALUES (@u1, 'AB123CD', 1);
UPDATE personal SET unidad_venta_id = @u1 WHERE id = @resp_u1;
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u1, @pl_chori);
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u1, @pl_hamb);
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u1, @pl_papas);
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u1, @pl_cerv);

-- Cervecería del Sur
INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('lucia.fernandez', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @user_u2 = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@user_u2, 'ROLE_RESPONSABLE', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Lucía', 'Fernández', 32444555, '1990-09-30', '2023-06-15', 100000, 0, NULL, @user_u2);
SET @resp_u2 = LAST_INSERT_ID();
INSERT INTO responsable (id) VALUES (@resp_u2);
INSERT INTO unidad_venta (nombre_comercial, responsable_id, superficie_m2, codigo_unico, festival_id)
VALUES ('Cervecería del Sur', @resp_u2, 12, 'PD-QR-0001', @f_quilmes);
SET @u2 = LAST_INSERT_ID();
INSERT INTO puesto_desarmable (id, carpas_componen, tiempo_montaje) VALUES (@u2, 3, 45);
UPDATE personal SET unidad_venta_id = @u2 WHERE id = @resp_u2;
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u2, @pl_cerv);
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u2, @pl_emp);
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u2, @pl_papas);
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u2, @pl_limo);

-- Tacos del Sol
INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('martin.rossi', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @user_u3 = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@user_u3, 'ROLE_RESPONSABLE', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Martín', 'Rossi', 29888111, '1982-01-25', '2022-11-10', 100000, 0, NULL, @user_u3);
SET @resp_u3 = LAST_INSERT_ID();
INSERT INTO responsable (id) VALUES (@resp_u3);
INSERT INTO unidad_venta (nombre_comercial, responsable_id, superficie_m2, codigo_unico, festival_id)
VALUES ('Tacos del Sol', @resp_u3, 15, 'FT-CR-0001', @f_cosquin);
SET @u3 = LAST_INSERT_ID();
INSERT INTO food_truck (id, patente, conexion_electrica) VALUES (@u3, 'AC456EF', 0);
UPDATE personal SET unidad_venta_id = @u3 WHERE id = @resp_u3;
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u3, @pl_tacos);
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u3, @pl_emp);
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u3, @pl_limo);
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u3, @pl_cerv);

-- Dulces Sierras
INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('sofia.herrera', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @user_u4 = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@user_u4, 'ROLE_RESPONSABLE', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Sofía', 'Herrera', 35222333, '1994-07-08', '2025-01-20', 100000, 0, NULL, @user_u4);
SET @resp_u4 = LAST_INSERT_ID();
INSERT INTO responsable (id) VALUES (@resp_u4);
INSERT INTO unidad_venta (nombre_comercial, responsable_id, superficie_m2, codigo_unico, festival_id)
VALUES ('Dulces Sierras', @resp_u4, 9, 'PD-CR-0001', @f_cosquin);
SET @u4 = LAST_INSERT_ID();
INSERT INTO puesto_desarmable (id, carpas_componen, tiempo_montaje) VALUES (@u4, 2, 30);
UPDATE personal SET unidad_venta_id = @u4 WHERE id = @resp_u4;
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u4, @pl_brow);
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u4, @pl_limo);
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u4, @pl_gas);

-- Burger Bros
INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('julian.pereyra', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @user_u5 = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@user_u5, 'ROLE_RESPONSABLE', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Julián', 'Pereyra', 31555666, '1988-12-03', '2021-09-01', 100000, 0, NULL, @user_u5);
SET @resp_u5 = LAST_INSERT_ID();
INSERT INTO responsable (id) VALUES (@resp_u5);
INSERT INTO unidad_venta (nombre_comercial, responsable_id, superficie_m2, codigo_unico, festival_id)
VALUES ('Burger Bros', @resp_u5, 20, 'FT-LL-0001', @f_lolla);
SET @u5 = LAST_INSERT_ID();
INSERT INTO food_truck (id, patente, conexion_electrica) VALUES (@u5, 'AD789GH', 1);
UPDATE personal SET unidad_venta_id = @u5 WHERE id = @resp_u5;
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u5, @pl_hamb);
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u5, @pl_papas);
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u5, @pl_cerv);
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u5, @pl_limo);

-- Sabores de Mar
INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('valeria.nunez', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @user_u6 = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@user_u6, 'ROLE_RESPONSABLE', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Valeria', 'Núñez', 33777888, '1991-05-19', '2020-02-14', 100000, 0, NULL, @user_u6);
SET @resp_u6 = LAST_INSERT_ID();
INSERT INTO responsable (id) VALUES (@resp_u6);
INSERT INTO unidad_venta (nombre_comercial, responsable_id, superficie_m2, codigo_unico, festival_id)
VALUES ('Sabores de Mar', @resp_u6, 16, 'PD-EG-0001', @f_epicentro);
SET @u6 = LAST_INSERT_ID();
INSERT INTO puesto_desarmable (id, carpas_componen, tiempo_montaje) VALUES (@u6, 4, 60);
UPDATE personal SET unidad_venta_id = @u6 WHERE id = @resp_u6;
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u6, @pl_emp);
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u6, @pl_pizza);
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u6, @pl_cerv);
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u6, @pl_limo);

-- Pizza Móvil
INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('diego.suarez', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @user_u7 = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@user_u7, 'ROLE_RESPONSABLE', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Diego', 'Suárez', 28666777, '1980-10-27', '2019-08-05', 100000, 0, NULL, @user_u7);
SET @resp_u7 = LAST_INSERT_ID();
INSERT INTO responsable (id) VALUES (@resp_u7);
INSERT INTO unidad_venta (nombre_comercial, responsable_id, superficie_m2, codigo_unico, festival_id)
VALUES ('Pizza Móvil', @resp_u7, 14, 'FT-EG-0001', @f_epicentro);
SET @u7 = LAST_INSERT_ID();
INSERT INTO food_truck (id, patente, conexion_electrica) VALUES (@u7, 'AE012IJ', 1);
UPDATE personal SET unidad_venta_id = @u7 WHERE id = @resp_u7;
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u7, @pl_pizza);
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u7, @pl_gas);
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES (@u7, @pl_cerv);

-- ---------------------------------------------------------------------
-- Empleados: Cocinero (plus por categoría: Cocinero 150.000, Cocinero
-- ayudante 100.000, Lavaplatos 50.000) y Cajero (turno MANIANA / NOCHE).
-- Usuario = su email. Jorge Paz está dado de baja (eliminado = 1) para probar
-- la baja lógica y los filtros. Sueldo base 100.000.
-- ---------------------------------------------------------------------

INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('mario.lopez@epicentro.test', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @ue = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@ue, 'ROLE_EMPLEADO', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Mario', 'López', 34100001, '1992-02-14', '2022-03-01', 100000, 0, @u1, @ue);
SET @pe = LAST_INSERT_ID();
INSERT INTO cocinero (id, especialidad_culinaria, plus) VALUES (@pe, 'Cocinero', 150000);  -- categoría Cocinero, Parrillero

INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('ana.diaz@epicentro.test', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @ue = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@ue, 'ROLE_EMPLEADO', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Ana', 'Díaz', 36100002, '1997-06-21', '2025-01-10', 100000, 0, @u1, @ue);
SET @pe = LAST_INSERT_ID();
INSERT INTO cocinero (id, especialidad_culinaria, plus) VALUES (@pe, 'Cocinero ayudante', 100000);  -- categoría Cocinero ayudante, Ayudante de parrilla

INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('pablo.ruiz@epicentro.test', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @ue = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@ue, 'ROLE_EMPLEADO', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Pablo', 'Ruiz', 38100003, '2000-11-05', '2025-05-01', 100000, 0, @u1, @ue);
SET @pe = LAST_INSERT_ID();
INSERT INTO cocinero (id, especialidad_culinaria, plus) VALUES (@pe, 'Lavaplatos', 50000);  -- categoría Lavaplatos, Lavado de vajilla

INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('laura.vega@epicentro.test', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @ue = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@ue, 'ROLE_EMPLEADO', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Laura', 'Vega', 35100004, '1995-08-17', '2021-02-01', 100000, 0, @u1, @ue);
SET @pe = LAST_INSERT_ID();
INSERT INTO cajero (id, turno_trabajo) VALUES (@pe, 'MANIANA');

INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('jorge.paz@epicentro.test', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @ue = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@ue, 'ROLE_EMPLEADO', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Jorge', 'Paz', 33100005, '1991-03-09', '2023-04-01', 100000, 1, @u1, @ue);
SET @pe = LAST_INSERT_ID();
INSERT INTO cajero (id, turno_trabajo) VALUES (@pe, 'NOCHE');

INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('romina.castro@epicentro.test', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @ue = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@ue, 'ROLE_EMPLEADO', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Romina', 'Castro', 34200001, '1993-10-02', '2023-07-01', 100000, 0, @u2, @ue);
SET @pe = LAST_INSERT_ID();
INSERT INTO cocinero (id, especialidad_culinaria, plus) VALUES (@pe, 'Cocinero', 150000);  -- categoría Cocinero, Cocina fría

INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('tomas.ibarra@epicentro.test', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @ue = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@ue, 'ROLE_EMPLEADO', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Tomás', 'Ibarra', 37200002, '1999-01-30', '2024-09-01', 100000, 0, @u2, @ue);
SET @pe = LAST_INSERT_ID();
INSERT INTO cajero (id, turno_trabajo) VALUES (@pe, 'NOCHE');

INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('facundo.molina@epicentro.test', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @ue = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@ue, 'ROLE_EMPLEADO', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Facundo', 'Molina', 33300001, '1990-07-11', '2020-05-01', 100000, 0, @u3, @ue);
SET @pe = LAST_INSERT_ID();
INSERT INTO cocinero (id, especialidad_culinaria, plus) VALUES (@pe, 'Cocinero', 150000);  -- categoría Cocinero, Cocina mexicana

INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('camila.sosa@epicentro.test', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @ue = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@ue, 'ROLE_EMPLEADO', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Camila', 'Sosa', 38300002, '2001-04-23', '2025-02-01', 100000, 0, @u3, @ue);
SET @pe = LAST_INSERT_ID();
INSERT INTO cocinero (id, especialidad_culinaria, plus) VALUES (@pe, 'Lavaplatos', 50000);  -- categoría Lavaplatos, Lavado de vajilla

INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('nicolas.ortiz@epicentro.test', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @ue = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@ue, 'ROLE_EMPLEADO', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Nicolás', 'Ortiz', 35300003, '1996-12-12', '2022-08-01', 100000, 0, @u3, @ue);
SET @pe = LAST_INSERT_ID();
INSERT INTO cajero (id, turno_trabajo) VALUES (@pe, 'MANIANA');

INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('julieta.acosta@epicentro.test', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @ue = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@ue, 'ROLE_EMPLEADO', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Julieta', 'Acosta', 36400001, '1998-05-05', '2024-11-01', 100000, 0, @u4, @ue);
SET @pe = LAST_INSERT_ID();
INSERT INTO cocinero (id, especialidad_culinaria, plus) VALUES (@pe, 'Cocinero ayudante', 100000);  -- categoría Cocinero ayudante, Pastelería

INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('bruno.medina@epicentro.test', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @ue = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@ue, 'ROLE_EMPLEADO', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Bruno', 'Medina', 37400002, '1999-09-09', '2025-03-01', 100000, 0, @u4, @ue);
SET @pe = LAST_INSERT_ID();
INSERT INTO cajero (id, turno_trabajo) VALUES (@pe, 'MANIANA');

INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('agustin.rios@epicentro.test', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @ue = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@ue, 'ROLE_EMPLEADO', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Agustín', 'Ríos', 32500001, '1989-03-28', '2019-10-01', 100000, 0, @u5, @ue);
SET @pe = LAST_INSERT_ID();
INSERT INTO cocinero (id, especialidad_culinaria, plus) VALUES (@pe, 'Cocinero', 150000);  -- categoría Cocinero, Hamburguesería

INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('micaela.torres@epicentro.test', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @ue = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@ue, 'ROLE_EMPLEADO', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Micaela', 'Torres', 37500002, '1999-07-15', '2023-01-15', 100000, 0, @u5, @ue);
SET @pe = LAST_INSERT_ID();
INSERT INTO cajero (id, turno_trabajo) VALUES (@pe, 'NOCHE');

INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('gonzalo.benitez@epicentro.test', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @ue = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@ue, 'ROLE_EMPLEADO', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Gonzalo', 'Benítez', 31600001, '1987-11-20', '2018-06-01', 100000, 0, @u6, @ue);
SET @pe = LAST_INSERT_ID();
INSERT INTO cocinero (id, especialidad_culinaria, plus) VALUES (@pe, 'Cocinero', 150000);  -- categoría Cocinero, Cocina de mar

INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('florencia.luna@epicentro.test', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @ue = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@ue, 'ROLE_EMPLEADO', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Florencia', 'Luna', 38600002, '2000-02-02', '2024-01-10', 100000, 0, @u6, @ue);
SET @pe = LAST_INSERT_ID();
INSERT INTO cajero (id, turno_trabajo) VALUES (@pe, 'MANIANA');

INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('ignacio.cabrera@epicentro.test', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @ue = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@ue, 'ROLE_EMPLEADO', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Ignacio', 'Cabrera', 34700001, '1994-06-06', '2022-12-01', 100000, 0, @u7, @ue);
SET @pe = LAST_INSERT_ID();
INSERT INTO cocinero (id, especialidad_culinaria, plus) VALUES (@pe, 'Cocinero ayudante', 100000);  -- categoría Cocinero ayudante, Pizzero

INSERT INTO `user` (username, password, enabled, created_at, updated_at) VALUES ('daniela.romero@epicentro.test', '$2a$10$HK9UtztrMuGR8f2s.nPqPuTJs5VjkMSsJlW7hS4mKpwMWyvRzpg4q', 1, NOW(), NOW());
SET @ue = LAST_INSERT_ID();
INSERT INTO user_role (user_id, role, created_at, updated_at) VALUES (@ue, 'ROLE_EMPLEADO', NOW(), NOW());
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo_base, eliminado, unidad_venta_id, user_id)
VALUES ('Daniela', 'Romero', 36700002, '1997-09-25', '2021-05-01', 100000, 0, @u7, @ue);
SET @pe = LAST_INSERT_ID();
INSERT INTO cajero (id, turno_trabajo) VALUES (@pe, 'NOCHE');

-- ---------------------------------------------------------------------
-- Pedidos con sus items (la fecha de cada pedido cae dentro de las fechas
-- de su festival). Cada pedido solo incluye platos del menú de su unidad.
-- ---------------------------------------------------------------------

-- Pedidos de La Parrilla Rodante
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2026-11-20', @u1);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_chori, 3), (@ped, @pl_cerv, 2);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2026-11-20', @u1);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_hamb, 2), (@ped, @pl_papas, 2), (@ped, @pl_cerv, 2);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2026-11-21', @u1);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_chori, 4), (@ped, @pl_papas, 1);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2026-11-21', @u1);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_hamb, 5), (@ped, @pl_cerv, 5);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2026-11-22', @u1);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_chori, 2), (@ped, @pl_hamb, 1), (@ped, @pl_cerv, 3);

-- Pedidos de Cervecería del Sur
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2026-11-20', @u2);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_cerv, 6), (@ped, @pl_papas, 2);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2026-11-21', @u2);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_emp, 3), (@ped, @pl_cerv, 3), (@ped, @pl_limo, 1);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2026-11-21', @u2);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_cerv, 8);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2026-11-22', @u2);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_emp, 2), (@ped, @pl_limo, 2);

-- Pedidos de Tacos del Sol
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2027-02-13', @u3);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_tacos, 4), (@ped, @pl_limo, 4);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2027-02-13', @u3);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_tacos, 2), (@ped, @pl_cerv, 2), (@ped, @pl_emp, 1);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2027-02-14', @u3);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_tacos, 6), (@ped, @pl_cerv, 4);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2027-02-14', @u3);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_emp, 3), (@ped, @pl_limo, 3);

-- Pedidos de Dulces Sierras
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2027-02-13', @u4);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_brow, 3), (@ped, @pl_limo, 3);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2027-02-14', @u4);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_brow, 5), (@ped, @pl_gas, 2);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2027-02-14', @u4);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_brow, 2);

-- Pedidos de Burger Bros
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2027-03-12', @u5);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_hamb, 4), (@ped, @pl_papas, 4), (@ped, @pl_cerv, 4);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2027-03-13', @u5);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_hamb, 6), (@ped, @pl_cerv, 6);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2027-03-13', @u5);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_hamb, 2), (@ped, @pl_limo, 2);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2027-03-14', @u5);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_hamb, 5), (@ped, @pl_papas, 3), (@ped, @pl_cerv, 5);

-- Pedidos de Sabores de Mar
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2026-12-03', @u6);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_emp, 4), (@ped, @pl_limo, 2);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2026-12-10', @u6);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_pizza, 2), (@ped, @pl_cerv, 2);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2026-12-15', @u6);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_emp, 6), (@ped, @pl_pizza, 1), (@ped, @pl_cerv, 3);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2026-12-22', @u6);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_pizza, 3), (@ped, @pl_limo, 3);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2026-12-28', @u6);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_emp, 2), (@ped, @pl_cerv, 2);

-- Pedidos de Pizza Móvil
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2026-12-02', @u7);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_pizza, 5), (@ped, @pl_gas, 5);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2026-12-09', @u7);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_pizza, 3), (@ped, @pl_cerv, 3);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2026-12-16', @u7);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_pizza, 8), (@ped, @pl_gas, 4), (@ped, @pl_cerv, 4);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2026-12-24', @u7);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_pizza, 6), (@ped, @pl_cerv, 6);
INSERT INTO pedido (fecha_transaccion, unidad_venta_id) VALUES ('2026-12-30', @u7);
SET @ped = LAST_INSERT_ID();
INSERT INTO item_pedido (pedido_id, plato_id, cantidad) VALUES (@ped, @pl_pizza, 2), (@ped, @pl_gas, 2);

COMMIT;
