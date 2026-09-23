package com.unla.tpgrupaloo2hito2grupo01.dtos;

import com.unla.tpgrupaloo2hito2grupo01.entities.TipoUnidad;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Datos de una unidad de venta para mostrar en listados (no persiste).
// Los campos propios de cada tipo quedan vacíos cuando no aplican.
@Getter @Setter @NoArgsConstructor
public class UnidadVentaDTO {

	private long id;

	private String nombreComercial;

	private String codigoUnico;

	private TipoUnidad tipo;

	private float superficieM2;

	private String responsable;

	// Food Truck
	private String patente;

	private boolean conexionElectrica;

	// Puesto Desarmable
	private int carpasComponen;

	private int tiempoMontaje;
}
