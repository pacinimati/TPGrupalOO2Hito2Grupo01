package com.unla.tpgrupaloo2hito2grupo01.services.implementation;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.tpgrupaloo2hito2grupo01.dtos.EmpleadoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.EmpleadoFiltroDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.EmpleadoFormDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.UnidadVentaOpcionDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cajero;
import com.unla.tpgrupaloo2hito2grupo01.entities.CategoriaCocina;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cocinero;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.Personal;
import com.unla.tpgrupaloo2hito2grupo01.entities.Responsable;
import com.unla.tpgrupaloo2hito2grupo01.entities.TipoEmpleado;
import com.unla.tpgrupaloo2hito2grupo01.entities.UnidadVenta;
import com.unla.tpgrupaloo2hito2grupo01.entities.User;
import com.unla.tpgrupaloo2hito2grupo01.entities.UserRole;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUnidadVentaRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUserRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUserRoleRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.IEmpleadoService;
import com.unla.tpgrupaloo2hito2grupo01.services.IMailService;

@Service("empleadoService")
public class EmpleadoService implements IEmpleadoService {

	public static final String ROLE_EMPLEADO = "ROLE_EMPLEADO";

	private static final int LARGO_CONTRASENIA = 10;

	// Sin caracteres que se confunden al leerlos (0/O, 1/l/I)
	private static final String CARACTERES_CONTRASENIA = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

	private static final SecureRandom RANDOM = new SecureRandom();

	private final IPersonalRepository personalRepository;
	private final IUnidadVentaRepository unidadVentaRepository;
	private final IUserRepository userRepository;
	private final IUserRoleRepository userRoleRepository;
	private final IMailService mailService;
	private final PasswordEncoder passwordEncoder;

