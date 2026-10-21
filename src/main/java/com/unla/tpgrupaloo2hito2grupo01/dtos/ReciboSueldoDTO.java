package com.unla.tpgrupaloo2hito2grupo01.dtos;

import java.time.LocalDate;

// Datos del recibo de sueldo de un empleado en un festival, ya calculados (no persiste).
// total = porcentaje * (sueldoBase + adicional)
public record ReciboSueldoDTO(
		String nombreEmpleado,
		int dni,
		String usuario,
		String rol,
		LocalDate fechaIngreso,
		String nombreFestival,
		String temporada,
		LocalDate fechaInicio,
		LocalDate fechaFin,
		long diasFestival,
		String nombreUnidad,
		String codigoUnico,
		String tipoUnidad,
		double sueldoBase,
		String conceptoAdicional,
		double adicional,
		double porcentaje,
		double total,
		LocalDate fechaEmision) {
}
