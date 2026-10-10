package com.unla.tpgrupaloo2hito2grupo01.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.unla.tpgrupaloo2hito2grupo01.entities.Cajero;
import com.unla.tpgrupaloo2hito2grupo01.entities.CategoriaCocina;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cocinero;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.services.implementation.CalculoSalarialService;
import com.unla.tpgrupaloo2hito2grupo01.services.implementation.ConstantesSalariales;

class CalculoSalarialServiceTest {

	private final ICalculoSalarialService service = new CalculoSalarialService();

	@Test
	void calculaElSueldoDeCadaCategoriaDeCocina() {
		Festival festival = festivalConDuracion(38);

		for (CategoriaCocina categoria : CategoriaCocina.values()) {
			Cocinero cocinero = new Cocinero();
			cocinero.setPlus(categoria.getPlus());

			ICalculoSalarialService.Resultado resultado = service.calcularSueldo(cocinero, festival).orElseThrow();

			assertTrue(resultado.esCalculable());
			assertEquals(1.5 * (ConstantesSalariales.SUELDO_BASE + categoria.getPlus()), resultado.sueldo(), 0.001);
		}
	}

	@Test
	void calculaElSueldoDeLavaplatosEnUnFestivalDeTreintaYOchoDias() {
		Festival festival = festivalConDuracion(38);
		Cocinero lavaplatos = new Cocinero();
		lavaplatos.setPlus(CategoriaCocina.LAVAPLATOS.getPlus());

		ICalculoSalarialService.Resultado resultado = service.calcularSueldo(lavaplatos, festival).orElseThrow();

		assertTrue(resultado.esCalculable());
		assertEquals(225_000, resultado.sueldo(), 0.001);
	}

	@Test
	void calculaElSueldoDelCajeroConTresAniosEnUnFestivalDeCincuentaYDosDias() {
		Festival festival = festivalConDuracion(52);
		Cajero cajero = new Cajero();
		cajero.setFechaIngreso(festival.getFechaFin().minusYears(3));

		ICalculoSalarialService.Resultado resultado = service.calcularSueldo(cajero, festival).orElseThrow();

		assertTrue(resultado.esCalculable());
		assertEquals(230_000, resultado.sueldo(), 0.001);
	}

	private Festival festivalConDuracion(int dias) {
		Festival festival = new Festival();
		festival.setFechaInicio(LocalDate.of(2026, 1, 1));
		festival.setFechaFin(festival.getFechaInicio().plusDays(dias - 1));
		return festival;
	}
}
