package com.unla.tpgrupaloo2hito2grupo01.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;

import com.unla.tpgrupaloo2hito2grupo01.dtos.CierreCajaDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.CierreCajaFormDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cajero;
import com.unla.tpgrupaloo2hito2grupo01.entities.CierreCaja;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.FoodTruck;
import com.unla.tpgrupaloo2hito2grupo01.entities.ItemPedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Pedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Personal;
import com.unla.tpgrupaloo2hito2grupo01.entities.Plato;
import com.unla.tpgrupaloo2hito2grupo01.entities.UnidadVenta;
import com.unla.tpgrupaloo2hito2grupo01.repositories.ICierreCajaRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPedidoRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.implementation.CierreCajaService;

class CierreCajaServiceTest {

	private static final String USERNAME = "cajero@epicentro.test";
	private static final long UNIDAD_ID = 12;

	private final ICierreCajaRepository cierreCajaRepository = mock(ICierreCajaRepository.class);
	private final IPedidoRepository pedidoRepository = mock(IPedidoRepository.class);
	private final IPersonalRepository personalRepository = mock(IPersonalRepository.class);
	private final ICierreCajaService service = new CierreCajaService(cierreCajaRepository, pedidoRepository,
			personalRepository);

	private Cajero cajero;
	private UnidadVenta unidad;

	@BeforeEach
	void setUp() {
		Festival festival = new Festival();
		festival.setNombre("Festival actual");
		festival.setFechaInicio(LocalDate.now());
		festival.setFechaFin(LocalDate.now());
		unidad = new FoodTruck();
		unidad.setId(UNIDAD_ID);
		unidad.setNombreComercial("La Esquina");
		unidad.setCodigoUnico("FT12345678");
		unidad.setFestivalAsistio(festival);
		cajero = new Cajero();
		cajero.setNombre("Ana");
		cajero.setApellido("Paz");
		cajero.setUnidadVenta(unidad);
		when(personalRepository.findByUserUsernameAndEliminadoFalse(USERNAME)).thenReturn(Optional.of(cajero));
		when(cierreCajaRepository.findByUnidadVentaIdAndFecha(UNIDAD_ID, LocalDate.now()))
				.thenReturn(Optional.empty());
		when(cierreCajaRepository.save(any(CierreCaja.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	void estadoActualSumaLosItemsDePedidosDeLaUnidadYFecha() {
		when(pedidoRepository.findByUnidadVentaIdAndFechaConItems(UNIDAD_ID, LocalDate.now()))
				.thenReturn(List.of(pedido(125.5f, 2), pedido(80f, 1)));

		var estado = service.getEstadoActual(USERNAME);

		assertTrue(estado.festivalActivo());
		assertEquals("La Esquina", estado.nombreUnidad());
		assertEquals(new BigDecimal("331.00"), estado.recaudacionPedidos());
		assertTrue(estado.mensaje().contains("cerrarla"));
	}

	@Test
	void elEstadoActualMuestraCeroCuandoNoHayPedidos() {
		when(pedidoRepository.findByUnidadVentaIdAndFechaConItems(UNIDAD_ID, LocalDate.now()))
				.thenReturn(List.of());

		var estado = service.getEstadoActual(USERNAME);

		assertEquals(new BigDecimal("0.00"), estado.recaudacionPedidos());
	}

	@Test
	void cerrarCajaGuardaElEfectivoLaRecaudacionYLaDiferencia() {
		when(pedidoRepository.findByUnidadVentaIdAndFechaConItems(UNIDAD_ID, LocalDate.now()))
				.thenReturn(List.of(pedido(125.5f, 2)));
		CierreCajaFormDTO form = form("260.00");

		CierreCajaDTO resultado = service.cerrarCaja(form, USERNAME);

		assertEquals(new BigDecimal("251.00"), resultado.recaudacionPedidos());
		assertEquals(new BigDecimal("260.00"), resultado.montoDeclarado());
		assertEquals(new BigDecimal("9.00"), resultado.diferencia());
		ArgumentCaptor<CierreCaja> captor = ArgumentCaptor.forClass(CierreCaja.class);
		verify(cierreCajaRepository).save(captor.capture());
		assertEquals(cajero, captor.getValue().getCajero());
		assertEquals(unidad, captor.getValue().getUnidadVenta());
		assertEquals(LocalDate.now(), captor.getValue().getFecha());
	}

	@Test
	void rechazaUnSegundoCierreDeLaMismaUnidadYFecha() {
		when(cierreCajaRepository.findByUnidadVentaIdAndFecha(UNIDAD_ID, LocalDate.now()))
				.thenReturn(Optional.of(new CierreCaja()));

		List<ErrorCampoDTO> errores = service.validarCierre(form("100.00"), USERNAME);

		assertFalse(errores.isEmpty());
		assertEquals("cajaCerrada", errores.get(0).codigo());
		assertThrows(IllegalArgumentException.class, () -> service.cerrarCaja(form("100.00"), USERNAME));
	}

	@Test
	void impideCerrarCuandoNoHayFestivalActivo() {
		Festival festival = unidad.getFestivalAsistio();
		festival.setFechaInicio(LocalDate.now().minusDays(2));
		festival.setFechaFin(LocalDate.now().minusDays(1));

		List<ErrorCampoDTO> errores = service.validarCierre(form("100.00"), USERNAME);

		assertEquals("sinFestivalActivo", errores.get(0).codigo());
		assertThrows(IllegalArgumentException.class, () -> service.cerrarCaja(form("100.00"), USERNAME));
	}

	@Test
	void rechazaEmpleadosQueNoSeanCajeros() {
		Personal cocinero = mock(Personal.class);
		when(personalRepository.findByUserUsernameAndEliminadoFalse(USERNAME)).thenReturn(Optional.of(cocinero));

		assertThrows(AccessDeniedException.class, () -> service.getEstadoActual(USERNAME));
	}

	@Test
	void validaMontosNegativosYConMasDeDosDecimales() {
		List<ErrorCampoDTO> negativos = service.validarCierre(form("-1.00"), USERNAME);
		List<ErrorCampoDTO> decimales = service.validarCierre(form("1.001"), USERNAME);

		assertTrue(negativos.stream().anyMatch(error -> error.codigo().equals("negativo")));
		assertTrue(decimales.stream().anyMatch(error -> error.codigo().equals("decimales")));
	}

	private Pedido pedido(float precio, int cantidad) {
		Plato plato = new Plato();
		plato.setPrecioVenta(precio);
		ItemPedido item = new ItemPedido();
		item.setPlato(plato);
		item.setCantidad(cantidad);
		Pedido pedido = new Pedido();
		pedido.setLstItems(List.of(item));
		return pedido;
	}

	private CierreCajaFormDTO form(String monto) {
		CierreCajaFormDTO form = new CierreCajaFormDTO();
		form.setMontoDeclarado(new BigDecimal(monto));
		return form;
	}
}
