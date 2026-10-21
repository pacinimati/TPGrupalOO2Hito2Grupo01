package com.unla.tpgrupaloo2hito2grupo01.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.unla.tpgrupaloo2hito2grupo01.dtos.HistorialLaboralDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ReciboSueldoDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cajero;
import com.unla.tpgrupaloo2hito2grupo01.entities.CategoriaCocina;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cocinero;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.FoodTruck;
import com.unla.tpgrupaloo2hito2grupo01.entities.Personal;
import com.unla.tpgrupaloo2hito2grupo01.entities.Responsable;
import com.unla.tpgrupaloo2hito2grupo01.entities.TurnoTrabajo;
import com.unla.tpgrupaloo2hito2grupo01.entities.UnidadVenta;
import com.unla.tpgrupaloo2hito2grupo01.entities.User;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.implementation.CalculoSalarialService;
import com.unla.tpgrupaloo2hito2grupo01.services.implementation.HistorialLaboralService;

class HistorialLaboralServiceTest {

	private static final String USERNAME = "empleado@epicentro.test";
	private static final long FESTIVAL_ID = 7L;

	private final IPersonalRepository personalRepository = mock(IPersonalRepository.class);
	private final IHistorialLaboralService service = new HistorialLaboralService(personalRepository,
			new CalculoSalarialService());

	@Test
	void muestraElFestivalYLaUnidadDondeTrabajoConSuSueldo() {
		Festival festival = festivalConDuracion(38, LocalDate.of(2020, 6, 1));
		Cocinero lavaplatos = cocinero(CategoriaCocina.LAVAPLATOS, unidad(festival));
		autenticado(lavaplatos);

		List<HistorialLaboralDTO> historial = service.getHistorialLaboral(USERNAME);

		assertEquals(1, historial.size());
		HistorialLaboralDTO item = historial.get(0);
		assertEquals(FESTIVAL_ID, item.festivalId());
		assertEquals("Festival de prueba", item.nombreFestival());
		assertEquals("Finalizado", item.estadoFestival());
		assertEquals("Food Truck La Esquina", item.nombreUnidad());
		assertEquals("Food Truck", item.tipoUnidad());
		assertEquals("Cocina · Lavaplatos", item.rol());
		assertEquals(225_000, item.sueldo(), 0.001);
		assertNull(item.motivoNoCalculable());
	}

	@Test
	void informaElMotivoCuandoElSueldoNoSePuedeCalcular() {
		Festival festival = festivalConDuracion(10, LocalDate.of(2020, 6, 1));
		Cajero cajero = cajero(null, unidad(festival));
		autenticado(cajero);

		HistorialLaboralDTO item = service.getHistorialLaboral(USERNAME).get(0);

		assertNull(item.sueldo());
		assertTrue(item.motivoNoCalculable().contains("fecha de ingreso"));
	}

	@Test
	void devuelveHistorialVacioSiNoTieneUnidadConFestival() {
		autenticado(cocinero(CategoriaCocina.COCINERO, null));

		assertTrue(service.getHistorialLaboral(USERNAME).isEmpty());
	}

	@Test
	void armaElReciboConElDetalleDelSueldoDelCajero() {
		Festival festival = festivalConDuracion(52, LocalDate.of(2026, 9, 25));
		Cajero cajero = cajero(festival.getFechaFin().minusYears(3), unidad(festival));
		autenticado(cajero);

		ReciboSueldoDTO recibo = service.getReciboSueldo(USERNAME, FESTIVAL_ID).orElseThrow();

		assertEquals("Luis Gómez", recibo.nombreEmpleado());
		assertEquals(USERNAME, recibo.usuario());
		assertEquals("Cajero · Turno noche", recibo.rol());
		assertEquals(52, recibo.diasFestival());
		assertEquals(100_000, recibo.sueldoBase(), 0.001);
		assertEquals("Antigüedad (3 años)", recibo.conceptoAdicional());
		assertEquals(15_000, recibo.adicional(), 0.001);
		assertEquals(2.0, recibo.porcentaje(), 0.001);
		assertEquals(230_000, recibo.total(), 0.001);
	}

	@Test
	void noDaReciboDeUnFestivalEnElQueNoTrabajo() {
		Festival festival = festivalConDuracion(38, LocalDate.of(2020, 6, 1));
		autenticado(cocinero(CategoriaCocina.COCINERO, unidad(festival)));

		assertTrue(service.getReciboSueldo(USERNAME, FESTIVAL_ID + 1).isEmpty());
	}

	@Test
	void noDaReciboSiElSueldoNoSePuedeCalcular() {
		Festival festival = festivalConDuracion(38, LocalDate.of(2020, 6, 1));
		autenticado(cajero(null, unidad(festival)));

		assertTrue(service.getReciboSueldo(USERNAME, FESTIVAL_ID).isEmpty());
	}

	@Test
	void rechazaAUsuariosQueNoSonEmpleados() {
		autenticado(new Responsable());

		assertThrows(AccessDeniedException.class, () -> service.getHistorialLaboral(USERNAME));
	}

	@Test
	void fallaSiElUsuarioNoExisteOEstaEliminado() {
		when(personalRepository.findByUserUsernameAndEliminadoFalse(USERNAME)).thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class, () -> service.getHistorialLaboral(USERNAME));
	}

	private void autenticado(Personal personal) {
		User user = new User();
		user.setUsername(USERNAME);
		personal.setUser(user);
		when(personalRepository.findByUserUsernameAndEliminadoFalse(USERNAME)).thenReturn(Optional.of(personal));
	}

	private Cocinero cocinero(CategoriaCocina categoria, UnidadVenta unidad) {
		Cocinero cocinero = new Cocinero();
		cocinero.setNombre("Ana");
		cocinero.setApellido("Pérez");
		cocinero.setPlus(categoria.getPlus());
		cocinero.setUnidadVenta(unidad);
		return cocinero;
	}

	private Cajero cajero(LocalDate fechaIngreso, UnidadVenta unidad) {
		Cajero cajero = new Cajero();
		cajero.setNombre("Luis");
		cajero.setApellido("Gómez");
		cajero.setFechaIngreso(fechaIngreso);
		cajero.setTurnoTrabajo(TurnoTrabajo.NOCHE);
		cajero.setUnidadVenta(unidad);
		return cajero;
	}

	private FoodTruck unidad(Festival festival) {
		FoodTruck unidad = new FoodTruck();
		unidad.setNombreComercial("Food Truck La Esquina");
		unidad.setCodigoUnico("ESQ0000001");
		unidad.setFestivalAsistio(festival);
		return unidad;
	}

	private Festival festivalConDuracion(int dias, LocalDate inicio) {
		Festival festival = new Festival();
		festival.setId(FESTIVAL_ID);
		festival.setNombre("Festival de prueba");
		festival.setFechaInicio(inicio);
		festival.setFechaFin(inicio.plusDays(dias - 1));
		return festival;
	}
}
