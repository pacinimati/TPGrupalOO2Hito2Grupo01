package com.unla.tpgrupaloo2hito2grupo01.services.implementation;

import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.tpgrupaloo2hito2grupo01.dtos.RegistroUnidadDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.FoodTruck;
import com.unla.tpgrupaloo2hito2grupo01.entities.PuestoDesarmable;
import com.unla.tpgrupaloo2hito2grupo01.entities.Responsable;
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
	public boolean existeUsuario(String usuario) {
		return userRepository.existsByUsername(usuario);
	}

	@Override
	public boolean existeCodigoUnico(String codigoUnico) {
		return unidadVentaRepository.existsByCodigoUnico(codigoUnico);
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
