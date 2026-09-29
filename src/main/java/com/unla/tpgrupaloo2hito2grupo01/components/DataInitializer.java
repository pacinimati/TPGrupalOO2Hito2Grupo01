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
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.Plato;
import com.unla.tpgrupaloo2hito2grupo01.entities.User;
import com.unla.tpgrupaloo2hito2grupo01.entities.UserRole;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IFestivalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPlatoRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUserRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUserRoleRepository;

// Precarga los festivales, los platos y el usuario Administrador si todavía no existen
@Component
public class DataInitializer implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

	private static final String ROLE_ADMIN = "ROLE_ADMIN";

	private final IFestivalRepository festivalRepository;
	private final IUserRepository userRepository;
	private final IUserRoleRepository userRoleRepository;
	private final IPersonalRepository personalRepository;
	private final IPlatoRepository platoRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.admin.username}")
	private String adminUsername;

	@Value("${app.admin.password}")
	private String adminPassword;

	public DataInitializer(IFestivalRepository festivalRepository, IUserRepository userRepository,
			IUserRoleRepository userRoleRepository, IPersonalRepository personalRepository,
			IPlatoRepository platoRepository, PasswordEncoder passwordEncoder) {
		this.festivalRepository = festivalRepository;
		this.userRepository = userRepository;
		this.userRoleRepository = userRoleRepository;
		this.personalRepository = personalRepository;
		this.platoRepository = platoRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(String... args) {
		cargarFestivales();
		cargarPlatos();
		cargarAdministrador();
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
		User user = userRepository.save(new User(adminUsername, passwordEncoder.encode(adminPassword), true));
		UserRole userRole = new UserRole();
		userRole.setUser(user);
		userRole.setRole(ROLE_ADMIN);
		userRoleRepository.save(userRole);

		Administrador admin = new Administrador();
		admin.setNombre("Administrador");
		admin.setApellido("Sistema");
		admin.setFechaNacimiento(LocalDate.of(1990, 1, 1));
		admin.setFechaIngreso(LocalDate.now());
		admin.setUser(user);
		personalRepository.save(admin);
		logger.info("Usuario Administrador precargado: {}", adminUsername);
	}
}
