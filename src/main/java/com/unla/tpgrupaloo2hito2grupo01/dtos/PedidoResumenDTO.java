package com.unla.tpgrupaloo2hito2grupo01.dtos;

// Resumen del pedido recién registrado, para confirmarlo en pantalla (no persiste)
public record PedidoResumenDTO(long id, String unidadVenta, int cantidadItems, double total) {
}
