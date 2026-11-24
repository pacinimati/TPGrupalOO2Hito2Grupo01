package com.unla.tpgrupaloo2hito2grupo01.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.unla.tpgrupaloo2hito2grupo01.dtos.CierreCajaDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.CierreCajaFiltroDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.CierreCajaRegistroDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cajero;
import com.unla.tpgrupaloo2hito2grupo01.entities.CierreCaja;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cocinero;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.FoodTruck;
import com.unla.tpgrupaloo2hito2grupo01.entities.ItemPedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Pedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Plato;
import com.unla.tpgrupaloo2hito2grupo01.entities.UnidadVenta;
import com.unla.tpgrupaloo2hito2grupo01.repositories.ICierreCajaRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPedidoRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.implementation.CierreCajaService;

// Cierre de caja desde la API REST (Hito 3): el cajero y la jornada vienen en el pedido
class CierreCajaApiServiceTest {

	private static final long CAJERO_ID = 7;
	private static final long UNIDAD_ID = 12;
	private static final LocalDate INICIO = LocalDate.now().minusDays(10);
	private static final LocalDate JORNADA = LocalDate.now().minusDays(5);

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
		festival.setNombre("Festival pasado");
		festival.setFechaInicio(INICIO);
		festival.setFechaFin(LocalDate.now().minusDays(1));
		unidad = new FoodTruck();
		unidad.setId(UNIDAD_ID);
		unidad.setNombreComercial("La Esquina");
		unidad.setCodigoUnico("FT12345678");
		unidad.setFestivalAsistio(festival);
		cajero = new Cajero();
		cajero.setId(CAJERO_ID);
		cajero.setNombre("Ana");
		cajero.setApellido("Paz");
		cajero.setUnidadVenta(unidad);
		when(personalRepository.findById(CAJERO_ID)).thenReturn(Optional.of(cajero));
		when(cierreCajaRepository.findByUnidadVentaIdAndFecha(any(Long.class), any())).thenReturn(Optional.empty());
		when(cierreCajaRepository.save(any(CierreCaja.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	void registraElCierreDeUnaJornadaPasadaDelFestival() {
		when(pedidoRepository.findByUnidadVentaIdAndFechaConItems(UNIDAD_ID, JORNADA))
				.thenReturn(List.of(pedido(1000f, 3)));

		CierreCajaDTO resultado = service.registrarCierre(registro(JORNADA, "2900.00"));

		assertEquals(JORNADA, resultado.fecha());
		assertEquals(CAJERO_ID, resultado.cajeroId());
		assertEquals(UNIDAD_ID, resultado.unidadVentaId());
		assertEquals("Festival pasado", resultado.nombreFestival());
		assertEquals(new BigDecimal("3000.00"), resultado.recaudacionPedidos());
		assertEquals(new BigDecimal("-100.00"), resultado.diferencia());
	}

	@Test
	void rechazaFechasFueraDelFestivalOFuturas() {
		List<ErrorCampoDTO> antes = service.validarRegistro(registro(INICIO.minusDays(1), "100.00"));
		List<ErrorCampoDTO> futura = service.validarRegistro(registro(LocalDate.now().plusDays(1), "100.00"));

		assertEquals("fueraDelFestival", antes.get(0).codigo());
		assertEquals("futura", futura.get(0).codigo());
		assertThrows(IllegalArgumentException.class,
				() -> service.registrarCierre(registro(INICIO.minusDays(1), "100.00")));
	}

	@Test
	void rechazaUnSegundoCierreDeLaMismaUnidadYJornada() {
		when(cierreCajaRepository.findByUnidadVentaIdAndFecha(UNIDAD_ID, JORNADA))
				.thenReturn(Optional.of(new CierreCaja()));

		List<ErrorCampoDTO> errores = service.validarRegistro(registro(JORNADA, "100.00"));

		assertEquals("cajaCerrada", errores.get(0).codigo());
	}

	@Test
	void soloAceptaCajerosActivos() {
		Cocinero cocinero = new Cocinero();
		when(personalRepository.findById(8L)).thenReturn(Optional.of(cocinero));
		cajero.setEliminado(true);

		assertFalse(service.existeCajero(8));
		assertFalse(service.existeCajero(CAJERO_ID));
		assertFalse(service.existeCajero(99));
	}

	@Test
	void filtraLosCierresSegunTenganONoDiferencia() {
		when(cierreCajaRepository.filtrar(null, UNIDAD_ID, null, null, null))
				.thenReturn(List.of(cierre("500.00", "500.00"), cierre("450.00", "500.00")));
		CierreCajaFiltroDTO filtro = new CierreCajaFiltroDTO();
		filtro.setUnidadVentaId(UNIDAD_ID);

		assertEquals(2, service.filtrar(filtro).size());
		filtro.setConDiferencia(true);
		assertEquals(new BigDecimal("-50.00"), service.filtrar(filtro).get(0).diferencia());
		filtro.setConDiferencia(false);
		assertEquals(1, service.filtrar(filtro).size());
	}

	@Test
	void rechazaUnRangoDeFechasInvertido() {
		CierreCajaFiltroDTO filtro = new CierreCajaFiltroDTO();
		filtro.setFechaDesde(JORNADA);
		filtro.setFechaHasta(INICIO);

		assertTrue(service.validarFiltro(filtro).stream().anyMatch(error -> error.codigo().equals("rangoInvalido")));
	}

	private CierreCaja cierre(String declarado, String recaudacion) {
		CierreCaja cierre = new CierreCaja();
		cierre.setFecha(JORNADA);
		cierre.setCajero(cajero);
		cierre.setUnidadVenta(unidad);
		cierre.setMontoDeclarado(new BigDecimal(declarado));
		cierre.setRecaudacionPedidos(new BigDecimal(recaudacion));
		return cierre;
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

	private CierreCajaRegistroDTO registro(LocalDate fecha, String monto) {
		CierreCajaRegistroDTO registro = new CierreCajaRegistroDTO();
		registro.setCajeroId(CAJERO_ID);
		registro.setFecha(fecha);
		registro.setMontoDeclarado(new BigDecimal(monto));
		return registro;
	}
}
