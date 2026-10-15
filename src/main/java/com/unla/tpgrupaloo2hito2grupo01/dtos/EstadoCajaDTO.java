package com.unla.tpgrupaloo2hito2grupo01.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EstadoCajaDTO(
		LocalDate fecha,
		String nombreCajero,
		String nombreUnidad,
		String codigoUnico,
		String nombreFestival,
		boolean festivalActivo,
		BigDecimal recaudacionPedidos,
		CierreCajaDTO cierre,
		String mensaje) {
}
