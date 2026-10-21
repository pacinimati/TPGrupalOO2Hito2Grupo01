package com.unla.tpgrupaloo2hito2grupo01.dtos;

// Plato con lo que registró en los pedidos de una unidad en un festival (no persiste): en cuántos pedidos
// aparece y cuántas unidades se vendieron
public record PlatoDTO(
		long id,
		String nombre,
		float precioVenta,
		float costoProduccion,
		long cantidadPedidos,
		long unidadesVendidas) {
}
