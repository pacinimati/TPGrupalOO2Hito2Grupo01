package com.unla.tpgrupaloo2hito2grupo01.services.implementation;

import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.OptionalLong;

import org.springframework.stereotype.Service;

import com.unla.tpgrupaloo2hito2grupo01.entities.Cajero;
import com.unla.tpgrupaloo2hito2grupo01.entities.CategoriaCocina;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cocinero;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.Personal;
import com.unla.tpgrupaloo2hito2grupo01.services.ICalculoSalarialService;
import com.unla.tpgrupaloo2hito2grupo01.services.ICalculoSalarialService.Resultado;

@Service("calculoSalarialService")
public class CalculoSalarialService implements ICalculoSalarialService {

	@Override
	public Optional<Resultado> calcularSueldo(Personal personal, Festival festival) {
		if (personal instanceof Cocinero cocinero) {
			return Optional.of(calcularSueldoCocinero(cocinero, festival));
		}
		if (personal instanceof Cajero cajero) {
			return Optional.of(calcularSueldoCajero(cajero, festival));
		}
		return Optional.empty();
	}

	private Resultado calcularSueldoCocinero(Cocinero cocinero, Festival festival) {
		OptionalLong diasDuracion = diasDuracionFestival(festival);
		if (diasDuracion.isEmpty()) {
			return new Resultado(0.0, "No se puede calcular el sueldo de cocina: el festival no tiene fechas válidas.");
		}

		Optional<CategoriaCocina> categoria = CategoriaCocina.desdePlus(cocinero.getPlus());
		if (categoria.isEmpty()) {
			return new Resultado(0.0,
					"No se puede calcular el sueldo de cocina: el plus no corresponde a una categoría conocida.");
		}

		return resultado(diasDuracion.getAsLong(), "Plus por categoría (" + categoria.get().getEtiqueta() + ")",
				categoria.get().getPlus());
	}

	private Resultado calcularSueldoCajero(Cajero cajero, Festival festival) {
		OptionalLong diasDuracion = diasDuracionFestival(festival);
		if (diasDuracion.isEmpty()) {
			return new Resultado(0.0, "No se puede calcular el sueldo del Cajero: el festival no tiene fechas válidas.");
		}

		if (cajero.getFechaIngreso() == null) {
			return new Resultado(0.0,
					"No se puede calcular el sueldo del Cajero: falta la fecha de ingreso para determinar la antigüedad.");
		}

		if (cajero.getFechaIngreso().isAfter(festival.getFechaFin())) {
			return new Resultado(0.0,
					"No se puede calcular el sueldo del Cajero: la fecha de ingreso es posterior al fin del festival.");
		}

		int aniosAntiguedad = Period.between(cajero.getFechaIngreso(), festival.getFechaFin()).getYears();
		return resultado(diasDuracion.getAsLong(),
				"Antigüedad (" + aniosAntiguedad + (aniosAntiguedad == 1 ? " año)" : " años)"),
				aniosAntiguedad * ConstantesSalariales.ANTIGUEDAD_POR_ANIO);
	}

	// (int(díasFestival / 15) + 1) * 50% sobre (sueldo base + adicional del rol)
	private Resultado resultado(long diasFestival, String conceptoAdicional, double adicional) {
		double porcentaje = (diasFestival / ConstantesSalariales.DIAS_POR_TRAMO + 1) * ConstantesSalariales.FACTOR_SUELDO;
		double sueldo = porcentaje * (ConstantesSalariales.SUELDO_BASE + adicional);
		return new Resultado(sueldo, null, new Detalle(diasFestival, porcentaje, ConstantesSalariales.SUELDO_BASE,
				conceptoAdicional, adicional));
	}

	// Se cuentan ambos días de calendario que delimitan el festival.
	private OptionalLong diasDuracionFestival(Festival festival) {
		if (festival.getFechaInicio() == null || festival.getFechaFin() == null
				|| festival.getFechaFin().isBefore(festival.getFechaInicio())) {
			return OptionalLong.empty();
		}
		return OptionalLong.of(ChronoUnit.DAYS.between(festival.getFechaInicio(), festival.getFechaFin()) + 1);
	}
}
