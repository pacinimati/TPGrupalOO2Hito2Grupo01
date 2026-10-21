package com.unla.tpgrupaloo2hito2grupo01.dtos;

import java.time.LocalDate;

// Un festival en el que trabajó el empleado, con la unidad de venta y el sueldo de ese festival (no persiste)
public record HistorialLaboralDTO(
		long festivalId,
		String nombreFestival,
		String temporada,
		LocalDate fechaInicio,
		LocalDate fechaFin,
		String estadoFestival,
		String nombreUnidad,
		String codigoUnico,
		String tipoUnidad,
		String rol,
		Double sueldo,
		String motivoNoCalculable) {
}
