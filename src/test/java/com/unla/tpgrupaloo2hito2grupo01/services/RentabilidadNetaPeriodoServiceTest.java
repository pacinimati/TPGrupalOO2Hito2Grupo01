package com.unla.tpgrupaloo2hito2grupo01.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ReporteRentabilidadNetaPeriodoDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cajero;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cocinero;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.FoodTruck;
import com.unla.tpgrupaloo2hito2grupo01.entities.ItemPedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Pedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Plato;
import com.unla.tpgrupaloo2hito2grupo01.entities.Responsable;
import com.unla.tpgrupaloo2hito2grupo01.exceptions.UnidadNoPerteneceFestivalException;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IFestivalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPedidoRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUnidadVentaRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.implementation.CalculoSalarialService;
import com.unla.tpgrupaloo2hito2grupo01.services.implementation.RentabilidadNetaService;

class RentabilidadNetaPeriodoServiceTest {

	private static final long FESTIVAL_ID = 1;
	private static final long UNIDAD_ID = 2;
	private static final LocalDate INICIO_FESTIVAL = LocalDate.of(2026, 1, 1);
	private static final LocalDate FIN_FESTIVAL = LocalDate.of(2026, 1, 30);

	private final IFestivalRepository festivalRepository = mock(IFestivalRepository.class);
	private final IUnidadVentaRepository unidadVentaRepository = mock(IUnidadVentaRepository.class);
	private final IPedidoRepository pedidoRepository = mock(IPedidoRepository.class);
	private final IPersonalRepository personalRepository = mock(IPersonalRepository.class);
	private final RentabilidadNetaService service = new RentabilidadNetaService(festivalRepository,
			unidadVentaRepository, pedidoRepository, personalRepository, new CalculoSalarialService());

	@Test
	void validaFechasNulasComoErroresDeCampo() {
		List<ErrorCampoDTO> errores = service.validarPeriodo(festivalValido(), null, null);

		assertEquals(List.of("desde", "hasta"), errores.stream().map(ErrorCampoDTO::campo).toList());
		assertEquals(List.of("obligatoria", "obligatoria"),
				errores.stream().map(ErrorCampoDTO::codigo).toList());
	}

	@Test
	void validaPeriodoInvertidoComoErrorEnHasta() {
		List<ErrorCampoDTO> errores = service.validarPeriodo(festivalValido(),
				LocalDate.of(2026, 1, 20), LocalDate.of(2026, 1, 10));

		assertEquals(1, errores.size());
		assertEquals("hasta", errores.get(0).campo());
		assertEquals("periodoInvertido", errores.get(0).codigo());
	}

	@Test
	void validaComoFueraDelFestivalLasFechasQueExcedenSusLimites() {
		List<ErrorCampoDTO> errores = service.validarPeriodo(festivalValido(),
				LocalDate.of(2025, 12, 31), LocalDate.of(2026, 1, 31));

		assertEquals(List.of("desde", "hasta"), errores.stream().map(ErrorCampoDTO::campo).toList());
		assertTrue(errores.stream().allMatch(error -> error.codigo().equals("fueraDelFestival")));
	}

