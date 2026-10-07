package com.unla.tpgrupaloo2hito2grupo01.entities;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class PuestoDesarmable extends UnidadVenta {

	private int carpasComponen;

	// En minutos
	private int tiempoMontaje;

	@Override
	public TipoUnidad getTipo() {
		return TipoUnidad.PUESTO_DESARMABLE;
	}

	// Superficie * costo por superficie, menos el tiempo de montaje (min) * costo por montaje
	@Override
	public double calcularCanon(Festival festival) {
		return (double) getSuperficieM2() * festival.getCostoPorSuperficie()
				- (double) tiempoMontaje * festival.getCostoPorMontaje();
	}
}
