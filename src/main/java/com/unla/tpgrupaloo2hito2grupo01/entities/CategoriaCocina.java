package com.unla.tpgrupaloo2hito2grupo01.entities;

import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;

// Categorías del personal de cocina con su plus fijo (el que se guarda en Cocinero.plus)
@Getter
public enum CategoriaCocina {
	COCINERO("Cocinero", 150000),
	COCINERO_AYUDANTE("Cocinero ayudante", 100000),
	LAVAPLATOS("Lavaplatos", 50000);

	private final String etiqueta;
	private final float plus;

	CategoriaCocina(String etiqueta, float plus) {
		this.etiqueta = etiqueta;
		this.plus = plus;
	}

	// La categoría no se persiste: se deduce del plus fijo guardado en el cocinero
	public static Optional<CategoriaCocina> desdePlus(float plus) {
		return Arrays.stream(values()).filter(c -> c.plus == plus).findFirst();
	}
}
