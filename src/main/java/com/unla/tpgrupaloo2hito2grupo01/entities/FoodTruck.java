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

	// Superficie * costo por superficie, más el plus por electricidad si requiere conexión eléctrica
	@Override
	public double calcularCanon(Festival festival) {
		double canon = (double) getSuperficieM2() * festival.getCostoPorSuperficie();
		if (conexionElectrica) {
			canon += festival.getPlusUsoElectricidad();
		}
		return canon;
	}
}
