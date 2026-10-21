package com.unla.tpgrupaloo2hito2grupo01.services.implementation;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.tpgrupaloo2hito2grupo01.dtos.PlatoDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.ItemPedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Pedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Personal;
import com.unla.tpgrupaloo2hito2grupo01.entities.Plato;
import com.unla.tpgrupaloo2hito2grupo01.entities.Responsable;
import com.unla.tpgrupaloo2hito2grupo01.entities.UnidadVenta;
import com.unla.tpgrupaloo2hito2grupo01.exceptions.UnidadNoPerteneceFestivalException;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IFestivalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPedidoRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUnidadVentaRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.IPlatoEstrellaService;

@Service("platoEstrellaService")
public class PlatoEstrellaService implements IPlatoEstrellaService {

	// Gana el plato que aparece en más pedidos; si empatan, el que vendió más unidades y, si siguen
	// empatados, el primero por nombre
	private static final Comparator<PlatoDTO> CRITERIO_ESTRELLA = Comparator
			.comparingLong(PlatoDTO::cantidadPedidos)
			.thenComparingLong(PlatoDTO::unidadesVendidas)
			.thenComparing(PlatoDTO::nombre, Comparator.reverseOrder());

	private final IFestivalRepository festivalRepository;
	private final IUnidadVentaRepository unidadVentaRepository;
	private final IPedidoRepository pedidoRepository;
	private final IPersonalRepository personalRepository;

	public PlatoEstrellaService(IFestivalRepository festivalRepository, IUnidadVentaRepository unidadVentaRepository,
			IPedidoRepository pedidoRepository, IPersonalRepository personalRepository) {
		this.festivalRepository = festivalRepository;
		this.unidadVentaRepository = unidadVentaRepository;
		this.pedidoRepository = pedidoRepository;
		this.personalRepository = personalRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<PlatoDTO> getPlatoEstrella(long festivalId, long unidadVentaId) {
		Festival festival = festivalRepository.findById(festivalId)
				.orElseThrow(() -> new IllegalArgumentException("El festival seleccionado no existe"));
		UnidadVenta unidad = unidadVentaRepository.findByIdConFestival(unidadVentaId)
				.orElseThrow(() -> new IllegalArgumentException("La unidad de venta seleccionada no existe"));

		Festival festivalUnidad = unidad.getFestivalAsistio();
		if (festivalUnidad == null || festivalUnidad.getId() != festival.getId()) {
			throw new UnidadNoPerteneceFestivalException(
					"La unidad de venta no pertenece al festival seleccionado");
		}

		List<Pedido> pedidos = pedidoRepository.findByUnidadVentaIdAndFestivalConItems(unidadVentaId, festivalId);
		return resumirPorPlato(pedidos).stream().max(CRITERIO_ESTRELLA);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<PlatoDTO> getPlatoEstrellaDeUnidadDelResponsable(String username) {
		Optional<UnidadVenta> unidad = unidadDelResponsable(username);
		if (unidad.isEmpty() || unidad.get().getFestivalAsistio() == null) {
			return Optional.empty();
		}
		return getPlatoEstrella(unidad.get().getFestivalAsistio().getId(), unidad.get().getId());
	}

	// Por cada plato pedido: en cuántos pedidos distintos aparece y cuántas unidades se vendieron
	private List<PlatoDTO> resumirPorPlato(List<Pedido> pedidos) {
		Map<Long, Plato> platos = new LinkedHashMap<>();
		Map<Long, Long> pedidosPorPlato = new LinkedHashMap<>();
		Map<Long, Long> unidadesPorPlato = new LinkedHashMap<>();

		for (Pedido pedido : pedidos) {
			// Un plato repetido en varios items del mismo pedido cuenta como un solo pedido
			Set<Long> platosDelPedido = new HashSet<>();
			for (ItemPedido item : pedido.getLstItems()) {
				Plato plato = item.getPlato();
				platos.putIfAbsent(plato.getId(), plato);
				unidadesPorPlato.merge(plato.getId(), (long) item.getCantidad(), Long::sum);
				if (platosDelPedido.add(plato.getId())) {
					pedidosPorPlato.merge(plato.getId(), 1L, Long::sum);
				}
			}
		}

		return platos.values().stream()
				.map(plato -> new PlatoDTO(plato.getId(), plato.getNombre(), plato.getPrecioVenta(),
						plato.getCostoProduccion(), pedidosPorPlato.get(plato.getId()),
						unidadesPorPlato.get(plato.getId())))
				.toList();
	}

	private Optional<UnidadVenta> unidadDelResponsable(String username) {
		Personal personal = personalRepository.findByUserUsernameAndEliminadoFalse(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
		if (!(personal instanceof Responsable responsable)) {
			throw new AccessDeniedException("El usuario autenticado no es Responsable");
		}
		return Optional.ofNullable(responsable.getUnidadVenta());
	}
}
