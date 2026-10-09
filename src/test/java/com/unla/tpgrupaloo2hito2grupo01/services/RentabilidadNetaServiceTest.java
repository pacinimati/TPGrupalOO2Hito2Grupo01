package com.unla.tpgrupaloo2hito2grupo01.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ReporteRentabilidadNetaDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cajero;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cocinero;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.FoodTruck;
import com.unla.tpgrupaloo2hito2grupo01.entities.ItemPedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Pedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Plato;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IFestivalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPedidoRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUnidadVentaRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.implementation.RentabilidadNetaService;

class RentabilidadNetaServiceTest {

	private static final long FESTIVAL_ID = 1;
	private static final long UNIDAD_ID = 2;

	private final IFestivalRepository festivalRepository = mock(IFestivalRepository.class);
	private final IUnidadVentaRepository unidadVentaRepository = mock(IUnidadVentaRepository.class);
	private final IPedidoRepository pedidoRepository = mock(IPedidoRepository.class);
	private final IPersonalRepository personalRepository = mock(IPersonalRepository.class);
	private final IRentabilidadNetaService service = new RentabilidadNetaService(festivalRepository,
			unidadVentaRepository, pedidoRepository, personalRepository);

	@Test
	void informaCuandoElFestivalNoExiste() {
		when(festivalRepository.findById(FESTIVAL_ID)).thenReturn(Optional.empty());

		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> service.getReporte(FESTIVAL_ID, UNIDAD_ID));

