package com.unla.tpgrupaloo2hito2grupo01.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ReporteMayoresCanonDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.FoodTruck;
import com.unla.tpgrupaloo2hito2grupo01.entities.PuestoDesarmable;
import com.unla.tpgrupaloo2hito2grupo01.entities.TipoUnidad;
import com.unla.tpgrupaloo2hito2grupo01.entities.UnidadVenta;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IFestivalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPedidoRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUnidadVentaRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUserRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUserRoleRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.implementation.UnidadVentaService;

// Reporte de mayores canon sin base de datos: los repositorios se simulan con Mockito
class MayoresCanonTest {

	private static final long FESTIVAL_ID = 1;

	private final IUnidadVentaRepository unidadVentaRepository = mock(IUnidadVentaRepository.class);
	private final IFestivalRepository festivalRepository = mock(IFestivalRepository.class);
	private final IUnidadVentaService service = new UnidadVentaService(unidadVentaRepository,
			mock(IPersonalRepository.class), festivalRepository, mock(IPedidoRepository.class),
			mock(IUserRepository.class), mock(IUserRoleRepository.class), null);

	@Test
	void calculaElCanonSegunElTipoDeUnidad() {
		Festival festival = festival();

		assertEquals(10000, foodTruck("A", 20, false).calcularCanon(festival));
		assertEquals(12000, foodTruck("B", 20, true).calcularCanon(festival));
		assertEquals(9000, puesto("C", 20, 100).calcularCanon(festival));
	}

	@Test
	void devuelveLasXUnidadesConMayorCanonDeMayorAMenor() {
		when(festivalRepository.findById(FESTIVAL_ID)).thenReturn(Optional.of(festival()));
		when(unidadVentaRepository.findByFestivalAsistioId(FESTIVAL_ID)).thenReturn(List.of(
				puesto("Puesto chico", 10, 60),         // 5.000 - 600 = 4.400
				foodTruck("Truck grande", 30, true),    // 15.000 + 2.000 = 17.000
				foodTruck("Truck sin luz", 30, false),  // 15.000
				puesto("Puesto mediano", 20, 100)));    // 10.000 - 1.000 = 9.000

		List<ReporteMayoresCanonDTO> reporte = service.getUnidadesConMayorCanon(FESTIVAL_ID, 3);

		assertEquals(List.of("Truck grande", "Truck sin luz", "Puesto mediano"),
				reporte.stream().map(ReporteMayoresCanonDTO::getNombreComercial).toList());
		assertEquals(17000, reporte.get(0).getCanon());
		assertEquals(TipoUnidad.FOOD_TRUCK, reporte.get(0).getTipo());
		assertEquals(TipoUnidad.PUESTO_DESARMABLE, reporte.get(2).getTipo());
	}

	@Test
	void conMenosUnidadesQueLasPedidasDevuelveTodas() {
		when(festivalRepository.findById(FESTIVAL_ID)).thenReturn(Optional.of(festival()));
		when(unidadVentaRepository.findByFestivalAsistioId(FESTIVAL_ID))
				.thenReturn(List.of(foodTruck("Unico", 10, false)));

		assertEquals(1, service.getUnidadesConMayorCanon(FESTIVAL_ID, 5).size());
	}

	@Test
	void rechazaUnaCantidadMenorAUno() {
		assertTrue(service.validarCantidadMayoresCanon(1).isEmpty());
		assertEquals(1, service.validarCantidadMayoresCanon(0).size());
		assertEquals("cantidad", service.validarCantidadMayoresCanon(-2).get(0).campo());
	}

	private Festival festival() {
		Festival festival = new Festival();
		festival.setCostoPorSuperficie(500);
		festival.setCostoPorMontaje(10);
		festival.setPlusUsoElectricidad(2000);
		return festival;
	}

	private UnidadVenta foodTruck(String nombre, float superficie, boolean conexionElectrica) {
		FoodTruck unidad = new FoodTruck();
		unidad.setNombreComercial(nombre);
		unidad.setSuperficieM2(superficie);
		unidad.setConexionElectrica(conexionElectrica);
		return unidad;
	}

	private UnidadVenta puesto(String nombre, float superficie, int tiempoMontaje) {
		PuestoDesarmable unidad = new PuestoDesarmable();
		unidad.setNombreComercial(nombre);
		unidad.setSuperficieM2(superficie);
		unidad.setTiempoMontaje(tiempoMontaje);
		return unidad;
	}
}
