package com.unla.tpgrupaloo2hito2grupo01.services.implementation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.PedidoFormDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.PedidoResumenDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.UnidadVentaOpcionDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Administrador;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.ItemPedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Pedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Personal;
import com.unla.tpgrupaloo2hito2grupo01.entities.Plato;
import com.unla.tpgrupaloo2hito2grupo01.entities.UnidadVenta;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPedidoRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPlatoRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUnidadVentaRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.IPedidoService;

@Service("pedidoService")
public class PedidoService implements IPedidoService {

	private final IPedidoRepository pedidoRepository;
	private final IPlatoRepository platoRepository;
	private final IUnidadVentaRepository unidadVentaRepository;
	private final IPersonalRepository personalRepository;

	public PedidoService(IPedidoRepository pedidoRepository, IPlatoRepository platoRepository,
			IUnidadVentaRepository unidadVentaRepository, IPersonalRepository personalRepository) {
		this.pedidoRepository = pedidoRepository;
		this.platoRepository = platoRepository;
		this.unidadVentaRepository = unidadVentaRepository;
		this.personalRepository = personalRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Plato> getPlatos() {
		return platoRepository.findAllByOrderByNombreAsc();
	}

	@Override
	@Transactional(readOnly = true)
	public List<UnidadVentaOpcionDTO> getUnidadesHabilitadas(String username) {
		Optional<Personal> personal = personalRepository.findByUserUsernameAndEliminadoFalse(username);
		if (personal.isEmpty()) {
			return List.of();
		}
		List<UnidadVenta> unidades = personal.get() instanceof Administrador
				? unidadVentaRepository.findAllConFestival()
				: unidadAsignada(personal.get()).stream().toList();
		return unidades.stream().map(this::toOpcionDTO).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ErrorCampoDTO> validarReglasDeNegocio(PedidoFormDTO dto, String username) {
		return validar(dto, buscarUnidadHabilitada(dto.getUnidadVentaId(), username), buscarPlatos(dto));
	}

	@Override
	@Transactional
	public PedidoResumenDTO registrar(PedidoFormDTO dto, String username) {
		Optional<UnidadVenta> unidad = buscarUnidadHabilitada(dto.getUnidadVentaId(), username);
		Map<Long, Plato> platos = buscarPlatos(dto);
		List<ErrorCampoDTO> errores = validar(dto, unidad, platos);
		if (!errores.isEmpty()) {
			throw new IllegalArgumentException(errores.get(0).mensaje());
		}

		Pedido pedido = new Pedido();
		pedido.setFechaTransaccion(dto.getFechaTransaccion());
		pedido.setUnidadVenta(unidad.get());
		cantidadesPedidas(dto).forEach((platoId, cantidad) -> {
			ItemPedido item = new ItemPedido();
			item.setPedido(pedido);
			item.setPlato(platos.get(platoId));
			item.setCantidad(cantidad);
			pedido.getLstItems().add(item);
		});
		pedidoRepository.save(pedido);

		return new PedidoResumenDTO(pedido.getId(), unidad.get().getNombreComercial(),
				pedido.getLstItems().size(), pedido.getTotal());
	}

	private List<ErrorCampoDTO> validar(PedidoFormDTO dto, Optional<UnidadVenta> unidad, Map<Long, Plato> platos) {
		List<ErrorCampoDTO> errores = new ArrayList<>();
		if (dto.getUnidadVentaId() != null && unidad.isEmpty()) {
			errores.add(new ErrorCampoDTO("unidadVentaId", "noHabilitada",
					"No puede registrar pedidos para esa unidad de venta"));
		}
		Festival festival = unidad.map(UnidadVenta::getFestivalAsistio).orElse(null);
		if (festival != null && dto.getFechaTransaccion() != null && fueraDelFestival(dto.getFechaTransaccion(), festival)) {
			errores.add(new ErrorCampoDTO("fechaTransaccion", "fueraDeFestival",
					"La fecha debe estar entre el inicio y el fin de " + festival.getNombre()));
		}
		Map<Long, Integer> pedidas = cantidadesPedidas(dto);
		if (dto.getCantidades().values().stream().anyMatch(c -> c != null && c < 0)) {
			errores.add(new ErrorCampoDTO("cantidades", "negativa", "Las cantidades no pueden ser negativas"));
		} else if (pedidas.isEmpty()) {
			errores.add(new ErrorCampoDTO("cantidades", "vacio", "Indique la cantidad de al menos un plato"));
		} else if (!platos.keySet().containsAll(pedidas.keySet())) {
			errores.add(new ErrorCampoDTO("cantidades", "inexistente", "Alguno de los platos elegidos no existe"));
		}
		return errores;
	}

	private boolean fueraDelFestival(LocalDate fecha, Festival festival) {
		return fecha.isBefore(festival.getFechaInicio()) || fecha.isAfter(festival.getFechaFin());
	}

	// La unidad solo se devuelve si existe y el usuario puede operar en ella
	private Optional<UnidadVenta> buscarUnidadHabilitada(Long unidadVentaId, String username) {
		if (unidadVentaId == null) {
			return Optional.empty();
		}
		return personalRepository.findByUserUsernameAndEliminadoFalse(username)
				.filter(personal -> personal instanceof Administrador || esSuUnidad(personal, unidadVentaId))
				.flatMap(personal -> unidadVentaRepository.findByIdConFestival(unidadVentaId));
	}

	private boolean esSuUnidad(Personal personal, long unidadVentaId) {
		return personal.getUnidadVenta() != null && personal.getUnidadVenta().getId() == unidadVentaId;
	}

	private Optional<UnidadVenta> unidadAsignada(Personal personal) {
		return personal.getUnidadVenta() == null
				? Optional.empty()
				: unidadVentaRepository.findByIdConFestival(personal.getUnidadVenta().getId());
	}

	// Platos pedidos (cantidad mayor a 0) indexados por id; los ids inexistentes no aparecen
	private Map<Long, Plato> buscarPlatos(PedidoFormDTO dto) {
		return platoRepository.findAllById(cantidadesPedidas(dto).keySet()).stream()
				.collect(Collectors.toMap(Plato::getId, Function.identity()));
	}

	private Map<Long, Integer> cantidadesPedidas(PedidoFormDTO dto) {
		return dto.getCantidades().entrySet().stream()
				.filter(e -> e.getKey() != null && e.getValue() != null && e.getValue() > 0)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, TreeMap::new));
	}

	private UnidadVentaOpcionDTO toOpcionDTO(UnidadVenta unidad) {
		Festival festival = unidad.getFestivalAsistio();
		return new UnidadVentaOpcionDTO(unidad.getId(), unidad.getNombreComercial(), unidad.getCodigoUnico(),
				festival == null ? "-" : festival.getNombre(),
				festival == null ? null : festival.getFechaInicio(),
				festival == null ? null : festival.getFechaFin());
	}
}
