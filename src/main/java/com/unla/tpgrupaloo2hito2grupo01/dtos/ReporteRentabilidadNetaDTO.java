package com.unla.tpgrupaloo2hito2grupo01.dtos;

public record ReporteRentabilidadNetaDTO(
		long unidadVentaId,
		String nombreUnidad,
		String nombreFestival,
		double importePedidos,
		double costoPlatos,
		Double costoSueldos,
		double canon,
		Double rentabilidadNeta,
		boolean completo,
		String estado) {
}
