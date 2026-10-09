package com.unla.tpgrupaloo2hito2grupo01.services.implementation;

import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ReporteRentabilidadNetaDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Administrador;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cajero;
import com.unla.tpgrupaloo2hito2grupo01.entities.CategoriaCocina;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cocinero;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.ItemPedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Pedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Personal;
import com.unla.tpgrupaloo2hito2grupo01.entities.Responsable;
import com.unla.tpgrupaloo2hito2grupo01.entities.UnidadVenta;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IFestivalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPedidoRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUnidadVentaRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.IRentabilidadNetaService;

@Service("rentabilidadNetaService")
public class RentabilidadNetaService implements IRentabilidadNetaService {

	private final IFestivalRepository festivalRepository;
	private final IUnidadVentaRepository unidadVentaRepository;
	private final IPedidoRepository pedidoRepository;
	private final IPersonalRepository personalRepository;

	public RentabilidadNetaService(IFestivalRepository festivalRepository,
			IUnidadVentaRepository unidadVentaRepository, IPedidoRepository pedidoRepository,
			IPersonalRepository personalRepository) {
		this.festivalRepository = festivalRepository;
		this.unidadVentaRepository = unidadVentaRepository;
		this.pedidoRepository = pedidoRepository;
		this.personalRepository = personalRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ReporteRentabilidadNetaDTO> getReporte(long festivalId, long unidadVentaId) {
		Festival festival = festivalRepository.findById(festivalId)
				.orElseThrow(() -> new IllegalArgumentException("El festival seleccionado no existe"));

		Optional<UnidadVenta> unidadEncontrada = unidadVentaRepository.findByIdConFestival(unidadVentaId);
		if (unidadEncontrada.isEmpty()) {
			return Optional.empty();
		}

		UnidadVenta unidad = unidadEncontrada.get();
		Festival festivalUnidad = unidad.getFestivalAsistio();
		if (festivalUnidad == null || festivalUnidad.getId() != festivalId) {
			throw new IllegalArgumentException("La unidad de venta no pertenece al festival seleccionado");
		}

		List<Pedido> pedidos = pedidoRepository.findByUnidadVentaIdAndFestivalConItems(unidadVentaId, festivalId);
		double importePedidos = pedidos.stream().mapToDouble(Pedido::getTotal).sum();
		double costoPlatos = pedidos.stream()
				.flatMap(pedido -> pedido.getLstItems().stream())
				.mapToDouble(this::costoProduccionItem)
				.sum();
		double canon = unidad.calcularCanon(festival);

		List<String> pendientesSalariales = new ArrayList<>();
		List<Personal> personalActivo = personalRepository
				.findByUnidadVentaIdAndEliminadoFalse(unidadVentaId);
		double sueldosCalculables = calcularSueldosCocineros(personalActivo, festival, pendientesSalariales)
				+ calcularSueldosCajeros(personalActivo, festival, pendientesSalariales);
		identificarTratamientoSalarialPendiente(personalActivo, pendientesSalariales);

		boolean completo = pendientesSalariales.isEmpty();
		Double costoSueldos = completo ? sueldosCalculables : null;
		Double rentabilidadNeta = completo
				? importePedidos - costoPlatos - costoSueldos - canon
				: null;
		String estado = completo
				? "Cálculo completo: no hay costos salariales pendientes de determinar."
				: String.join(" ", pendientesSalariales);

		return Optional.of(new ReporteRentabilidadNetaDTO(unidad.getId(), unidad.getNombreComercial(),
				festival.getNombre(), importePedidos, costoPlatos, costoSueldos, canon, rentabilidadNeta,
				completo, estado));
	}

	private double costoProduccionItem(ItemPedido item) {
		return item.getCantidad() * (double) item.getPlato().getCostoProduccion();
	}

	private double calcularSueldosCocineros(List<Personal> personal, Festival festival, List<String> pendientes) {
		OptionalLong diasDuracion = diasDuracionFestival(festival);
		double sueldos = 0.0;

		for (Personal integrante : personal) {
			if (!(integrante instanceof Cocinero cocinero)) {
				continue;
			}

			if (diasDuracion.isEmpty()) {
				pendientes.add("No se puede calcular el sueldo de cocina: el festival no tiene fechas válidas.");
				continue;
			}

			Optional<CategoriaCocina> categoria = CategoriaCocina.desdePlus(cocinero.getPlus());
			if (categoria.isEmpty()) {
				pendientes.add("No se puede calcular el sueldo de cocina: el plus no corresponde a una categoría conocida.");
				continue;
			}

			double factor = (diasDuracion.getAsLong() / ConstantesSalariales.DIAS_POR_TRAMO + 1)
					* ConstantesSalariales.FACTOR_SUELDO;
			sueldos += factor * (ConstantesSalariales.SUELDO_BASE + categoria.get().getPlus());
		}

		return sueldos;
	}

	private double calcularSueldosCajeros(List<Personal> personal, Festival festival, List<String> pendientes) {
		OptionalLong diasDuracion = diasDuracionFestival(festival);
		double sueldos = 0.0;

		for (Personal integrante : personal) {
			if (!(integrante instanceof Cajero cajero)) {
				continue;
			}

			if (diasDuracion.isEmpty()) {
				pendientes.add("No se puede calcular el sueldo del Cajero: el festival no tiene fechas válidas.");
				continue;
			}

			if (cajero.getFechaIngreso() == null) {
				pendientes.add("No se puede calcular el sueldo del Cajero: falta la fecha de ingreso para determinar la antigüedad.");
				continue;
			}

			if (cajero.getFechaIngreso().isAfter(festival.getFechaFin())) {
				pendientes.add("No se puede calcular el sueldo del Cajero: la fecha de ingreso es posterior al fin del festival.");
				continue;
			}

			int aniosAntiguedad = Period.between(cajero.getFechaIngreso(), festival.getFechaFin()).getYears();
			double factor = (diasDuracion.getAsLong() / ConstantesSalariales.DIAS_POR_TRAMO + 1)
					* ConstantesSalariales.FACTOR_SUELDO;
			double sueldoBaseMasAntiguedad = ConstantesSalariales.SUELDO_BASE
					+ aniosAntiguedad * ConstantesSalariales.ANTIGUEDAD_POR_ANIO;
			sueldos += factor * sueldoBaseMasAntiguedad;
		}

		return sueldos;
	}

	// Se cuentan ambos días de calendario que delimitan el festival.
	private OptionalLong diasDuracionFestival(Festival festival) {
		if (festival.getFechaInicio() == null || festival.getFechaFin() == null
				|| festival.getFechaFin().isBefore(festival.getFechaInicio())) {
			return OptionalLong.empty();
		}
		return OptionalLong.of(ChronoUnit.DAYS.between(festival.getFechaInicio(), festival.getFechaFin()) + 1);
	}

	private void identificarTratamientoSalarialPendiente(List<Personal> personal, List<String> pendientes) {
		boolean responsablePendiente = false;
		boolean administradorPendiente = false;
		boolean otroTipoPendiente = false;

		for (Personal integrante : personal) {
			if (integrante instanceof Cocinero) {
				continue;
			} else if (integrante instanceof Responsable) {
				responsablePendiente = true;
			} else if (integrante instanceof Cajero) {
				continue;
			} else if (integrante instanceof Administrador) {
				administradorPendiente = true;
			} else {
				otroTipoPendiente = true;
			}
		}

		if (responsablePendiente) {
			pendientes.add("No se puede completar la rentabilidad: el enunciado no define el sueldo del Responsable.");
		}
		if (administradorPendiente) {
			pendientes.add("No se puede determinar el tratamiento salarial del Administrador asociado a la unidad.");
		}
		if (otroTipoPendiente) {
			pendientes.add("No se puede determinar el tratamiento salarial de un subtipo de Personal no contemplado.");
		}
	}
}
