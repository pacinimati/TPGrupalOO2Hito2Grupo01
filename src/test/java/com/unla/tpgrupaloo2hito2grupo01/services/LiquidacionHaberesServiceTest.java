package com.unla.tpgrupaloo2hito2grupo01.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.unla.tpgrupaloo2hito2grupo01.dtos.LiquidacionHaberesDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Administrador;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cajero;
import com.unla.tpgrupaloo2hito2grupo01.entities.CategoriaCocina;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cocinero;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.FoodTruck;
import com.unla.tpgrupaloo2hito2grupo01.entities.Responsable;
import com.unla.tpgrupaloo2hito2grupo01.entities.TipoEmpleado;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.implementation.LiquidacionHaberesService;

class LiquidacionHaberesServiceTest {

	private static final String USERNAME = "responsable";
	private static final long UNIDAD_ID = 12L;

	private final IPersonalRepository personalRepository = mock(IPersonalRepository.class);
	private final ICalculoSalarialService calculoSalarialService = mock(ICalculoSalarialService.class);
	private final ILiquidacionHaberesService service = new LiquidacionHaberesService(
			personalRepository, calculoSalarialService);

	@Test
	void devuelveListaVaciaSiElResponsableNoTieneUnidadAsignada() {
		Responsable responsable = new Responsable();
		when(personalRepository.findByUserUsernameAndEliminadoFalse(USERNAME))
				.thenReturn(Optional.of(responsable));

		assertTrue(service.getLiquidacionesDeUnidadDelResponsable(USERNAME).isEmpty());

		verify(personalRepository, never()).findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID);
	}

	@Test
	void devuelveListaVaciaCuandoLaUnidadNoTieneEmpleadosActivos() {
		Responsable responsable = responsableConUnidad(unidadConFestival(festivalConDuracion(1)));

		when(personalRepository.findByUserUsernameAndEliminadoFalse(USERNAME))
				.thenReturn(Optional.of(responsable));
		when(personalRepository.findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID))
				.thenReturn(List.of());

		assertTrue(service.getLiquidacionesDeUnidadDelResponsable(USERNAME).isEmpty());

		verify(personalRepository).findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID);
	}

	@Test
	void calculaLiquidacionesDeCocineroYCajeroDeLaUnidadDelResponsable() {
		Festival festival = festivalConDuracion(52);
		FoodTruck unidad = unidadConFestival(festival);
		Responsable responsable = responsableConUnidad(unidad);
		Cocinero cocinero = cocinero(1L, "Ana", "Pérez", CategoriaCocina.LAVAPLATOS);
		Cajero cajero = cajero(2L, "Luis", "Gómez");

		when(personalRepository.findByUserUsernameAndEliminadoFalse(USERNAME))
				.thenReturn(Optional.of(responsable));
		when(personalRepository.findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID))
				.thenReturn(List.of(cocinero, cajero));
		when(calculoSalarialService.calcularSueldo(cocinero, festival))
				.thenReturn(Optional.of(new ICalculoSalarialService.Resultado(225_000, null)));
		when(calculoSalarialService.calcularSueldo(cajero, festival))
				.thenReturn(Optional.of(new ICalculoSalarialService.Resultado(230_000, null)));

		List<LiquidacionHaberesDTO> liquidaciones =
				service.getLiquidacionesDeUnidadDelResponsable(USERNAME);

		assertEquals(2, liquidaciones.size());
		assertEquals(1L, liquidaciones.get(0).empleadoId());
		assertEquals("Ana", liquidaciones.get(0).nombre());
		assertEquals("Pérez", liquidaciones.get(0).apellido());
		assertEquals(TipoEmpleado.COCINERO, liquidaciones.get(0).tipo());
		assertEquals(CategoriaCocina.LAVAPLATOS, liquidaciones.get(0).categoriaCocina());
		assertEquals(225_000.0, liquidaciones.get(0).sueldoCalculado(), 0.001);
		assertNull(liquidaciones.get(0).motivoNoCalculable());

		assertEquals(2L, liquidaciones.get(1).empleadoId());
		assertEquals("Luis", liquidaciones.get(1).nombre());
		assertEquals("Gómez", liquidaciones.get(1).apellido());
		assertEquals(TipoEmpleado.CAJERO, liquidaciones.get(1).tipo());
		assertNull(liquidaciones.get(1).categoriaCocina());
		assertEquals(230_000.0, liquidaciones.get(1).sueldoCalculado(), 0.001);
		assertNull(liquidaciones.get(1).motivoNoCalculable());

		verify(personalRepository).findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID);
		verify(calculoSalarialService).calcularSueldo(cocinero, festival);
		verify(calculoSalarialService).calcularSueldo(cajero, festival);
	}

	@Test
	void excluyeSubtiposNoContemplados() {
		Festival festival = festivalConDuracion(38);
		Responsable responsable = responsableConUnidad(unidadConFestival(festival));
		Cocinero activo = cocinero(1L, "Ana", "Pérez", CategoriaCocina.LAVAPLATOS);
		Administrador administrador = new Administrador();

		when(personalRepository.findByUserUsernameAndEliminadoFalse(USERNAME))
				.thenReturn(Optional.of(responsable));
		when(personalRepository.findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID))
				.thenReturn(List.of(activo, administrador));
		when(calculoSalarialService.calcularSueldo(activo, festival))
				.thenReturn(Optional.of(new ICalculoSalarialService.Resultado(225_000, null)));

		List<LiquidacionHaberesDTO> liquidaciones =
				service.getLiquidacionesDeUnidadDelResponsable(USERNAME);

		assertEquals(1, liquidaciones.size());
		assertEquals(activo.getId(), liquidaciones.get(0).empleadoId());
		assertEquals(225_000.0, liquidaciones.get(0).sueldoCalculado(), 0.001);
		verify(calculoSalarialService, never()).calcularSueldo(administrador, festival);
	}

	@Test
	void lanzaExcepcionSiElUsuarioNoExiste() {
		when(personalRepository.findByUserUsernameAndEliminadoFalse(USERNAME))
				.thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class,
				() -> service.getLiquidacionesDeUnidadDelResponsable(USERNAME));

		verify(personalRepository, never()).findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID);
	}

	@Test
	void rechazaUnUsuarioQueNoEsResponsable() {
		when(personalRepository.findByUserUsernameAndEliminadoFalse(USERNAME))
				.thenReturn(Optional.of(new Cajero()));

		assertThrows(AccessDeniedException.class,
				() -> service.getLiquidacionesDeUnidadDelResponsable(USERNAME));

		verify(personalRepository, never()).findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID);
	}

	@Test
	void informaCuandoLaUnidadNoTieneFestivalAsociado() {
		Responsable responsable = responsableConUnidad(unidadConFestival(null));
		Cocinero cocinero = cocinero(1L, "Ana", "Pérez", CategoriaCocina.LAVAPLATOS);

		when(personalRepository.findByUserUsernameAndEliminadoFalse(USERNAME))
				.thenReturn(Optional.of(responsable));
		when(personalRepository.findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID))
				.thenReturn(List.of(cocinero));

		List<LiquidacionHaberesDTO> liquidaciones =
				service.getLiquidacionesDeUnidadDelResponsable(USERNAME);

		assertEquals(1, liquidaciones.size());
		assertNull(liquidaciones.get(0).sueldoCalculado());
		assertEquals("La unidad de venta no tiene un festival asociado.",
				liquidaciones.get(0).motivoNoCalculable());
		verify(calculoSalarialService, never()).calcularSueldo(cocinero, null);
	}

	@Test
	void conservaElMotivoCuandoElCalculoSalarialNoEsPosible() {
		Festival festival = festivalConDuracion(38);
		Responsable responsable = responsableConUnidad(unidadConFestival(festival));
		Cocinero cocinero = cocinero(1L, "Ana", "Pérez", CategoriaCocina.LAVAPLATOS);
		String motivo = "No se puede calcular el sueldo de cocina: el plus no corresponde a una categoría conocida.";

		when(personalRepository.findByUserUsernameAndEliminadoFalse(USERNAME))
				.thenReturn(Optional.of(responsable));
		when(personalRepository.findByUnidadVentaIdAndEliminadoFalse(UNIDAD_ID))
				.thenReturn(List.of(cocinero));
		when(calculoSalarialService.calcularSueldo(cocinero, festival))
				.thenReturn(Optional.of(new ICalculoSalarialService.Resultado(0.0, motivo)));

		List<LiquidacionHaberesDTO> liquidaciones =
				service.getLiquidacionesDeUnidadDelResponsable(USERNAME);

		assertEquals(1, liquidaciones.size());
		assertNull(liquidaciones.get(0).sueldoCalculado());
		assertEquals(motivo, liquidaciones.get(0).motivoNoCalculable());
	}

	private Responsable responsableConUnidad(FoodTruck unidad) {
		Responsable responsable = new Responsable();
		responsable.setUnidadVenta(unidad);
		return responsable;
	}

	private FoodTruck unidadConFestival(Festival festival) {
		FoodTruck unidad = new FoodTruck();
		unidad.setId(UNIDAD_ID);
		unidad.setFestivalAsistio(festival);
		return unidad;
	}

	private Festival festivalConDuracion(int dias) {
		Festival festival = new Festival();
		festival.setFechaInicio(LocalDate.of(2026, 1, 1));
		festival.setFechaFin(festival.getFechaInicio().plusDays(dias - 1));
		return festival;
	}

	private Cocinero cocinero(long id, String nombre, String apellido, CategoriaCocina categoria) {
		Cocinero cocinero = new Cocinero();
		cocinero.setId(id);
		cocinero.setNombre(nombre);
		cocinero.setApellido(apellido);
		cocinero.setPlus(categoria.getPlus());
		return cocinero;
	}

	private Cajero cajero(long id, String nombre, String apellido) {
		Cajero cajero = new Cajero();
		cajero.setId(id);
		cajero.setNombre(nombre);
		cajero.setApellido(apellido);
		return cajero;
	}
}
