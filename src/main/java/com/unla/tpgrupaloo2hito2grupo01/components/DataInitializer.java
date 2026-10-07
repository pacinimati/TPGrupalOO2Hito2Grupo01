package com.unla.tpgrupaloo2hito2grupo01.components;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.unla.tpgrupaloo2hito2grupo01.entities.Administrador;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cajero;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cocinero;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.FoodTruck;
import com.unla.tpgrupaloo2hito2grupo01.entities.ItemPedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Pedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Personal;
import com.unla.tpgrupaloo2hito2grupo01.entities.Plato;
import com.unla.tpgrupaloo2hito2grupo01.entities.PuestoDesarmable;
import com.unla.tpgrupaloo2hito2grupo01.entities.Responsable;
import com.unla.tpgrupaloo2hito2grupo01.entities.TurnoTrabajo;
import com.unla.tpgrupaloo2hito2grupo01.entities.UnidadVenta;
import com.unla.tpgrupaloo2hito2grupo01.entities.User;
import com.unla.tpgrupaloo2hito2grupo01.entities.UserRole;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IFestivalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPedidoRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPlatoRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUnidadVentaRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUserRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUserRoleRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.implementation.UnidadVentaService;

// Precarga festivales, platos, el Administrador y los datos de prueba (unidades de venta con su
// responsable, empleados y pedidos) si todavía no existen. El detalle de lo cargado está en docs/datos-precargados.txt
@Component
public class DataInitializer implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

	private static final String ROLE_ADMIN = "ROLE_ADMIN";
	private static final String ROLE_RESPONSABLE = UnidadVentaService.ROLE_RESPONSABLE;
	private static final String ROLE_EMPLEADO = "ROLE_EMPLEADO";

	// Credenciales de los usuarios de prueba (el Administrador usa las del .env)
	private static final String PASSWORD_RESPONSABLE = "Resp1234";
	private static final String PASSWORD_EMPLEADO = "Emp1234";
	private static final String DOMINIO_EMPLEADOS = "@epicentro.test";

	// Plus fijo por categoría de cocina
	private static final float PLUS_COCINERO = 150000;
	private static final float PLUS_AYUDANTE = 100000;
	private static final float PLUS_LAVAPLATOS = 50000;

	private static final String FESTIVAL_INVIERNO = "Feria Gastronómica de Invierno";
	private static final String FESTIVAL_FOOD_WEEK = "Buenos Aires Food Week";
	private static final String FESTIVAL_PRIMAVERA = "Epicentro Primavera Gourmet";

	private final IFestivalRepository festivalRepository;
	private final IUserRepository userRepository;
	private final IUserRoleRepository userRoleRepository;
	private final IPersonalRepository personalRepository;
	private final IPlatoRepository platoRepository;
	private final IUnidadVentaRepository unidadVentaRepository;
	private final IPedidoRepository pedidoRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.admin.username}")
	private String adminUsername;

	@Value("${app.admin.password}")
	private String adminPassword;

	public DataInitializer(IFestivalRepository festivalRepository, IUserRepository userRepository,
			IUserRoleRepository userRoleRepository, IPersonalRepository personalRepository,
			IPlatoRepository platoRepository, IUnidadVentaRepository unidadVentaRepository,
			IPedidoRepository pedidoRepository, PasswordEncoder passwordEncoder) {
		this.festivalRepository = festivalRepository;
		this.userRepository = userRepository;
		this.userRoleRepository = userRoleRepository;
		this.personalRepository = personalRepository;
		this.platoRepository = platoRepository;
		this.unidadVentaRepository = unidadVentaRepository;
		this.pedidoRepository = pedidoRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(String... args) {
		cargarFestivales();
		cargarPlatos();
		cargarAdministrador();
		cargarUnidadesPersonalYPedidos();
	}

	private void cargarPlatos() {
		crearPlatoSiNoExiste("Hamburguesa completa", 8500, 3500);
		crearPlatoSiNoExiste("Pizza muzzarella", 9000, 3200);
		crearPlatoSiNoExiste("Tacos x3", 7000, 2800);
		crearPlatoSiNoExiste("Choripán", 5000, 1800);
		crearPlatoSiNoExiste("Empanadas x3", 4500, 1500);
		crearPlatoSiNoExiste("Papas fritas", 4000, 1200);
		crearPlatoSiNoExiste("Cerveza artesanal", 4000, 1500);
		crearPlatoSiNoExiste("Limonada", 2500, 600);
		crearPlatoSiNoExiste("Vacío a la parrilla", 12000, 6000);
		crearPlatoSiNoExiste("Provoleta", 6000, 2200);
		crearPlatoSiNoExiste("Churros x6", 3500, 1000);
		crearPlatoSiNoExiste("Brownie con helado", 4800, 1700);
		crearPlatoSiNoExiste("Gaseosa", 2000, 500);
	}

	private void crearPlatoSiNoExiste(String nombre, float precioVenta, float costoProduccion) {
		if (platoRepository.existsByNombre(nombre)) {
			return;
		}
		Plato plato = new Plato();
		plato.setNombre(nombre);
		plato.setPrecioVenta(precioVenta);
		plato.setCostoProduccion(costoProduccion);
		platoRepository.save(plato);
		logger.info("Plato precargado: {}", nombre);
	}

	private void cargarFestivales() {
		crearFestivalSiNoExiste("Lollapalooza", "Otoño", LocalDate.of(2027, 3, 12), LocalDate.of(2027, 3, 14));
		crearFestivalSiNoExiste("Cosquín Rock", "Verano", LocalDate.of(2027, 2, 13), LocalDate.of(2027, 2, 14));
		crearFestivalSiNoExiste("Quilmes Rock", "Primavera", LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 22));
		// Festivales con unidades y pedidos: duran 38, 20 y 52 días (distintos tramos de sueldo) y el último está en curso
		crearFestivalSiNoExiste(FESTIVAL_INVIERNO, "Invierno", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 8));
		crearFestivalSiNoExiste(FESTIVAL_FOOD_WEEK, "Invierno", LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 29));
		crearFestivalSiNoExiste(FESTIVAL_PRIMAVERA, "Primavera", LocalDate.of(2026, 9, 25), LocalDate.of(2026, 11, 15));
	}

	private void crearFestivalSiNoExiste(String nombre, String temporada, LocalDate inicio, LocalDate fin) {
		if (festivalRepository.existsByNombre(nombre)) {
			return;
		}
		festivalRepository.save(festival(nombre, temporada, inicio, fin));
		logger.info("Festival precargado: {}", nombre);
	}

	private Festival festival(String nombre, String temporada, LocalDate inicio, LocalDate fin) {
		Festival festival = new Festival();
		festival.setNombre(nombre);
		festival.setTemporada(temporada);
		festival.setFechaInicio(inicio);
		festival.setFechaFin(fin);
		festival.setCostoPorSuperficie(500);
		festival.setCostoPorMontaje(10);
		festival.setPlusUsoElectricidad(2000);
		festival.setSueldoBase(100000);
		return festival;
	}

	private void cargarAdministrador() {
		if (adminPassword == null || adminPassword.isBlank()) {
			logger.warn("ADMIN_PASSWORD no está definida: no se crea el usuario Administrador");
			return;
		}
		if (userRepository.existsByUsername(adminUsername)) {
			return;
		}
		User user = crearUsuario(adminUsername, adminPassword, ROLE_ADMIN, true);

		Administrador admin = new Administrador();
		admin.setNombre("Administrador");
		admin.setApellido("Sistema");
		admin.setFechaNacimiento(LocalDate.of(1990, 1, 1));
		admin.setFechaIngreso(LocalDate.now());
		admin.setUser(user);
		personalRepository.save(admin);
		logger.info("Usuario Administrador precargado: {}", adminUsername);
	}

	// Cada unidad se carga completa (responsable, menú, empleados y pedidos) solo si su código no existe
	private void cargarUnidadesPersonalYPedidos() {
		cargarUnidadesInvierno();
		cargarUnidadesFoodWeek();
		cargarUnidadesPrimavera();
	}

	private void cargarUnidadesInvierno() {
		if (!existe("CHO0000011")) {
			FoodTruck pepe = foodTruck("Choripán Don Pepe", "CHO0000011", 15, "AA111BB", false);
			altaUnidad(pepe, FESTIVAL_INVIERNO, "resp.pepe", persona("José", "Martínez", 24500111, fecha(1975, 4, 12), fecha(2022, 2, 1)),
					"Choripán", "Papas fritas", "Cerveza artesanal", "Gaseosa");
			cocinero(pepe, "hernan.duarte", persona("Hernán", "Duarte", 30101101, fecha(1986, 2, 11), fecha(2023, 2, 1)), "Parrillero", PLUS_COCINERO);
			cocinero(pepe, "silvana.correa", persona("Silvana", "Correa", 33101102, fecha(1989, 8, 3), fecha(2024, 1, 8)), "Lavado de vajilla", PLUS_LAVAPLATOS);
			cajero(pepe, "julieta.paz", persona("Julieta", "Paz", 38101103, fecha(1999, 7, 21), fecha(2025, 3, 3)), TurnoTrabajo.MANIANA, false);
			pedido(pepe, dia(6, 5), item("Choripán", 40), item("Cerveza artesanal", 25));
			pedido(pepe, dia(6, 6), item("Choripán", 55), item("Papas fritas", 20), item("Gaseosa", 15));
			pedido(pepe, dia(6, 12), item("Choripán", 30), item("Cerveza artesanal", 30));
			pedido(pepe, dia(6, 20), item("Choripán", 45), item("Papas fritas", 25));
			pedido(pepe, dia(6, 27), item("Choripán", 60), item("Cerveza artesanal", 35), item("Gaseosa", 20));
			pedido(pepe, dia(7, 3), item("Choripán", 35), item("Papas fritas", 15));
			pedido(pepe, dia(7, 7), item("Choripán", 50), item("Cerveza artesanal", 40), item("Papas fritas", 20));
		}
		if (!existe("EMP0000012")) {
			PuestoDesarmable criolla = puestoDesarmable("Empanadas La Criolla", "EMP0000012", 10, 2, 40);
			altaUnidad(criolla, FESTIVAL_INVIERNO, "resp.marta", persona("Marta", "Ríos", 27300222, fecha(1980, 9, 23), fecha(2023, 3, 1)),
					"Empanadas x3", "Limonada", "Gaseosa", "Churros x6");
			cocinero(criolla, "rosa.medina", persona("Rosa", "Medina", 29201201, fecha(1983, 5, 14), fecha(2021, 1, 11)), "Empanadas criollas", PLUS_COCINERO);
			cajero(criolla, "tomas.vega", persona("Tomás", "Vega", 40201202, fecha(2000, 10, 9), fecha(2024, 1, 15)), TurnoTrabajo.NOCHE, false);
			pedido(criolla, dia(6, 5), item("Empanadas x3", 12), item("Limonada", 8));
			pedido(criolla, dia(6, 13), item("Empanadas x3", 15), item("Churros x6", 6));
			pedido(criolla, dia(6, 21), item("Empanadas x3", 10), item("Gaseosa", 10));
			pedido(criolla, dia(7, 2), item("Empanadas x3", 18), item("Limonada", 5), item("Churros x6", 4));
		}
	}

	private void cargarUnidadesFoodWeek() {
		if (!existe("PIZ0000021")) {
			FoodTruck napoli = foodTruck("Pizza Napoli", "PIZ0000021", 25, "AD789FG", true);
			altaUnidad(napoli, FESTIVAL_FOOD_WEEK, "resp.napoli", persona("Gustavo", "Napoli", 26100333, fecha(1978, 1, 30), fecha(2021, 1, 15)),
					"Pizza muzzarella", "Provoleta", "Cerveza artesanal", "Gaseosa");
			cocinero(napoli, "giuseppe.rossi", persona("Giuseppe", "Rossi", 28301301, fecha(1981, 3, 25), fecha(2022, 2, 7)), "Pizzero", PLUS_COCINERO);
			cajero(napoli, "agustina.sosa", persona("Agustina", "Sosa", 39301302, fecha(1998, 12, 1), fecha(2025, 1, 13)), TurnoTrabajo.MANIANA, false);
			pedido(napoli, dia(8, 10), item("Pizza muzzarella", 50), item("Cerveza artesanal", 30));
			pedido(napoli, dia(8, 12), item("Pizza muzzarella", 45), item("Provoleta", 15), item("Gaseosa", 20));
			pedido(napoli, dia(8, 15), item("Pizza muzzarella", 60), item("Cerveza artesanal", 40));
			pedido(napoli, dia(8, 18), item("Pizza muzzarella", 40), item("Provoleta", 20));
			pedido(napoli, dia(8, 22), item("Pizza muzzarella", 70), item("Cerveza artesanal", 35), item("Gaseosa", 25));
			pedido(napoli, dia(8, 26), item("Pizza muzzarella", 55), item("Provoleta", 10), item("Cerveza artesanal", 30));
			pedido(napoli, dia(8, 29), item("Pizza muzzarella", 65), item("Gaseosa", 30));
		}
		if (!existe("TAC0000022")) {
			FoodTruck guero = foodTruck("Tacos El Güero", "TAC0000022", 18, "AC456DE", false);
			altaUnidad(guero, FESTIVAL_FOOD_WEEK, "resp.sofia", persona("Sofía", "Herrera", 33200444, fecha(1988, 6, 17), fecha(2024, 2, 10)),
					"Tacos x3", "Papas fritas", "Limonada", "Cerveza artesanal");
			cocinero(guero, "marcos.ledesma", persona("Marcos", "Ledesma", 32401401, fecha(1987, 9, 18), fecha(2023, 2, 20)), "Taquero", PLUS_COCINERO);
			cajero(guero, "brenda.quiroga", persona("Brenda", "Quiroga", 41401402, fecha(2001, 4, 6), fecha(2025, 2, 17)), TurnoTrabajo.NOCHE, false);
			pedido(guero, dia(8, 10), item("Tacos x3", 30), item("Limonada", 20));
			pedido(guero, dia(8, 14), item("Tacos x3", 25), item("Papas fritas", 15), item("Cerveza artesanal", 10));
			pedido(guero, dia(8, 19), item("Tacos x3", 40), item("Cerveza artesanal", 20));
			pedido(guero, dia(8, 23), item("Tacos x3", 35), item("Papas fritas", 20), item("Limonada", 15));
			pedido(guero, dia(8, 28), item("Tacos x3", 30), item("Cerveza artesanal", 25));
		}
		if (!existe("DUL0000023")) {
			PuestoDesarmable dulce = puestoDesarmable("Dulce Tentación", "DUL0000023", 12, 2, 30);
			altaUnidad(dulce, FESTIVAL_FOOD_WEEK, "resp.valentina", persona("Valentina", "Cruz", 35600555, fecha(1992, 11, 5), fecha(2024, 3, 5)),
					"Churros x6", "Brownie con helado", "Limonada", "Gaseosa");
			cocinero(dulce, "florencia.aguirre", persona("Florencia", "Aguirre", 34501501, fecha(1991, 6, 29), fecha(2024, 2, 5)), "Pastelería", PLUS_COCINERO);
			cajero(dulce, "ignacio.ferreyra", persona("Ignacio", "Ferreyra", 42501502, fecha(2002, 1, 19), fecha(2025, 3, 10)), TurnoTrabajo.MANIANA, false);
			pedido(dulce, dia(8, 11), item("Churros x6", 20), item("Limonada", 10));
			pedido(dulce, dia(8, 17), item("Churros x6", 25), item("Brownie con helado", 10), item("Gaseosa", 8));
			pedido(dulce, dia(8, 24), item("Churros x6", 30), item("Brownie con helado", 12));
		}
	}

	private void cargarUnidadesPrimavera() {
		if (!existe("PAR0000031")) {
			PuestoDesarmable parrilla = puestoDesarmable("Parrilla Don Raúl", "PAR0000031", 30, 4, 60);
			altaUnidad(parrilla, FESTIVAL_PRIMAVERA, "resp.raul", persona("Raúl", "Acosta", 22800666, fecha(1972, 7, 19), fecha(2020, 1, 20)),
					"Vacío a la parrilla", "Choripán", "Provoleta", "Cerveza artesanal", "Papas fritas");
			cocinero(parrilla, "alberto.sanchez", persona("Alberto", "Sánchez", 27601601, fecha(1979, 11, 7), fecha(2021, 2, 1)), "Asador", PLUS_COCINERO);
			cocinero(parrilla, "matias.villalba", persona("Matías", "Villalba", 36601602, fecha(1993, 3, 15), fecha(2024, 1, 22)), "Ayudante de asador", PLUS_AYUDANTE);
			cocinero(parrilla, "walter.ponce", persona("Walter", "Ponce", 31601603, fecha(1986, 8, 30), fecha(2025, 2, 10)), "Lavado de vajilla", PLUS_LAVAPLATOS);
			cajero(parrilla, "carolina.bravo", persona("Carolina", "Bravo", 37601604, fecha(1995, 5, 22), fecha(2023, 2, 6)), TurnoTrabajo.MANIANA, false);
			cajero(parrilla, "federico.luna", persona("Federico", "Luna", 35601605, fecha(1992, 9, 12), fecha(2022, 1, 17)), TurnoTrabajo.NOCHE, false);
			// Baja lógica: no debe aparecer en los listados ni en los filtros de empleados activos
			cajero(parrilla, "exequiel.paz", persona("Exequiel", "Paz", 39601606, fecha(1997, 2, 28), fecha(2023, 3, 6)), TurnoTrabajo.NOCHE, true);
			pedido(parrilla, dia(9, 25), item("Vacío a la parrilla", 40), item("Choripán", 30), item("Cerveza artesanal", 40));
			pedido(parrilla, dia(9, 26), item("Vacío a la parrilla", 55), item("Provoleta", 25), item("Cerveza artesanal", 50));
			pedido(parrilla, dia(9, 27), item("Vacío a la parrilla", 60), item("Choripán", 35), item("Papas fritas", 30));
			pedido(parrilla, dia(10, 1), item("Vacío a la parrilla", 45), item("Provoleta", 20), item("Cerveza artesanal", 45));
			pedido(parrilla, dia(10, 2), item("Vacío a la parrilla", 70), item("Choripán", 40), item("Cerveza artesanal", 60));
			pedido(parrilla, dia(10, 3), item("Vacío a la parrilla", 80), item("Provoleta", 30), item("Papas fritas", 40));
			pedido(parrilla, dia(10, 4), item("Vacío a la parrilla", 75), item("Choripán", 45), item("Cerveza artesanal", 55));
			pedido(parrilla, dia(10, 5), item("Vacío a la parrilla", 65), item("Provoleta", 25), item("Cerveza artesanal", 50));
		}
		if (!existe("BUR0000032")) {
			FoodTruck burger = foodTruck("Burger Bros", "BUR0000032", 20, "AE321HI", true);
			altaUnidad(burger, FESTIVAL_PRIMAVERA, "resp.nico", persona("Nicolás", "Benítez", 34100777, fecha(1990, 3, 8), fecha(2023, 2, 14)),
					"Hamburguesa completa", "Papas fritas", "Cerveza artesanal", "Gaseosa");
			cocinero(burger, "joaquin.molina", persona("Joaquín", "Molina", 33701701, fecha(1989, 10, 4), fecha(2022, 2, 21)), "Hamburguesero", PLUS_COCINERO);
			cocinero(burger, "pilar.dominguez", persona("Pilar", "Domínguez", 40701702, fecha(2000, 6, 16), fecha(2025, 1, 20)), "Armado de hamburguesas", PLUS_AYUDANTE);
			cajero(burger, "santiago.ibarra", persona("Santiago", "Ibarra", 38701703, fecha(1998, 1, 25), fecha(2024, 3, 4)), TurnoTrabajo.NOCHE, false);
			pedido(burger, dia(9, 25), item("Hamburguesa completa", 50), item("Papas fritas", 40));
			pedido(burger, dia(9, 26), item("Hamburguesa completa", 60), item("Cerveza artesanal", 30), item("Gaseosa", 25));
			pedido(burger, dia(9, 28), item("Hamburguesa completa", 45), item("Papas fritas", 35));
			pedido(burger, dia(10, 2), item("Hamburguesa completa", 70), item("Cerveza artesanal", 40), item("Gaseosa", 30));
			pedido(burger, dia(10, 3), item("Hamburguesa completa", 80), item("Papas fritas", 50));
			pedido(burger, dia(10, 5), item("Hamburguesa completa", 65), item("Cerveza artesanal", 35));
		}
		if (!existe("NON0000033")) {
			FoodTruck nonna = foodTruck("La Nonna Pizzería", "NON0000033", 22, "AF654JK", true);
			altaUnidad(nonna, FESTIVAL_PRIMAVERA, "resp.lucia", persona("Lucía", "Fontana", 31500888, fecha(1985, 12, 2), fecha(2022, 3, 1)),
					"Pizza muzzarella", "Provoleta", "Cerveza artesanal", "Gaseosa");
			cocinero(nonna, "antonella.russo", persona("Antonella", "Russo", 36801801, fecha(1994, 4, 9), fecha(2023, 1, 16)), "Pizzera", PLUS_COCINERO);
			cajero(nonna, "leandro.cabrera", persona("Leandro", "Cabrera", 41801802, fecha(2001, 11, 13), fecha(2025, 2, 24)), TurnoTrabajo.MANIANA, false);
			pedido(nonna, dia(9, 26), item("Pizza muzzarella", 40), item("Cerveza artesanal", 25));
			pedido(nonna, dia(9, 27), item("Pizza muzzarella", 55), item("Provoleta", 15));
			pedido(nonna, dia(10, 1), item("Pizza muzzarella", 50), item("Gaseosa", 30));
			pedido(nonna, dia(10, 3), item("Pizza muzzarella", 60), item("Cerveza artesanal", 30), item("Provoleta", 10));
			pedido(nonna, dia(10, 4), item("Pizza muzzarella", 45), item("Gaseosa", 20));
		}
		if (!existe("COM0000034")) {
			FoodTruck compadres = foodTruck("Tacos Los Compadres", "COM0000034", 16, "AG987LM", false);
			altaUnidad(compadres, FESTIVAL_PRIMAVERA, "resp.diego", persona("Diego", "Romero", 36200999, fecha(1994, 5, 27), fecha(2025, 1, 10)),
					"Tacos x3", "Papas fritas", "Limonada", "Cerveza artesanal");
			cocinero(compadres, "ramiro.gutierrez", persona("Ramiro", "Gutiérrez", 35901901, fecha(1992, 7, 2), fecha(2024, 2, 12)), "Taquero", PLUS_COCINERO);
			cajero(compadres, "micaela.salinas", persona("Micaela", "Salinas", 43901902, fecha(2003, 3, 31), fecha(2025, 3, 17)), TurnoTrabajo.NOCHE, false);
			pedido(compadres, dia(9, 26), item("Tacos x3", 20), item("Limonada", 10));
			pedido(compadres, dia(10, 2), item("Tacos x3", 25), item("Papas fritas", 12));
			pedido(compadres, dia(10, 4), item("Tacos x3", 18), item("Cerveza artesanal", 10));
		}
		if (!existe("CER0000035")) {
			PuestoDesarmable ruta9 = puestoDesarmable("Cervecería Ruta 9", "CER0000035", 22, 3, 45);
			altaUnidad(ruta9, FESTIVAL_PRIMAVERA, "resp.camila", persona("Camila", "Ortiz", 37001010, fecha(1996, 8, 14), fecha(2025, 2, 3)),
					"Cerveza artesanal", "Papas fritas", "Provoleta", "Empanadas x3", "Choripán");
			cocinero(ruta9, "esteban.peralta", persona("Esteban", "Peralta", 34002001, fecha(1991, 1, 20), fecha(2022, 2, 28)), "Maestro cervecero", PLUS_COCINERO);
			cajero(ruta9, "daniela.roldan", persona("Daniela", "Roldán", 40002002, fecha(2000, 9, 8), fecha(2024, 1, 29)), TurnoTrabajo.MANIANA, false);
			pedido(ruta9, dia(9, 25), item("Cerveza artesanal", 80), item("Papas fritas", 30));
			pedido(ruta9, dia(9, 27), item("Cerveza artesanal", 90), item("Empanadas x3", 20));
			pedido(ruta9, dia(10, 1), item("Cerveza artesanal", 70), item("Provoleta", 15));
			pedido(ruta9, dia(10, 3), item("Cerveza artesanal", 100), item("Papas fritas", 40), item("Choripán", 20));
			pedido(ruta9, dia(10, 5), item("Cerveza artesanal", 85), item("Empanadas x3", 25));
		}
	}

	private boolean existe(String codigoUnico) {
		return unidadVentaRepository.existsByCodigoUnico(codigoUnico);
	}

	private FoodTruck foodTruck(String nombre, String codigoUnico, float superficieM2, String patente, boolean conexionElectrica) {
		FoodTruck foodTruck = new FoodTruck();
		foodTruck.setNombreComercial(nombre);
		foodTruck.setCodigoUnico(codigoUnico);
		foodTruck.setSuperficieM2(superficieM2);
		foodTruck.setPatente(patente);
		foodTruck.setConexionElectrica(conexionElectrica);
		return foodTruck;
	}

	private PuestoDesarmable puestoDesarmable(String nombre, String codigoUnico, float superficieM2, int carpas, int minutosMontaje) {
		PuestoDesarmable puesto = new PuestoDesarmable();
		puesto.setNombreComercial(nombre);
		puesto.setCodigoUnico(codigoUnico);
		puesto.setSuperficieM2(superficieM2);
		puesto.setCarpasComponen(carpas);
		puesto.setTiempoMontaje(minutosMontaje);
		return puesto;
	}

	// Crea el responsable con su usuario, guarda la unidad con su festival y menú, y enlaza al responsable con la unidad
	private void altaUnidad(UnidadVenta unidad, String nombreFestival, String usuarioResponsable, Persona datos, String... platos) {
		Festival festival = festivalRepository.findByNombre(nombreFestival)
				.orElseThrow(() -> new IllegalStateException("Festival inexistente: " + nombreFestival));
		User user = crearUsuario(usuarioResponsable, PASSWORD_RESPONSABLE, ROLE_RESPONSABLE, true);
		Responsable responsable = new Responsable();
		completar(responsable, datos, user, festival);
		personalRepository.save(responsable);

		unidad.setFestivalAsistio(festival);
		unidad.setResponsable(responsable);
		for (String nombrePlato : platos) {
			unidad.getLstPlatos().add(buscarPlato(nombrePlato));
		}
		unidadVentaRepository.save(unidad);
		responsable.setUnidadVenta(unidad);
		personalRepository.save(responsable);
		logger.info("Unidad de venta precargada: {} ({})", unidad.getNombreComercial(), festival.getNombre());
	}

	// Cocinero, ayudante o lavaplatos según el plus por categoría
	private void cocinero(UnidadVenta unidad, String usuario, Persona datos, String tarea, float plus) {
		Cocinero cocinero = new Cocinero();
		cocinero.setEspecialidadCulinaria(tarea);
		cocinero.setPlus(plus);
		altaEmpleado(cocinero, unidad, usuario, datos, false);
	}

	private void cajero(UnidadVenta unidad, String usuario, Persona datos, TurnoTrabajo turno, boolean eliminado) {
		Cajero cajero = new Cajero();
		cajero.setTurnoTrabajo(turno);
		altaEmpleado(cajero, unidad, usuario, datos, eliminado);
	}

	// El usuario del empleado es su email; el eliminado lógicamente queda con el usuario deshabilitado
	private void altaEmpleado(Personal empleado, UnidadVenta unidad, String usuario, Persona datos, boolean eliminado) {
		User user = crearUsuario(usuario + DOMINIO_EMPLEADOS, PASSWORD_EMPLEADO, ROLE_EMPLEADO, !eliminado);
		completar(empleado, datos, user, unidad.getFestivalAsistio());
		empleado.setUnidadVenta(unidad);
		empleado.setEliminado(eliminado);
		personalRepository.save(empleado);
	}

	private void completar(Personal personal, Persona datos, User user, Festival festival) {
		personal.setNombre(datos.nombre());
		personal.setApellido(datos.apellido());
		personal.setDni(datos.dni());
		personal.setFechaNacimiento(datos.fechaNacimiento());
		personal.setFechaIngreso(datos.fechaIngreso());
		personal.setSueldoBase(festival.getSueldoBase());
		personal.setUser(user);
	}

	private User crearUsuario(String username, String password, String role, boolean habilitado) {
		User user = userRepository.save(new User(username, passwordEncoder.encode(password), habilitado));
		UserRole userRole = new UserRole();
		userRole.setUser(user);
		userRole.setRole(role);
		userRoleRepository.save(userRole);
		return user;
	}

	private void pedido(UnidadVenta unidad, LocalDate fecha, ItemSpec... items) {
		Pedido pedido = new Pedido();
		pedido.setFechaTransaccion(fecha);
		pedido.setUnidadVenta(unidad);
		for (ItemSpec spec : items) {
			ItemPedido item = new ItemPedido();
			item.setPedido(pedido);
			item.setPlato(buscarPlato(spec.plato()));
			item.setCantidad(spec.cantidad());
			pedido.getLstItems().add(item);
		}
		pedidoRepository.save(pedido);
	}

	private Plato buscarPlato(String nombre) {
		return platoRepository.findByNombre(nombre)
				.orElseThrow(() -> new IllegalStateException("Plato inexistente: " + nombre));
	}

	private Persona persona(String nombre, String apellido, int dni, LocalDate fechaNacimiento, LocalDate fechaIngreso) {
		return new Persona(nombre, apellido, dni, fechaNacimiento, fechaIngreso);
	}

	private ItemSpec item(String plato, int cantidad) {
		return new ItemSpec(plato, cantidad);
	}

	private LocalDate fecha(int anio, int mes, int dia) {
		return LocalDate.of(anio, mes, dia);
	}

	// Los pedidos precargados son todos de 2026
	private LocalDate dia(int mes, int dia) {
		return LocalDate.of(2026, mes, dia);
	}

	private record Persona(String nombre, String apellido, int dni, LocalDate fechaNacimiento, LocalDate fechaIngreso) {
	}

	private record ItemSpec(String plato, int cantidad) {
	}
}
