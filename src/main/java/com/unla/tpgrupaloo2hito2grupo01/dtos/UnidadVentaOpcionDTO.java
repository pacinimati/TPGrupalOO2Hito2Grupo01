package com.unla.tpgrupaloo2hito2grupo01.dtos;

import java.time.LocalDate;

// Unidad de venta ofrecida en el formulario de pedidos, con el festival al que asiste (no persiste)
public record UnidadVentaOpcionDTO(long id, String nombreComercial, String codigoUnico, String festival,
		LocalDate fechaInicio, LocalDate fechaFin) {
}
