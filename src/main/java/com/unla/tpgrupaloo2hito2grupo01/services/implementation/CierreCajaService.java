package com.unla.tpgrupaloo2hito2grupo01.services.implementation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.tpgrupaloo2hito2grupo01.dtos.CierreCajaDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.CierreCajaFormDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.EstadoCajaDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cajero;
import com.unla.tpgrupaloo2hito2grupo01.entities.CierreCaja;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.ItemPedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Pedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Personal;
import com.unla.tpgrupaloo2hito2grupo01.entities.UnidadVenta;
import com.unla.tpgrupaloo2hito2grupo01.repositories.ICierreCajaRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPedidoRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.ICierreCajaService;

@Service("cierreCajaService")
public class CierreCajaService implements ICierreCajaService {

	private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2);

	private final ICierreCajaRepository cierreCajaRepository;
	private final IPedidoRepository pedidoRepository;
	private final IPersonalRepository personalRepository;

	public CierreCajaService(ICierreCajaRepository cierreCajaRepository, IPedidoRepository pedidoRepository,
			IPersonalRepository personalRepository) {
		this.cierreCajaRepository = cierreCajaRepository;
		this.pedidoRepository = pedidoRepository;
		this.personalRepository = personalRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public EstadoCajaDTO getEstadoActual(String username) {
		ContextoCaja contexto = obtenerContexto(username);
		CierreCaja cierre = contexto.unidad() == null
				? null
				: cierreCajaRepository.findByUnidadVentaIdAndFecha(contexto.unidad().getId(), contexto.fecha())
						.orElse(null);
		BigDecimal recaudacion = cierre == null
				? calcularRecaudacion(contexto.unidad(), contexto.fecha())
				: cierre.getRecaudacionPedidos();
		String mensaje = mensajeEstado(contexto, cierre);

		return new EstadoCajaDTO(contexto.fecha(), nombre(contexto.cajero()), nombre(contexto.unidad()),
				codigo(contexto.unidad()), nombre(contexto.festival()), contexto.festivalActivo(),
				recaudacion, cierre == null ? null : toDTO(cierre), mensaje);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ErrorCampoDTO> validarCierre(CierreCajaFormDTO form, String username) {
		ContextoCaja contexto = obtenerContexto(username);
		CierreCaja cierre = obtenerCierreActual(contexto);
		return validarCierre(form, contexto, cierre);
	}

	@Override
	@Transactional
	public CierreCajaDTO cerrarCaja(CierreCajaFormDTO form, String username) {
		ContextoCaja contexto = obtenerContexto(username);
		CierreCaja cierreExistente = obtenerCierreActual(contexto);
		List<ErrorCampoDTO> errores = validarCierre(form, contexto, cierreExistente);
		if (!errores.isEmpty()) {
			throw new IllegalArgumentException(errores.get(0).mensaje());
		}

		CierreCaja cierre = new CierreCaja();
		cierre.setFecha(contexto.fecha());
		cierre.setCajero(contexto.cajero());
		cierre.setUnidadVenta(contexto.unidad());
		cierre.setMontoDeclarado(form.getMontoDeclarado().setScale(2, RoundingMode.UNNECESSARY));
		cierre.setRecaudacionPedidos(calcularRecaudacion(contexto.unidad(), contexto.fecha()));
		return toDTO(cierreCajaRepository.save(cierre));
	}

	private List<ErrorCampoDTO> validarCierre(CierreCajaFormDTO form, ContextoCaja contexto, CierreCaja cierre) {
		List<ErrorCampoDTO> errores = new ArrayList<>();
		if (form == null || form.getMontoDeclarado() == null) {
			errores.add(new ErrorCampoDTO("montoDeclarado", "obligatorio",
					"Ingresá el monto de efectivo contado"));
			return errores;
		}
		if (form.getMontoDeclarado().signum() < 0) {
			errores.add(new ErrorCampoDTO("montoDeclarado", "negativo",
					"El monto contado no puede ser negativo"));
		}
		if (form.getMontoDeclarado().scale() > 2) {
			errores.add(new ErrorCampoDTO("montoDeclarado", "decimales",
					"El monto contado admite hasta dos decimales"));
		}

		if (cierre != null) {
			errores.add(new ErrorCampoDTO("montoDeclarado", "cajaCerrada",
					"La caja de hoy ya fue cerrada para esta unidad"));
		} else if (!contexto.festivalActivo()) {
			errores.add(new ErrorCampoDTO("montoDeclarado", "sinFestivalActivo",
					mensajeEstado(contexto, null)));
		}
		return errores;
	}

	private CierreCaja obtenerCierreActual(ContextoCaja contexto) {
		if (contexto.unidad() == null) {
			return null;
		}
		return cierreCajaRepository.findByUnidadVentaIdAndFecha(contexto.unidad().getId(), contexto.fecha())
				.orElse(null);
	}

	private ContextoCaja obtenerContexto(String username) {
		Personal personal = personalRepository.findByUserUsernameAndEliminadoFalse(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
		if (!(personal instanceof Cajero cajero)) {
			throw new AccessDeniedException("El usuario autenticado no es Cajero");
		}

		LocalDate hoy = LocalDate.now();
		UnidadVenta unidad = cajero.getUnidadVenta();
		Festival festival = unidad == null ? null : unidad.getFestivalAsistio();
		boolean festivalActivo = festival != null
				&& festival.getFechaInicio() != null
				&& festival.getFechaFin() != null
				&& !hoy.isBefore(festival.getFechaInicio())
				&& !hoy.isAfter(festival.getFechaFin());
		return new ContextoCaja(cajero, unidad, festival, hoy, festivalActivo);
	}

	private BigDecimal calcularRecaudacion(UnidadVenta unidad, LocalDate fecha) {
		if (unidad == null) {
			return CERO;
		}
		return pedidoRepository.findByUnidadVentaIdAndFechaConItems(unidad.getId(), fecha).stream()
				.flatMap(pedido -> pedido.getLstItems().stream())
				.map(this::subtotal)
				.reduce(CERO, BigDecimal::add)
				.setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal subtotal(ItemPedido item) {
		BigDecimal precio = new BigDecimal(Float.toString(item.getPlato().getPrecioVenta()));
		return precio.multiply(BigDecimal.valueOf(item.getCantidad()));
	}

	private String mensajeEstado(ContextoCaja contexto, CierreCaja cierre) {
		if (contexto.unidad() == null) {
			return "No tenés una unidad de venta asignada.";
		}
		if (cierre != null) {
			return "La caja de hoy ya fue cerrada para esta unidad.";
		}
		if (contexto.festival() == null) {
			return "Tu unidad no tiene un festival asociado.";
		}
		if (contexto.festival().getFechaInicio() == null || contexto.festival().getFechaFin() == null
				|| contexto.festival().getFechaFin().isBefore(contexto.festival().getFechaInicio())) {
			return "El festival asociado no tiene fechas válidas.";
		}
		if (!contexto.festivalActivo()) {
			return "Hoy no hay un festival activo para tu unidad.";
		}
		return "La caja está abierta. Podés cerrarla al finalizar el día.";
	}

	private String nombre(Personal personal) {
		return personal == null ? null : personal.getNombre() + " " + personal.getApellido();
	}

	private String nombre(UnidadVenta unidad) {
		return unidad == null ? null : unidad.getNombreComercial();
	}

	private String codigo(UnidadVenta unidad) {
		return unidad == null ? null : unidad.getCodigoUnico();
	}

	private String nombre(Festival festival) {
		return festival == null ? null : festival.getNombre();
	}

	private CierreCajaDTO toDTO(CierreCaja cierre) {
		BigDecimal diferencia = cierre.getMontoDeclarado().subtract(cierre.getRecaudacionPedidos())
				.setScale(2, RoundingMode.HALF_UP);
		return new CierreCajaDTO(cierre.getId(), cierre.getFecha(), nombre(cierre.getCajero()),
				nombre(cierre.getUnidadVenta()), codigo(cierre.getUnidadVenta()),
				cierre.getRecaudacionPedidos(), cierre.getMontoDeclarado(), diferencia, cierre.getFechaCierre());
	}

	private record ContextoCaja(Cajero cajero, UnidadVenta unidad, Festival festival, LocalDate fecha,
			boolean festivalActivo) {
	}
}
