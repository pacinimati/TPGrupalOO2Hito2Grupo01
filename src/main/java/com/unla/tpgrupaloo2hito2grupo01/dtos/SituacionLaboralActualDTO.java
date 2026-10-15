package com.unla.tpgrupaloo2hito2grupo01.dtos;

import java.time.LocalDate;

public record SituacionLaboralActualDTO(
		String nombreEmpleado,
		boolean festivalActivo,
		String nombreFestival,
		LocalDate fechaInicio,
		LocalDate fechaFin,
		String nombreUnidad,
		String codigoUnico) {
}
