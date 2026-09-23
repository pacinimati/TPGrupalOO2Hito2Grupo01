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
}
