package com.unla.tpgrupaloo2hito2grupo01.dtos;

import java.time.LocalDate;

public record ReporteRentabilidadNetaPeriodoDTO(
		long unidadVentaId,
		String nombreUnidad,
		String nombreFestival,
		LocalDate desde,
		LocalDate hasta,
		long diasPeriodo,
		long diasTotalesFestival,
		double proporcionProrrateo,
		double importePedidos,
		double costoPlatos,
		Double costoSueldos,
		double canonFestival,
		double costoCanonProrrateado,
		Double rentabilidadNeta,
		boolean completo,
		String estado) {
}