		assertEquals("El festival seleccionado no existe", error.getMessage());
		verify(unidadVentaRepository, never()).findByIdConFestival(UNIDAD_ID);
		verify(pedidoRepository, never()).findByUnidadVentaIdAndFestivalConItems(UNIDAD_ID, FESTIVAL_ID);
	}

	@Test
	void devuelveVacioCuandoLaUnidadNoExiste() {
		when(festivalRepository.findById(FESTIVAL_ID)).thenReturn(Optional.of(festival(FESTIVAL_ID)));
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID)).thenReturn(Optional.empty());

		assertTrue(service.getReporte(FESTIVAL_ID, UNIDAD_ID).isEmpty());
		verify(pedidoRepository, never()).findByUnidadVentaIdAndFestivalConItems(UNIDAD_ID, FESTIVAL_ID);
	}

	@Test
	void rechazaUnaUnidadAsociadaAOtroFestivalAntesDeConsultarPedidos() {
		Festival solicitado = festival(FESTIVAL_ID);
		FoodTruck unidad = unidad(UNIDAD_ID, festival(FESTIVAL_ID + 1));
		when(festivalRepository.findById(FESTIVAL_ID)).thenReturn(Optional.of(solicitado));
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID)).thenReturn(Optional.of(unidad));

		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> service.getReporte(FESTIVAL_ID, UNIDAD_ID));

		assertEquals("La unidad de venta no pertenece al festival seleccionado", error.getMessage());
		verify(pedidoRepository, never()).findByUnidadVentaIdAndFestivalConItems(UNIDAD_ID, FESTIVAL_ID);
	}

	@Test
	void sumaLosIngresosYCostosDeProduccionDeVariosPedidosEItems() {
		Festival festival = festival(FESTIVAL_ID);
		FoodTruck unidad = unidad(UNIDAD_ID, festival);
		Plato hamburguesa = plato(100, 40);
		Plato limonada = plato(50, 20);
		when(festivalRepository.findById(FESTIVAL_ID)).thenReturn(Optional.of(festival));
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID)).thenReturn(Optional.of(unidad));
		when(pedidoRepository.findByUnidadVentaIdAndFestivalConItems(UNIDAD_ID, FESTIVAL_ID))
				.thenReturn(List.of(
						pedido(item(hamburguesa, 2), item(limonada, 3)), // ingreso 350, costo 140
						pedido(item(hamburguesa, 1), item(limonada, 2)))); // ingreso 200, costo 80
		when(personalRepository.findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID)).thenReturn(List.of());

		ReporteRentabilidadNetaDTO reporte = service.getReporte(FESTIVAL_ID, UNIDAD_ID).orElseThrow();

		assertEquals(550, reporte.importePedidos());
		assertEquals(220, reporte.costoPlatos());
		assertTrue(reporte.completo());
		assertEquals(0, reporte.costoSueldos());
	}

	@Test
	void calculaRentabilidadNetaCompletaConPedidoCocineroYCanon() {
		Festival festival = festivalConDuracion(FESTIVAL_ID, 38);
		festival.setCostoPorSuperficie(500);
		festival.setPlusUsoElectricidad(2000);
		FoodTruck unidad = unidad(UNIDAD_ID, festival);
		unidad.setSuperficieM2(20);
		unidad.setConexionElectrica(true);
		Plato plato = plato(100, 40);
		Pedido pedido = pedido(item(plato, 2));
		Cocinero cocinero = new Cocinero();
		cocinero.setPlus(50_000);

		when(festivalRepository.findById(FESTIVAL_ID)).thenReturn(Optional.of(festival));
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID)).thenReturn(Optional.of(unidad));
		when(pedidoRepository.findByUnidadVentaIdAndFestivalConItems(UNIDAD_ID, FESTIVAL_ID))
				.thenReturn(List.of(pedido));
		when(personalRepository.findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID)).thenReturn(List.of(cocinero));

		ReporteRentabilidadNetaDTO reporte = service.getReporte(FESTIVAL_ID, UNIDAD_ID).orElseThrow();

		assertEquals(200, reporte.importePedidos());
		assertEquals(80, reporte.costoPlatos());
		assertEquals(225_000, reporte.costoSueldos());
		assertEquals(12_000, reporte.canon());
		assertEquals(-236_880, reporte.rentabilidadNeta());
		assertTrue(reporte.completo());
		assertTrue(reporte.rentabilidadNeta() != null);
	}

	@Test
	void delegaElCalculoDelCanonALaUnidad() {
		Festival festival = festival(FESTIVAL_ID);
		FoodTruck unidad = unidad(UNIDAD_ID, festival);
		unidad.setSuperficieM2(20);
		unidad.setConexionElectrica(true);
		festival.setCostoPorSuperficie(500);
		festival.setPlusUsoElectricidad(2000);
		when(festivalRepository.findById(FESTIVAL_ID)).thenReturn(Optional.of(festival));
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID)).thenReturn(Optional.of(unidad));
		when(pedidoRepository.findByUnidadVentaIdAndFestivalConItems(UNIDAD_ID, FESTIVAL_ID))
				.thenReturn(List.of());
		when(personalRepository.findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID)).thenReturn(List.of());

		ReporteRentabilidadNetaDTO reporte = service.getReporte(FESTIVAL_ID, UNIDAD_ID).orElseThrow();

		assertEquals(12000, reporte.canon());
	}

	@Test
	void dejaElReporteIncompletoSiHayPersonalConSueldoPendiente() {
		Festival festival = festivalConDuracion(FESTIVAL_ID, 15);
		FoodTruck unidad = unidad(UNIDAD_ID, festival);
		Cocinero cocinero = new Cocinero();
		cocinero.setPlus(123);
		when(festivalRepository.findById(FESTIVAL_ID)).thenReturn(Optional.of(festival));
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID)).thenReturn(Optional.of(unidad));
		when(pedidoRepository.findByUnidadVentaIdAndFestivalConItems(UNIDAD_ID, FESTIVAL_ID))
				.thenReturn(List.of());
		when(personalRepository.findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID)).thenReturn(List.of(cocinero));

		ReporteRentabilidadNetaDTO reporte = service.getReporte(FESTIVAL_ID, UNIDAD_ID).orElseThrow();

		assertFalse(reporte.completo());
		assertNull(reporte.costoSueldos());
		assertNull(reporte.rentabilidadNeta());
		assertTrue(reporte.estado().contains("categoría conocida"));
	}

	@Test
	void calculaSueldoDeLavaplatosParaFestivalDeHastaQuinceDias() {
		ReporteRentabilidadNetaDTO reporte = reporteConCocineroDeDuracion(14, 50_000);

		assertTrue(reporte.completo());
		assertEquals(75_000, reporte.costoSueldos());
	}

	@Test
	void calculaSueldoLavaplatosParaFestivalDeExactamenteQuinceDias() {
		ReporteRentabilidadNetaDTO reporte = reporteConCocineroDeDuracion(15, 50_000);

		assertTrue(reporte.completo());
		assertEquals(150_000, reporte.costoSueldos());
	}

	@Test
	void calculaSueldoDeLavaplatosParaFestivalDeDieciseisATreintaDias() {
		ReporteRentabilidadNetaDTO reporte = reporteConCocineroDeDuracion(16, 50_000);

		assertTrue(reporte.completo());
		assertEquals(150_000, reporte.costoSueldos());
	}

	@Test
	void calculaSueldoLavaplatosParaFestivalDeExactamenteTreintaDias() {
		ReporteRentabilidadNetaDTO reporte = reporteConCocineroDeDuracion(30, 50_000);

		assertTrue(reporte.completo());
		assertEquals(225_000, reporte.costoSueldos());
	}

	@Test
	void calculaSueldoDeLavaplatosParaFestivalDeMasDeTreintaDias() {
		ReporteRentabilidadNetaDTO reporte = reporteConCocineroDeDuracion(31, 50_000);

		assertTrue(reporte.completo());
		assertEquals(225_000, reporte.costoSueldos());
	}

	@Test
	void ejemploOficialLavaplatosEnFestivalDeTreintaYOchoDias() {
		ReporteRentabilidadNetaDTO reporte = reporteConCocineroDeDuracion(38, 50_000);

		assertTrue(reporte.completo());
		assertEquals(225_000, reporte.costoSueldos());
	}

	@Test
	void calculaSueldoDelCajeroConTresAniosAlFinDelFestivalDeCincuentaYDosDias() {
		Festival festival = festivalConDuracion(FESTIVAL_ID, 52);
		FoodTruck unidad = unidad(UNIDAD_ID, festival);
		Cajero cajero = new Cajero();
		cajero.setFechaIngreso(festival.getFechaFin().minusYears(3));
		when(festivalRepository.findById(FESTIVAL_ID)).thenReturn(Optional.of(festival));
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID)).thenReturn(Optional.of(unidad));
		when(pedidoRepository.findByUnidadVentaIdAndFestivalConItems(UNIDAD_ID, FESTIVAL_ID))
				.thenReturn(List.of());
		when(personalRepository.findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID)).thenReturn(List.of(cajero));

		ReporteRentabilidadNetaDTO reporte = service.getReporte(FESTIVAL_ID, UNIDAD_ID).orElseThrow();

		assertTrue(reporte.completo());
		assertEquals(230_000, reporte.costoSueldos());
		assertEquals(-230_000, reporte.rentabilidadNeta());
		assertTrue(reporte.rentabilidadNeta() != null);
	}

	@Test
	void cajeroSinFechaDeIngresoInformaQueNoPuedeDeterminarAntiguedad() {
		Festival festival = festivalConDuracion(FESTIVAL_ID, 52);
		FoodTruck unidad = unidad(UNIDAD_ID, festival);
		when(festivalRepository.findById(FESTIVAL_ID)).thenReturn(Optional.of(festival));
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID)).thenReturn(Optional.of(unidad));
		when(pedidoRepository.findByUnidadVentaIdAndFestivalConItems(UNIDAD_ID, FESTIVAL_ID))
				.thenReturn(List.of());
		when(personalRepository.findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID)).thenReturn(List.of(new Cajero()));

		ReporteRentabilidadNetaDTO reporte = service.getReporte(FESTIVAL_ID, UNIDAD_ID).orElseThrow();

		assertFalse(reporte.completo());
		assertNull(reporte.costoSueldos());
		assertNull(reporte.rentabilidadNeta());
		assertTrue(reporte.estado().contains("falta la fecha de ingreso"));
	}

	@Test
	void cajeroConFechaDeIngresoPosteriorAlFestivalDejaElReporteIncompleto() {
		Festival festival = festivalConDuracion(FESTIVAL_ID, 52);
		FoodTruck unidad = unidad(UNIDAD_ID, festival);
		Cajero cajero = new Cajero();
		cajero.setFechaIngreso(festival.getFechaFin().plusDays(1));
		when(festivalRepository.findById(FESTIVAL_ID)).thenReturn(Optional.of(festival));
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID)).thenReturn(Optional.of(unidad));
		when(pedidoRepository.findByUnidadVentaIdAndFestivalConItems(UNIDAD_ID, FESTIVAL_ID))
				.thenReturn(List.of());
		when(personalRepository.findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID)).thenReturn(List.of(cajero));

		ReporteRentabilidadNetaDTO reporte = service.getReporte(FESTIVAL_ID, UNIDAD_ID).orElseThrow();

		assertFalse(reporte.completo());
		assertNull(reporte.costoSueldos());
		assertNull(reporte.rentabilidadNeta());
		assertTrue(reporte.estado().contains("posterior al fin del festival"));
	}

	@Test
	void cajeroConFinDeFestivalNuloDejaElReporteIncompleto() {
		Festival festival = festivalConDuracion(FESTIVAL_ID, 52);
		festival.setFechaFin(null);
		FoodTruck unidad = unidad(UNIDAD_ID, festival);
		Cajero cajero = new Cajero();
		cajero.setFechaIngreso(LocalDate.of(2023, 1, 1));
		when(festivalRepository.findById(FESTIVAL_ID)).thenReturn(Optional.of(festival));
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID)).thenReturn(Optional.of(unidad));
		when(pedidoRepository.findByUnidadVentaIdAndFestivalConItems(UNIDAD_ID, FESTIVAL_ID))
				.thenReturn(List.of());
		when(personalRepository.findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID)).thenReturn(List.of(cajero));

		ReporteRentabilidadNetaDTO reporte = service.getReporte(FESTIVAL_ID, UNIDAD_ID).orElseThrow();

		assertFalse(reporte.completo());
		assertNull(reporte.costoSueldos());
		assertNull(reporte.rentabilidadNeta());
		assertTrue(reporte.estado().contains("el festival no tiene fechas válidas"));
	}

	@Test
	void conPersonalActivoVacioDevuelveCostoSalarialCeroYNetoCompleto() {
		Festival festival = festival(FESTIVAL_ID);
		FoodTruck unidad = unidad(UNIDAD_ID, festival);
		when(festivalRepository.findById(FESTIVAL_ID)).thenReturn(Optional.of(festival));
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID)).thenReturn(Optional.of(unidad));
		when(pedidoRepository.findByUnidadVentaIdAndFestivalConItems(UNIDAD_ID, FESTIVAL_ID))
				.thenReturn(List.of());
		when(personalRepository.findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID)).thenReturn(List.of());

		ReporteRentabilidadNetaDTO reporte = service.getReporte(FESTIVAL_ID, UNIDAD_ID).orElseThrow();

		assertTrue(reporte.completo());
		assertEquals(0, reporte.costoSueldos());
		assertEquals(0.0, reporte.rentabilidadNeta());
	}

	private Festival festival(long id) {
		Festival festival = new Festival();
		festival.setId(id);
		festival.setNombre("Festival " + id);
		return festival;
	}

	private Festival festivalConDuracion(long id, int dias) {
		Festival festival = festival(id);
		festival.setFechaInicio(LocalDate.of(2026, 1, 1));
		festival.setFechaFin(festival.getFechaInicio().plusDays(dias - 1));
		return festival;
	}

	private ReporteRentabilidadNetaDTO reporteConCocineroDeDuracion(int dias, float plus) {
		Festival festival = festivalConDuracion(FESTIVAL_ID, dias);
		FoodTruck unidad = unidad(UNIDAD_ID, festival);
		Cocinero cocinero = new Cocinero();
		cocinero.setPlus(plus);
		when(festivalRepository.findById(FESTIVAL_ID)).thenReturn(Optional.of(festival));
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID)).thenReturn(Optional.of(unidad));
		when(pedidoRepository.findByUnidadVentaIdAndFestivalConItems(UNIDAD_ID, FESTIVAL_ID))
				.thenReturn(List.of());
		when(personalRepository.findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID)).thenReturn(List.of(cocinero));

		return service.getReporte(FESTIVAL_ID, UNIDAD_ID).orElseThrow();
	}

	private FoodTruck unidad(long id, Festival festival) {
		FoodTruck unidad = new FoodTruck();
		unidad.setId(id);
		unidad.setNombreComercial("Unidad " + id);
		unidad.setFestivalAsistio(festival);
		return unidad;
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

	private Pedido pedido(ItemPedido... items) {
		Pedido pedido = new Pedido();
		for (ItemPedido item : items) {
			item.setPedido(pedido);
			pedido.getLstItems().add(item);
		}
		return pedido;
	}
}
