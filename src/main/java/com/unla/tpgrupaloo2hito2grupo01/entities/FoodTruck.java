package com.unla.tpgrupaloo2hito2grupo01.entities;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class FoodTruck extends UnidadVenta {

	private String patente;

	private boolean conexionElectrica;

	@Override
	public TipoUnidad getTipo() {
		return TipoUnidad.FOOD_TRUCK;
	}
}
