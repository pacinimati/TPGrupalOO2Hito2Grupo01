package com.unla.tpgrupaloo2hito2grupo01.entities;

import lombok.Getter;

// Roles de empleado que puede dar de alta un Responsable en su unidad de venta
@Getter
public enum TipoEmpleado {
	COCINERO("Cocina"),
	CAJERO("Cajero");

	private final String etiqueta;

	TipoEmpleado(String etiqueta) {
		this.etiqueta = etiqueta;
	}
}
