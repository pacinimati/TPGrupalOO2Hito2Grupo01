package com.unla.tpgrupaloo2hito2grupo01.services.implementation;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.tpgrupaloo2hito2grupo01.dtos.HistorialLaboralDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ReciboSueldoDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cajero;
import com.unla.tpgrupaloo2hito2grupo01.entities.CategoriaCocina;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cocinero;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.Personal;
import com.unla.tpgrupaloo2hito2grupo01.entities.TipoEmpleado;
import com.unla.tpgrupaloo2hito2grupo01.entities.UnidadVenta;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.ICalculoSalarialService;
import com.unla.tpgrupaloo2hito2grupo01.services.ICalculoSalarialService.Detalle;
import com.unla.tpgrupaloo2hito2grupo01.services.ICalculoSalarialService.Resultado;
import com.unla.tpgrupaloo2hito2grupo01.services.IHistorialLaboralService;

@Service("historialLaboralService")
public class HistorialLaboralService implements IHistorialLaboralService {

	private static final String MOTIVO_TIPO_NO_CONTEMPLADO = "No se puede calcular el sueldo para este tipo de empleado.";

	private static final String ESTADO_PROXIMO = "Próximo";
	private static final String ESTADO_EN_CURSO = "En curso";
	private static final String ESTADO_FINALIZADO = "Finalizado";

	private final IPersonalRepository personalRepository;
	private final ICalculoSalarialService calculoSalarialService;

	public HistorialLaboralService(IPersonalRepository personalRepository,
			ICalculoSalarialService calculoSalarialService) {
		this.personalRepository = personalRepository;
		this.calculoSalarialService = calculoSalarialService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<HistorialLaboralDTO> getHistorialLaboral(String username) {
		Personal empleado = empleadoDe(username);
		return unidadesTrabajadas(empleado).stream()
				.map(unidad -> toHistorialDTO(empleado, unidad))
				// El festival más reciente primero
				.sorted(Comparator.comparing(HistorialLaboralDTO::fechaInicio,
						Comparator.nullsLast(Comparator.reverseOrder())))
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ReciboSueldoDTO> getReciboSueldo(String username, long festivalId) {
		Personal empleado = empleadoDe(username);
		return unidadesTrabajadas(empleado).stream()
				.filter(unidad -> unidad.getFestivalAsistio().getId() == festivalId)
				.findFirst()
				.flatMap(unidad -> calculoSalarialService.calcularSueldo(empleado, unidad.getFestivalAsistio())
						.filter(Resultado::esCalculable)
						.map(resultado -> toReciboDTO(empleado, unidad, resultado)));
	}

	// Solo los empleados (cocina y cajeros) activos consultan su información laboral
	private Personal empleadoDe(String username) {
		Personal empleado = personalRepository.findByUserUsernameAndEliminadoFalse(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
		if (!(empleado instanceof Cocinero || empleado instanceof Cajero)) {
			throw new AccessDeniedException("El usuario autenticado no es un Empleado");
		}
		return empleado;
	}

	// Unidades de venta (con festival) en las que trabajó el empleado. En el modelo actual el empleado trabaja en
	// la unidad que tiene asignada (Personal.unidadVenta), que asiste a un único festival
	private List<UnidadVenta> unidadesTrabajadas(Personal empleado) {
		return Optional.ofNullable(empleado.getUnidadVenta())
				.filter(unidad -> unidad.getFestivalAsistio() != null)
				.stream()
				.toList();
	}

	private HistorialLaboralDTO toHistorialDTO(Personal empleado, UnidadVenta unidad) {
		Festival festival = unidad.getFestivalAsistio();
		Resultado resultado = calculoSalarialService.calcularSueldo(empleado, festival)
				.orElse(new Resultado(0.0, MOTIVO_TIPO_NO_CONTEMPLADO));
		return new HistorialLaboralDTO(festival.getId(), festival.getNombre(), festival.getTemporada(),
				festival.getFechaInicio(), festival.getFechaFin(), estado(festival), unidad.getNombreComercial(),
				unidad.getCodigoUnico(), unidad.getTipo().getDescripcion(), rol(empleado),
				resultado.esCalculable() ? resultado.sueldo() : null,
				resultado.esCalculable() ? null : resultado.error());
	}

	private ReciboSueldoDTO toReciboDTO(Personal empleado, UnidadVenta unidad, Resultado resultado) {
		Festival festival = unidad.getFestivalAsistio();
		Detalle detalle = resultado.detalle();
		return new ReciboSueldoDTO(empleado.getNombre() + " " + empleado.getApellido(), empleado.getDni(),
				empleado.getUser() == null ? "-" : empleado.getUser().getUsername(), rol(empleado),
				empleado.getFechaIngreso(), festival.getNombre(), festival.getTemporada(), festival.getFechaInicio(),
				festival.getFechaFin(), detalle.diasFestival(), unidad.getNombreComercial(), unidad.getCodigoUnico(),
				unidad.getTipo().getDescripcion(), detalle.sueldoBase(), detalle.conceptoAdicional(),
				detalle.adicional(), detalle.porcentaje(), resultado.sueldo(), LocalDate.now());
	}

	private String estado(Festival festival) {
		LocalDate hoy = LocalDate.now();
		if (festival.getFechaInicio() != null && hoy.isBefore(festival.getFechaInicio())) {
			return ESTADO_PROXIMO;
		}
		if (festival.getFechaFin() != null && hoy.isAfter(festival.getFechaFin())) {
			return ESTADO_FINALIZADO;
		}
		return ESTADO_EN_CURSO;
	}

	// Categoría de cocina o turno del cajero
	private String rol(Personal empleado) {
		if (empleado instanceof Cocinero cocinero) {
			return CategoriaCocina.desdePlus(cocinero.getPlus())
					.map(categoria -> TipoEmpleado.COCINERO.getEtiqueta() + " · " + categoria.getEtiqueta())
					.orElse(TipoEmpleado.COCINERO.getEtiqueta());
		}
		Cajero cajero = (Cajero) empleado;
		return cajero.getTurnoTrabajo() == null
				? TipoEmpleado.CAJERO.getEtiqueta()
				: TipoEmpleado.CAJERO.getEtiqueta() + " · Turno " + cajero.getTurnoTrabajo().getEtiqueta().toLowerCase();
	}
}
