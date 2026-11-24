package com.unla.tpgrupaloo2hito2grupo01.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CierreCajaDTO(
		long id,
		LocalDate fecha,
		long cajeroId,
		String nombreCajero,
		long unidadVentaId,
		String nombreUnidad,
		String codigoUnico,
		String nombreFestival,
		BigDecimal recaudacionPedidos,
		BigDecimal montoDeclarado,
		BigDecimal diferencia,
		LocalDateTime fechaCierre) {
}