	public EmpleadoService(IPersonalRepository personalRepository, IUnidadVentaRepository unidadVentaRepository,
			IUserRepository userRepository, IUserRoleRepository userRoleRepository, IMailService mailService,
			PasswordEncoder passwordEncoder) {
		this.personalRepository = personalRepository;
		this.unidadVentaRepository = unidadVentaRepository;
		this.userRepository = userRepository;
		this.userRoleRepository = userRoleRepository;
		this.mailService = mailService;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UnidadVentaOpcionDTO> getUnidadDelResponsable(String username) {
		return unidadDe(username)
				.flatMap(unidad -> unidadVentaRepository.findByIdConFestival(unidad.getId()))
				.map(this::toOpcionDTO);
	}

	@Override
	@Transactional(readOnly = true)
	public List<EmpleadoDTO> filtrar(String username, EmpleadoFiltroDTO filtro) {
		Optional<UnidadVenta> unidad = unidadDe(username);
		if (unidad.isEmpty()) {
			return List.of();
		}
		return personalRepository.filtrarEmpleados(unidad.get().getId(), filtro.isEliminados(),
				textoOVacio(filtro.getNombre()), textoOVacio(filtro.getApellido()), filtro.getDni()).stream()
				.filter(empleado -> filtro.getTipo() == null || tipoDe(empleado) == filtro.getTipo())
				.map(this::toDTO)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<EmpleadoFormDTO> getParaModificar(String username, long id) {
		return buscarEmpleado(username, id).filter(empleado -> !empleado.isEliminado()).map(this::toFormDTO);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ErrorCampoDTO> validarReglasDeNegocio(EmpleadoFormDTO dto, String username) {
		List<ErrorCampoDTO> errores = new ArrayList<>();
		Optional<UnidadVenta> unidad = unidadDe(username);
		boolean esAlta = dto.getId() == null;
		Optional<Personal> existente = esAlta ? Optional.empty() : buscarEmpleado(username, dto.getId());
		// Al modificar, el tipo es el que ya tiene el empleado (no cambia)
		TipoEmpleado tipo = existente.map(this::tipoDe).orElse(dto.getTipo());

		if (dto.getDni() > 0) {
			validarDniSinDuplicar(dto.getDni(), existente, unidad, errores);
		}
		if (esAlta && dto.getEmail() != null && userRepository.existsByUsername(normalizarEmail(dto.getEmail()))) {
			errores.add(new ErrorCampoDTO("email", "duplicado", "Ese email ya está en uso"));
		}
		if (dto.getFechaNacimiento() != null) {
			if (edad(dto.getFechaNacimiento()) < Personal.EDAD_MINIMA) {
				errores.add(new ErrorCampoDTO("fechaNacimiento", "menor", "El empleado debe ser mayor de edad"));
			} else if (dto.getFechaIngreso() != null
					&& dto.getFechaIngreso().isBefore(dto.getFechaNacimiento().plusYears(Personal.EDAD_MINIMA))) {
				errores.add(new ErrorCampoDTO("fechaIngreso", "anteriorMayoriaEdad",
						"La fecha de ingreso es anterior a que el empleado cumpliera " + Personal.EDAD_MINIMA + " años"));
			}
		}
		if (tipo == TipoEmpleado.COCINERO && dto.getCategoriaCocina() == null) {
			errores.add(new ErrorCampoDTO("categoriaCocina", "requerido", "Seleccione la categoría de cocina"));
		}
		if (tipo == TipoEmpleado.CAJERO && dto.getTurnoTrabajo() == null) {
			errores.add(new ErrorCampoDTO("turnoTrabajo", "requerido", "Seleccione el turno de trabajo"));
		}
		return errores;
	}

	@Override
	@Transactional
	public void altaEmpleado(EmpleadoFormDTO dto, String username) {
		UnidadVenta unidad = unidadDe(username)
				.orElseThrow(() -> new IllegalStateException("El responsable no tiene una unidad de venta asignada"));
		String email = normalizarEmail(dto.getEmail());
		String contrasenia = generarContrasenia();

		User user = userRepository.save(new User(email, passwordEncoder.encode(contrasenia), true));
		UserRole userRole = new UserRole();
		userRole.setUser(user);
		userRole.setRole(ROLE_EMPLEADO);
		userRoleRepository.save(userRole);

		Personal empleado = dto.getTipo() == TipoEmpleado.COCINERO ? new Cocinero() : new Cajero();
		completarDatos(empleado, dto);
		Festival festival = unidad.getFestivalAsistio();
		empleado.setSueldoBase(festival.getSueldoBase());
		empleado.setUnidadVenta(unidad);
		empleado.setUser(user);
		personalRepository.save(empleado);

		// Si el mail falla se revierte todo: un empleado que no recibe su contraseña no puede ingresar
		mailService.enviarCredenciales(email, empleado.getNombre(), contrasenia);
	}

	@Override
	@Transactional
	public boolean modificarEmpleado(EmpleadoFormDTO dto, String username) {
		if (dto.getId() == null) {
			return false;
		}
		Optional<Personal> empleado = buscarEmpleado(username, dto.getId()).filter(e -> !e.isEliminado());
		empleado.ifPresent(e -> {
			completarDatos(e, dto);
			personalRepository.save(e);
		});
		return empleado.isPresent();
	}

	@Override
	@Transactional
	public boolean eliminarEmpleado(long id, String username) {
		Optional<Personal> empleado = buscarEmpleado(username, id).filter(e -> !e.isEliminado());
		empleado.ifPresent(e -> {
			e.setEliminado(true);
			// Sin acceso al sistema mientras esté dado de baja
			e.getUser().setEnabled(false);
			personalRepository.save(e);
		});
		return empleado.isPresent();
	}

	@Override
	@Transactional
	public boolean reactivarEmpleado(long id, String username) {
		Optional<Personal> empleado = buscarEmpleado(username, id).filter(Personal::isEliminado);
		if (empleado.isEmpty()) {
			return false;
		}
		Personal reactivado = empleado.get();
		String contrasenia = generarContrasenia();
		User user = reactivado.getUser();
		user.setEnabled(true);
		user.setPassword(passwordEncoder.encode(contrasenia));
		reactivado.setEliminado(false);
		personalRepository.save(reactivado);

		mailService.enviarCredenciales(user.getUsername(), reactivado.getNombre(), contrasenia);
		return true;
	}

	// Unidad del responsable que inició sesión; vacío si no es un responsable o todavía no tiene unidad
	private Optional<UnidadVenta> unidadDe(String username) {
		return personalRepository.findByUserUsernameAndEliminadoFalse(username)
				.filter(personal -> personal instanceof Responsable)
				.map(Personal::getUnidadVenta);
	}

	// Un responsable solo alcanza a los empleados de su unidad
	private Optional<Personal> buscarEmpleado(String username, long id) {
		return unidadDe(username).flatMap(unidad -> personalRepository.findEmpleadoDeUnidad(id, unidad.getId()));
	}

	// El dni no puede repetirse. Si lo tiene un empleado eliminado de la misma unidad se indica que corresponde
	// reactivarlo en lugar de dar de alta uno nuevo
	private void validarDniSinDuplicar(int dni, Optional<Personal> existente, Optional<UnidadVenta> unidad,
			List<ErrorCampoDTO> errores) {
		personalRepository.findByDni(dni).stream()
				.filter(otro -> existente.isEmpty() || otro.getId() != existente.get().getId())
				.findFirst()
				.ifPresent(otro -> {
					boolean eliminadoDeEstaUnidad = otro.isEliminado() && unidad.isPresent()
							&& otro.getUnidadVenta() != null && otro.getUnidadVenta().getId() == unidad.get().getId();
					errores.add(eliminadoDeEstaUnidad
							? new ErrorCampoDTO("dni", "eliminado",
									"Ya existe un empleado eliminado con ese DNI: reactivalo desde el listado de eliminados")
							: new ErrorCampoDTO("dni", "duplicado", "Ya existe una persona registrada con ese DNI"));
				});
	}

	private void completarDatos(Personal empleado, EmpleadoFormDTO dto) {
		empleado.setNombre(dto.getNombre().trim());
		empleado.setApellido(dto.getApellido().trim());
		empleado.setDni(dto.getDni());
		empleado.setFechaNacimiento(dto.getFechaNacimiento());
		empleado.setFechaIngreso(dto.getFechaIngreso());
		if (empleado instanceof Cocinero cocinero) {
			cocinero.setPlus(dto.getCategoriaCocina().getPlus());
			cocinero.setEspecialidadCulinaria(textoOVacio(dto.getEspecialidadCulinaria()));
		} else if (empleado instanceof Cajero cajero) {
			cajero.setTurnoTrabajo(dto.getTurnoTrabajo());
		}
	}

	private TipoEmpleado tipoDe(Personal empleado) {
		return empleado instanceof Cocinero ? TipoEmpleado.COCINERO : TipoEmpleado.CAJERO;
	}

	private int edad(LocalDate fechaNacimiento) {
		return Period.between(fechaNacimiento, LocalDate.now()).getYears();
	}

	private String normalizarEmail(String email) {
		return email.trim().toLowerCase();
	}

	private String textoOVacio(String texto) {
		return texto == null ? "" : texto.trim();
	}

	private String generarContrasenia() {
		StringBuilder contrasenia = new StringBuilder(LARGO_CONTRASENIA);
		for (int i = 0; i < LARGO_CONTRASENIA; i++) {
			contrasenia.append(CARACTERES_CONTRASENIA.charAt(RANDOM.nextInt(CARACTERES_CONTRASENIA.length())));
		}
		return contrasenia.toString();
	}

	private EmpleadoDTO toDTO(Personal empleado) {
		return new EmpleadoDTO(empleado.getId(), empleado.getNombre(), empleado.getApellido(), empleado.getDni(),
				empleado.getUser() == null ? "-" : empleado.getUser().getUsername(), tipoDe(empleado),
				detalle(empleado), empleado.getFechaIngreso());
	}

	// Categoría y tarea del personal de cocina, o turno del cajero
	private String detalle(Personal empleado) {
		if (empleado instanceof Cocinero cocinero) {
			String categoria = CategoriaCocina.desdePlus(cocinero.getPlus())
					.map(CategoriaCocina::getEtiqueta).orElse("Cocina");
			String tarea = cocinero.getEspecialidadCulinaria();
			return tarea == null || tarea.isBlank() ? categoria : categoria + " · " + tarea;
		}
		Cajero cajero = (Cajero) empleado;
		return cajero.getTurnoTrabajo() == null
				? "Cajero"
				: "Turno " + cajero.getTurnoTrabajo().getEtiqueta().toLowerCase();
	}

	private EmpleadoFormDTO toFormDTO(Personal empleado) {
		EmpleadoFormDTO dto = new EmpleadoFormDTO();
		dto.setId(empleado.getId());
		dto.setTipo(tipoDe(empleado));
		dto.setNombre(empleado.getNombre());
		dto.setApellido(empleado.getApellido());
		dto.setDni(empleado.getDni());
		dto.setFechaNacimiento(empleado.getFechaNacimiento());
		dto.setFechaIngreso(empleado.getFechaIngreso());
		dto.setEmail(empleado.getUser() == null ? null : empleado.getUser().getUsername());
		if (empleado instanceof Cocinero cocinero) {
			dto.setCategoriaCocina(CategoriaCocina.desdePlus(cocinero.getPlus()).orElse(null));
			dto.setEspecialidadCulinaria(cocinero.getEspecialidadCulinaria());
		} else if (empleado instanceof Cajero cajero) {
			dto.setTurnoTrabajo(cajero.getTurnoTrabajo());
		}
		return dto;
	}

	private UnidadVentaOpcionDTO toOpcionDTO(UnidadVenta unidad) {
		Festival festival = unidad.getFestivalAsistio();
		return new UnidadVentaOpcionDTO(unidad.getId(), unidad.getNombreComercial(), unidad.getCodigoUnico(),
				festival == null ? "-" : festival.getNombre(),
				festival == null ? null : festival.getFechaInicio(),
				festival == null ? null : festival.getFechaFin());
	}
}
