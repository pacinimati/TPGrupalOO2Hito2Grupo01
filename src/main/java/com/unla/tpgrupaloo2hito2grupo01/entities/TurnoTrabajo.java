package com.unla.tpgrupaloo2hito2grupo01.entities;

import lombok.Getter;

@Getter
public enum TurnoTrabajo {
	MANIANA("Mañana"),
	NOCHE("Noche");

	private final String etiqueta;

	TurnoTrabajo(String etiqueta) {
		this.etiqueta = etiqueta;
	}
}
