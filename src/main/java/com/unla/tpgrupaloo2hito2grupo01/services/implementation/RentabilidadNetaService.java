package com.unla.tpgrupaloo2hito2grupo01.services.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ReporteRentabilidadNetaDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Administrador;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cajero;
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
import com.unla.tpgrupaloo2hito2grupo01.services.ICalculoSalarialService;
import com.unla.tpgrupaloo2hito2grupo01.services.IRentabilidadNetaService;
import com.unla.tpgrupaloo2hito2grupo01.services.ICalculoSalarialService.Resultado;

@Service("rentabilidadNetaService")
public class RentabilidadNetaService implements IRentabilidadNetaService {

	private final IFestivalRepository festivalRepository;
	private final IUnidadVentaRepository unidadVentaRepository;
	private final IPedidoRepository pedidoRepository;
	private final IPersonalRepository personalRepository;
	private final ICalculoSalarialService calculoSalarialService;

	public RentabilidadNetaService(IFestivalRepository festivalRepository,
			IUnidadVentaRepository unidadVentaRepository, IPedidoRepository pedidoRepository,
			IPersonalRepository personalRepository, ICalculoSalarialService calculoSalarialService) {
		this.festivalRepository = festivalRepository;
		this.unidadVentaRepository = unidadVentaRepository;
		this.pedidoRepository = pedidoRepository;
		this.personalRepository = personalRepository;
		this.calculoSalarialService = calculoSalarialService;
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
		// Solo se informa un estado cuando hay sueldos pendientes que dejan el reporte incompleto
		String estado = completo ? null : String.join(" ", pendientesSalariales);

		return Optional.of(new ReporteRentabilidadNetaDTO(unidad.getId(), unidad.getNombreComercial(),
				festival.getNombre(), importePedidos, costoPlatos, costoSueldos, canon, rentabilidadNeta,
				completo, estado));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ReporteRentabilidadNetaDTO> getReporteDeUnidadDelResponsable(String username) {
		Personal personal = personalRepository.findByUserUsernameAndEliminadoFalse(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
		if (!(personal instanceof Responsable responsable)) {
			throw new AccessDeniedException("El usuario autenticado no es Responsable");
		}

		UnidadVenta unidad = responsable.getUnidadVenta();
		if (unidad == null || unidad.getFestivalAsistio() == null) {
			return Optional.empty();
		}
		return getReporte(unidad.getFestivalAsistio().getId(), unidad.getId());
	}

	private double costoProduccionItem(ItemPedido item) {
		return item.getCantidad() * (double) item.getPlato().getCostoProduccion();
	}

	private double calcularSueldosCocineros(List<Personal> personal, Festival festival, List<String> pendientes) {
		double sueldos = 0.0;

		for (Personal integrante : personal) {
			if (!(integrante instanceof Cocinero)) {
				continue;
			}
			ICalculoSalarialService.Resultado resultado = calculoSalarialService
					.calcularSueldo(integrante, festival).orElseThrow();
			if (resultado.esCalculable()) {
				sueldos += resultado.sueldo();
			} else {
				pendientes.add(resultado.error());
			}
		}

		return sueldos;
	}

	private double calcularSueldosCajeros(List<Personal> personal, Festival festival, List<String> pendientes) {
		double sueldos = 0.0;

		for (Personal integrante : personal) {
			if (!(integrante instanceof Cajero)) {
				continue;
			}
			Resultado resultado = calculoSalarialService.calcularSueldo(integrante, festival).orElseThrow();
			if (resultado.esCalculable()) {
				sueldos += resultado.sueldo();
			} else {
				pendientes.add(resultado.error());
			}
		}

		return sueldos;
	}

	// El Responsable está a cargo de la unidad y no cobra sueldo de ella: el enunciado solo define el sueldo de
	// cocina y cajeros (igual que en la liquidación de haberes), así que no deja el reporte incompleto
	private void identificarTratamientoSalarialPendiente(List<Personal> personal, List<String> pendientes) {
		boolean administradorPendiente = false;
		boolean otroTipoPendiente = false;

		for (Personal integrante : personal) {
			if (integrante instanceof Cocinero || integrante instanceof Cajero || integrante instanceof Responsable) {
				continue;
			} else if (integrante instanceof Administrador) {
				administradorPendiente = true;
			} else {
				otroTipoPendiente = true;
			}
		}

		if (administradorPendiente) {
			pendientes.add("No se puede determinar el tratamiento salarial del Administrador asociado a la unidad.");
		}
		if (otroTipoPendiente) {
			pendientes.add("No se puede determinar el tratamiento salarial de un subtipo de Personal no contemplado.");
		}
	}
}
