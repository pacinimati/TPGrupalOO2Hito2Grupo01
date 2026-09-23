package com.unla.tpgrupaloo2hito2grupo01.services.implementation;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.RegistroUnidadDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.UnidadVentaDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.FoodTruck;
import com.unla.tpgrupaloo2hito2grupo01.entities.PuestoDesarmable;
import com.unla.tpgrupaloo2hito2grupo01.entities.Responsable;
import com.unla.tpgrupaloo2hito2grupo01.entities.TipoUnidad;
import com.unla.tpgrupaloo2hito2grupo01.entities.UnidadVenta;
import com.unla.tpgrupaloo2hito2grupo01.entities.User;
import com.unla.tpgrupaloo2hito2grupo01.entities.UserRole;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IFestivalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUnidadVentaRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUserRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUserRoleRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.IUnidadVentaService;

@Service("unidadVentaService")
public class UnidadVentaService implements IUnidadVentaService {

	public static final String ROLE_RESPONSABLE = "ROLE_RESPONSABLE";

	private static final int EDAD_MINIMA = 18;

	private final IUnidadVentaRepository unidadVentaRepository;
	private final IPersonalRepository personalRepository;
	private final IFestivalRepository festivalRepository;
	private final IUserRepository userRepository;
	private final IUserRoleRepository userRoleRepository;
	private final PasswordEncoder passwordEncoder;

	public UnidadVentaService(IUnidadVentaRepository unidadVentaRepository, IPersonalRepository personalRepository,
			IFestivalRepository festivalRepository, IUserRepository userRepository,
			IUserRoleRepository userRoleRepository, PasswordEncoder passwordEncoder) {
		this.unidadVentaRepository = unidadVentaRepository;
		this.personalRepository = personalRepository;
		this.festivalRepository = festivalRepository;
		this.userRepository = userRepository;
		this.userRoleRepository = userRoleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ErrorCampoDTO> validarReglasDeNegocio(RegistroUnidadDTO dto) {
		List<ErrorCampoDTO> errores = new ArrayList<>();
		if (dto.getUsuario() != null && userRepository.existsByUsername(dto.getUsuario())) {
			errores.add(new ErrorCampoDTO("usuario", "duplicado", "Ese usuario ya está en uso"));
		}
		if (dto.getCodigoUnico() != null && unidadVentaRepository.existsByCodigoUnico(dto.getCodigoUnico())) {
			errores.add(new ErrorCampoDTO("codigoUnico", "duplicado", "Ya existe una unidad con ese código"));
		}
		if (dto.getFechaNacimiento() != null
				&& Period.between(dto.getFechaNacimiento(), LocalDate.now()).getYears() < EDAD_MINIMA) {
			errores.add(new ErrorCampoDTO("fechaNacimiento", "menor", "El responsable debe ser mayor de edad"));
		}
		if (dto.getTipo() == TipoUnidad.FOOD_TRUCK && (dto.getPatente() == null || dto.getPatente().isBlank())) {
			errores.add(new ErrorCampoDTO("patente", "requerido", "La patente es obligatoria para un Food Truck"));
		}
		if (dto.getTipo() == TipoUnidad.PUESTO_DESARMABLE) {
			if (dto.getCarpasComponen() <= 0) {
				errores.add(new ErrorCampoDTO("carpasComponen", "requerido", "Indique la cantidad de carpas"));
			}
			if (dto.getTiempoMontaje() <= 0) {
				errores.add(new ErrorCampoDTO("tiempoMontaje", "requerido", "Indique el tiempo de montaje en minutos"));
			}
		}
		return errores;
	}

	@Override
	@Transactional
	public void registrarConResponsable(RegistroUnidadDTO dto) {
		Festival festival = festivalRepository.findById(dto.getFestivalId())
				.orElseThrow(() -> new IllegalArgumentException("El festival seleccionado no existe"));

		User user = userRepository.save(new User(dto.getUsuario(), passwordEncoder.encode(dto.getContrasenia()), true));
		UserRole userRole = new UserRole();
		userRole.setUser(user);
		userRole.setRole(ROLE_RESPONSABLE);
		userRoleRepository.save(userRole);

		Responsable responsable = new Responsable();
		responsable.setNombre(dto.getNombre());
		responsable.setApellido(dto.getApellido());
		responsable.setDni(dto.getDni());
		responsable.setFechaNacimiento(dto.getFechaNacimiento());
		responsable.setFechaIngreso(LocalDate.now());
		responsable.setSueldoBase(festival.getSueldoBase());
		responsable.setUser(user);
		personalRepository.save(responsable);

		UnidadVenta unidad = crearUnidad(dto);
		unidad.setNombreComercial(dto.getNombreComercial());
		unidad.setSuperficieM2(dto.getSuperficieM2());
		unidad.setCodigoUnico(dto.getCodigoUnico());
		unidad.setFestivalAsistio(festival);
		unidad.setResponsable(responsable);
		unidadVentaRepository.save(unidad);

		responsable.setUnidadVenta(unidad);
		personalRepository.save(responsable);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UnidadVentaDTO> getByFestival(long festivalId) {
		return unidadVentaRepository.findByFestivalConResponsable(festivalId).stream()
				.map(this::toDTO)
				.toList();
	}

	private UnidadVentaDTO toDTO(UnidadVenta unidad) {
		UnidadVentaDTO dto = new UnidadVentaDTO();
		dto.setId(unidad.getId());
		dto.setNombreComercial(unidad.getNombreComercial());
		dto.setCodigoUnico(unidad.getCodigoUnico());
		dto.setTipo(unidad.getTipo());
		dto.setSuperficieM2(unidad.getSuperficieM2());
		Responsable responsable = unidad.getResponsable();
		dto.setResponsable(responsable == null ? "-" : responsable.getNombre() + " " + responsable.getApellido());
		switch (unidad.getTipo()) {
			case FOOD_TRUCK -> {
				FoodTruck foodTruck = (FoodTruck) unidad;
				dto.setPatente(foodTruck.getPatente());
				dto.setConexionElectrica(foodTruck.isConexionElectrica());
			}
			case PUESTO_DESARMABLE -> {
				PuestoDesarmable puesto = (PuestoDesarmable) unidad;
				dto.setCarpasComponen(puesto.getCarpasComponen());
				dto.setTiempoMontaje(puesto.getTiempoMontaje());
			}
		}
		return dto;
	}

	private UnidadVenta crearUnidad(RegistroUnidadDTO dto) {
		return switch (dto.getTipo()) {
			case FOOD_TRUCK -> {
				FoodTruck foodTruck = new FoodTruck();
				foodTruck.setPatente(dto.getPatente());
				foodTruck.setConexionElectrica(dto.isConexionElectrica());
				yield foodTruck;
			}
			case PUESTO_DESARMABLE -> {
				PuestoDesarmable puesto = new PuestoDesarmable();
				puesto.setCarpasComponen(dto.getCarpasComponen());
				puesto.setTiempoMontaje(dto.getTiempoMontaje());
				yield puesto;
			}
		};
	}
}