	@Test
	void validaFechasNulasOInvertidasDelFestival() {
		Festival sinInicio = festivalValido();
		sinInicio.setFechaInicio(null);
		Festival fechasInvertidas = festivalValido();
		fechasInvertidas.setFechaInicio(FIN_FESTIVAL);
		fechasInvertidas.setFechaFin(INICIO_FESTIVAL);

		List<ErrorCampoDTO> errorSinInicio = service.validarPeriodo(sinInicio,
				LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 20));
		List<ErrorCampoDTO> errorFechasInvertidas = service.validarPeriodo(fechasInvertidas,
				LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 20));
		List<ErrorCampoDTO> errorSinFestival = service.validarPeriodo(null,
				LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 20));

		assertEquals("fechasFestivalInvalidas", errorSinInicio.get(0).codigo());
		assertEquals("fechasFestivalInvalidas", errorFechasInvertidas.get(0).codigo());
		assertEquals("fechasFestivalInvalidas", errorSinFestival.get(0).codigo());
	}

	@Test
	void rechazaPeriodoInvalidoAntesDeConsultarUnidadPedidosOPersonal() {
		Festival festival = festivalValido();

		assertThrows(IllegalArgumentException.class,
				() -> service.getReportePeriodo(festival, UNIDAD_ID, null, FIN_FESTIVAL));
		assertThrows(IllegalArgumentException.class,
				() -> service.getReportePeriodo(festival, UNIDAD_ID,
						LocalDate.of(2026, 1, 20), LocalDate.of(2026, 1, 10)));
		assertThrows(IllegalArgumentException.class,
				() -> service.getReportePeriodo(festival, UNIDAD_ID,
						LocalDate.of(2025, 12, 31), LocalDate.of(2026, 1, 10)));
		Festival fechasFestivalInvalidas = festivalValido();
		fechasFestivalInvalidas.setFechaFin(null);
		assertThrows(IllegalArgumentException.class,
				() -> service.getReportePeriodo(fechasFestivalInvalidas, UNIDAD_ID,
						INICIO_FESTIVAL, FIN_FESTIVAL));

		verifyNoInteractions(unidadVentaRepository, pedidoRepository, personalRepository);
	}

	@Test
	void calculaSoloConPedidosDevueltosParaElPeriodoYProrrateaSueldosYCanon() {
		Festival festival = festivalValido();
		FoodTruck unidad = unidad(festival);
		unidad.setSuperficieM2(20);
		unidad.setConexionElectrica(true);
		festival.setCostoPorSuperficie(500);
		festival.setPlusUsoElectricidad(2000);
		LocalDate desde = LocalDate.of(2026, 1, 10);
		LocalDate hasta = LocalDate.of(2026, 1, 20);
		Plato plato = plato(100, 40);
		Pedido pedidoEnDesde = pedido(desde, item(plato, 2));
		Pedido pedidoEnHasta = pedido(hasta, item(plato, 2));
		Cocinero cocinero = cocineroLavaplatos();
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID)).thenReturn(Optional.of(unidad));
		when(pedidoRepository.findByUnidadVentaIdAndFestivalConItemsEntreFechas(
				UNIDAD_ID, FESTIVAL_ID, desde, hasta)).thenReturn(List.of(pedidoEnDesde, pedidoEnHasta));
		when(personalRepository.findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID)).thenReturn(List.of(cocinero));

		ReporteRentabilidadNetaPeriodoDTO reporte = service
				.getReportePeriodo(festival, UNIDAD_ID, desde, hasta).orElseThrow();

		assertEquals(11, reporte.diasPeriodo());
		assertEquals(30, reporte.diasTotalesFestival());
		assertEquals(11.0 / 30.0, reporte.proporcionProrrateo());
		assertEquals(400, reporte.importePedidos());
		assertEquals(160, reporte.costoPlatos());
		assertEquals(82_500, reporte.costoSueldos());
		assertEquals(12_000, reporte.canonFestival());
		assertEquals(4_400, reporte.costoCanonProrrateado());
		assertEquals(-86_660, reporte.rentabilidadNeta());
		assertTrue(reporte.completo());
		verify(pedidoRepository).findByUnidadVentaIdAndFestivalConItemsEntreFechas(
				UNIDAD_ID, FESTIVAL_ID, desde, hasta);
	}

	@Test
	void cuentaUnPeriodoDeUnSoloDiaComoUnDiaInclusivo() {
		Festival festival = festivalValido();
		FoodTruck unidad = unidad(festival);
		unidad.setSuperficieM2(20);
		unidad.setConexionElectrica(true);
		festival.setCostoPorSuperficie(500);
		festival.setPlusUsoElectricidad(2000);
		LocalDate fecha = LocalDate.of(2026, 1, 15);
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID)).thenReturn(Optional.of(unidad));
		when(pedidoRepository.findByUnidadVentaIdAndFestivalConItemsEntreFechas(
				UNIDAD_ID, FESTIVAL_ID, fecha, fecha)).thenReturn(List.of());
		when(personalRepository.findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID)).thenReturn(List.of());

		ReporteRentabilidadNetaPeriodoDTO reporte = service
				.getReportePeriodo(festival, UNIDAD_ID, fecha, fecha).orElseThrow();

		assertEquals(1, reporte.diasPeriodo());
		assertEquals(1.0 / 30.0, reporte.proporcionProrrateo());
		assertEquals(400, reporte.costoCanonProrrateado());
		verify(pedidoRepository).findByUnidadVentaIdAndFestivalConItemsEntreFechas(
				UNIDAD_ID, FESTIVAL_ID, fecha, fecha);
	}

	@Test
	void periodoCompletoDelFestivalTieneProporcionUnoYSueldoYCanonSinProrrateo() {
		Festival festival = festivalValido();
		FoodTruck unidad = unidad(festival);
		unidad.setSuperficieM2(20);
		unidad.setConexionElectrica(true);
		festival.setCostoPorSuperficie(500);
		festival.setPlusUsoElectricidad(2000);
		Cocinero cocinero = cocineroLavaplatos();
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID)).thenReturn(Optional.of(unidad));
		when(pedidoRepository.findByUnidadVentaIdAndFestivalConItemsEntreFechas(
				UNIDAD_ID, FESTIVAL_ID, INICIO_FESTIVAL, FIN_FESTIVAL)).thenReturn(List.of());
		when(personalRepository.findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID)).thenReturn(List.of(cocinero));

		ReporteRentabilidadNetaPeriodoDTO reporte = service
				.getReportePeriodo(festival, UNIDAD_ID, INICIO_FESTIVAL, FIN_FESTIVAL).orElseThrow();

		assertEquals(30, reporte.diasPeriodo());
		assertEquals(30, reporte.diasTotalesFestival());
		assertEquals(1.0, reporte.proporcionProrrateo());
		assertEquals(225_000, reporte.costoSueldos());
		assertEquals(12_000, reporte.canonFestival());
		assertEquals(12_000, reporte.costoCanonProrrateado());
	}

	@Test
	void devuelveVacioCuandoLaUnidadNoExiste() {
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID)).thenReturn(Optional.empty());

		assertTrue(service.getReportePeriodo(festivalValido(), UNIDAD_ID,
				INICIO_FESTIVAL, FIN_FESTIVAL).isEmpty());
		verifyNoInteractions(pedidoRepository, personalRepository);
	}

	@Test
	void rechazaUnidadDeOtroFestivalAntesDeConsultarPedidosOPersonal() {
		Festival festival = festivalValido();
		FoodTruck unidadAjena = unidad(festivalConId(FESTIVAL_ID + 1));
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID)).thenReturn(Optional.of(unidadAjena));

		assertThrows(UnidadNoPerteneceFestivalException.class,
				() -> service.getReportePeriodo(festival, UNIDAD_ID, INICIO_FESTIVAL, FIN_FESTIVAL));

		verifyNoInteractions(pedidoRepository, personalRepository);
	}

	@Test
	void dejaElReporteIncompletoYLaRentabilidadNulaSiHaySalarioPendiente() {
		Festival festival = festivalValido();
		FoodTruck unidad = unidad(festival);
		Cocinero cocinero = new Cocinero();
		cocinero.setPlus(123);
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID)).thenReturn(Optional.of(unidad));
		when(pedidoRepository.findByUnidadVentaIdAndFestivalConItemsEntreFechas(
				UNIDAD_ID, FESTIVAL_ID, INICIO_FESTIVAL, FIN_FESTIVAL)).thenReturn(List.of());
		when(personalRepository.findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID)).thenReturn(List.of(cocinero));

		ReporteRentabilidadNetaPeriodoDTO reporte = service
				.getReportePeriodo(festival, UNIDAD_ID, INICIO_FESTIVAL, FIN_FESTIVAL).orElseThrow();

		assertFalse(reporte.completo());
		assertNull(reporte.costoSueldos());
		assertNull(reporte.rentabilidadNeta());
		assertTrue(reporte.estado().contains("categoría conocida"));
	}

	@Test
	void responsableObtieneElReporteDeSuUnidadSinRecibirUnIdDeUnidadDelCliente() {
		Festival festival = festivalValido();
		FoodTruck unidad = unidad(festival);
		Responsable responsable = new Responsable();
		responsable.setUnidadVenta(unidad);
		LocalDate desde = LocalDate.of(2026, 1, 10);
		LocalDate hasta = LocalDate.of(2026, 1, 20);
		when(personalRepository.findByUserUsernameAndEliminadoFalse("responsable"))
				.thenReturn(Optional.of(responsable));
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID)).thenReturn(Optional.of(unidad));
		when(pedidoRepository.findByUnidadVentaIdAndFestivalConItemsEntreFechas(
				UNIDAD_ID, FESTIVAL_ID, desde, hasta)).thenReturn(List.of());
		when(personalRepository.findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID)).thenReturn(List.of(responsable));

		ReporteRentabilidadNetaPeriodoDTO reporte = service
				.getReportePeriodoDeUnidadDelResponsable("responsable", desde, hasta).orElseThrow();

		assertEquals(UNIDAD_ID, reporte.unidadVentaId());
		assertEquals(desde, reporte.desde());
		assertEquals(hasta, reporte.hasta());
		assertTrue(reporte.completo());
		verify(unidadVentaRepository).findByIdConFestival(UNIDAD_ID);
		verify(pedidoRepository).findByUnidadVentaIdAndFestivalConItemsEntreFechas(
				UNIDAD_ID, FESTIVAL_ID, desde, hasta);
	}

	@Test
	void rechazaUnUsuarioInexistenteDeFormaExplicita() {
		when(personalRepository.findByUserUsernameAndEliminadoFalse("inexistente")).thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class,
				() -> service.getReportePeriodoDeUnidadDelResponsable("inexistente",
						INICIO_FESTIVAL, FIN_FESTIVAL));
		verifyNoInteractions(unidadVentaRepository, pedidoRepository);
	}

	@Test
	void rechazaUnUsuarioSinPersonalActivoDeFormaExplicita() {
		when(personalRepository.findByUserUsernameAndEliminadoFalse("inactivo")).thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class,
				() -> service.getReportePeriodoDeUnidadDelResponsable("inactivo",
						INICIO_FESTIVAL, FIN_FESTIVAL));
		verifyNoInteractions(unidadVentaRepository, pedidoRepository);
	}

	@Test
	void deniegaElReporteSiElUsuarioNoEsResponsable() {
		when(personalRepository.findByUserUsernameAndEliminadoFalse("cajero"))
				.thenReturn(Optional.of(new Cajero()));

		assertThrows(AccessDeniedException.class,
				() -> service.getReportePeriodoDeUnidadDelResponsable("cajero",
						INICIO_FESTIVAL, FIN_FESTIVAL));
		verifyNoInteractions(unidadVentaRepository, pedidoRepository);
	}

	@Test
	void devuelveVacioSiElResponsableNoTieneUnidadOFestival() {
		Responsable sinUnidad = new Responsable();
		Responsable sinFestival = new Responsable();
		sinFestival.setUnidadVenta(unidad(null));
		when(personalRepository.findByUserUsernameAndEliminadoFalse("sin-unidad"))
				.thenReturn(Optional.of(sinUnidad));
		when(personalRepository.findByUserUsernameAndEliminadoFalse("sin-festival"))
				.thenReturn(Optional.of(sinFestival));

		assertTrue(service.getReportePeriodoDeUnidadDelResponsable("sin-unidad",
				INICIO_FESTIVAL, FIN_FESTIVAL).isEmpty());
		assertTrue(service.getReportePeriodoDeUnidadDelResponsable("sin-festival",
				INICIO_FESTIVAL, FIN_FESTIVAL).isEmpty());
		verifyNoInteractions(unidadVentaRepository, pedidoRepository);
	}

	@Test
	void validaFechasDelResponsableAntesDeConsultarPedidosOPersonalDeLaUnidad() {
		Festival festival = festivalValido();
		Responsable responsable = new Responsable();
		responsable.setUnidadVenta(unidad(festival));
		when(personalRepository.findByUserUsernameAndEliminadoFalse("responsable"))
				.thenReturn(Optional.of(responsable));

		List<ErrorCampoDTO> errores = service.validarPeriodoDeUnidadDelResponsable(
				"responsable", LocalDate.of(2026, 1, 20), LocalDate.of(2026, 1, 10)).orElseThrow();

		assertEquals("periodoInvertido", errores.get(0).codigo());
		verifyNoInteractions(unidadVentaRepository, pedidoRepository);
	}

	private Festival festivalValido() {
		return festivalConId(FESTIVAL_ID);
	}

	private Festival festivalConId(long id) {
		Festival festival = new Festival();
		festival.setId(id);
		festival.setNombre("Festival " + id);
		festival.setFechaInicio(INICIO_FESTIVAL);
		festival.setFechaFin(FIN_FESTIVAL);
		return festival;
	}

	private FoodTruck unidad(Festival festival) {
		FoodTruck unidad = new FoodTruck();
		unidad.setId(UNIDAD_ID);
		unidad.setNombreComercial("Unidad " + UNIDAD_ID);
		unidad.setFestivalAsistio(festival);
		return unidad;
	}

	private Cocinero cocineroLavaplatos() {
		Cocinero cocinero = new Cocinero();
		cocinero.setPlus(50_000);
		return cocinero;
	}

	private Plato plato(float precioVenta, float costoProduccion) {
		Plato plato = new Plato();
		plato.setPrecioVenta(precioVenta);
		plato.setCostoProduccion(costoProduccion);
		return plato;
	}

	private ItemPedido item(Plato plato, int cantidad) {
		ItemPedido item = new ItemPedido();
		item.setPlato(plato);
		item.setCantidad(cantidad);
		return item;
	}

	private Pedido pedido(LocalDate fecha, ItemPedido... items) {
		Pedido pedido = new Pedido();
		pedido.setFechaTransaccion(fecha);
		for (ItemPedido item : items) {
			item.setPedido(pedido);
			pedido.getLstItems().add(item);
		}
		return pedido;
	}
}
