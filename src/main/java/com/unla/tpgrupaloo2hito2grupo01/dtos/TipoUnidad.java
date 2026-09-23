package com.unla.tpgrupaloo2hito2grupo01.dtos;

public enum TipoUnidad {
	FOOD_TRUCK("Food Truck"),
	PUESTO_DESARMABLE("Puesto Desarmable");

	private final String descripcion;

	TipoUnidad(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getDescripcion() {
		return descripcion;
	}
}
